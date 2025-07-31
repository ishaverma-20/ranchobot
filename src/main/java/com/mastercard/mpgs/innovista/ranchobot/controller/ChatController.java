package com.mastercard.mpgs.innovista.ranchobot.controller;

import com.mastercard.mpgs.innovista.ranchobot.model.ChatRequest;
import com.mastercard.mpgs.innovista.ranchobot.model.ChatResponse;
import com.mastercard.mpgs.innovista.ranchobot.service.RagService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final RagService ragService;

    // The base ChatClient is no longer needed in this controller
    public ChatController(RagService ragService) {
        this.ragService = ragService;
    }

    @GetMapping("/auth/status")
    public Map<String, Boolean> getAuthStatus() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isLoggedIn = authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);
        return Map.of("isAuthenticated", isLoggedIn);
    }

    @PostMapping("/rancho-chat")
    public ChatResponse ranchoChat(@RequestBody ChatRequest request) {
        String ranchoResponse = ragService.chatWithRancho(request.message());
        return new ChatResponse(ranchoResponse);
    }
}