package com.mastercard.mpgs.innovista.ranchobot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
public class AppConfig {

    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    @Value("classpath:/data/*.txt")
    private Resource[] resources;

    @Bean
    public TokenTextSplitter tokenTextSplitter() {
        // Defines the strategy for splitting documents into smaller chunks.
        return new TokenTextSplitter(
                400,
                100,
                5,
                1000,
                true);
    }

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel, TokenTextSplitter tokenTextSplitter) {
        // This bean creates the in-memory vector store at startup.
        SimpleVectorStore.SimpleVectorStoreBuilder builder = SimpleVectorStore.builder(embeddingModel);
        SimpleVectorStore vectorStore = builder.build();

        List<Document> allDocuments = new ArrayList<>();
        Arrays.stream(resources).forEach(resource -> {
            TextReader textReader = new TextReader(resource);
            textReader.getCustomMetadata().put("file_name", resource.getFilename());
            allDocuments.addAll(textReader.get());
        });
        logger.info("Extracted {} documents from resources.", allDocuments.size());

        List<Document> chunkedDocuments = tokenTextSplitter.apply(allDocuments);
        logger.info("Transformed documents into {} chunks.", chunkedDocuments.size());

        vectorStore.add(chunkedDocuments);
        logger.info("Vector Store initialized and {} chunks loaded.", chunkedDocuments.size());

        return vectorStore;
    }
}
