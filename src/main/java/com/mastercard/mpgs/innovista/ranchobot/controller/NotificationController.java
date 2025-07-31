package com.mastercard.mpgs.innovista.ranchobot.controller;

import com.mastercard.mpgs.innovista.ranchobot.service.NotificationService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List; // Import List

@RestController
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping(path = "/api/notifications", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    // The endpoint now streams a List of Strings
    public Flux<List<String>> getNotifications() {
        return notificationService.getNotificationStream();
    }
}