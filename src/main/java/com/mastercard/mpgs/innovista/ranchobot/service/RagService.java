package com.mastercard.mpgs.innovista.ranchobot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RagService {

    private static final Logger logger = LoggerFactory.getLogger(RagService.class);

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final UserDataService userDataService;

    @Value("classpath:/prompts/rag-prompt.st")
    private Resource ragPromptResource;

    public RagService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore, UserDataService userDataService) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
        this.userDataService = userDataService;
    }

    public String chatWithRancho(String userMessage) {
        SearchRequest request = SearchRequest.builder()
                .query(userMessage)
                .topK(4)
                .similarityThreshold(0.6)
                .build();
        List<Document> relevantDocuments = vectorStore.similaritySearch(request);
        String publicContext = relevantDocuments.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));

        logger.info("--- Retrieved Public RAG Context ---\n{}", publicContext.isEmpty() ? "[No relevant public context found]" : publicContext);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isLoggedIn = authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);

        if (!isLoggedIn && publicContext.isEmpty()) {
            logger.info("No public context found for anonymous user. Returning default message.");
            return "I do not have enough information regarding that topic to provide an answer.";
        }

        PromptTemplate promptTemplate = new PromptTemplate(ragPromptResource);
        Map<String, Object> promptParameters = Map.of(
                "context", publicContext,
                "question", userMessage
        );
        Prompt prompt = promptTemplate.create(promptParameters);

        ChatClient.ChatClientRequestSpec requestSpec = chatClient.prompt(prompt);

        if (isLoggedIn) {
            logger.info("User is logged in. Making user data tools available.");
            String currentMerchantId = "APPLE_MER4";

            requestSpec.system(s -> s.param("merchantId", currentMerchantId));
            requestSpec.tools(userDataService);
        } else {
            logger.info("User is anonymous.");
        }

        return requestSpec.call().content();
    }
}