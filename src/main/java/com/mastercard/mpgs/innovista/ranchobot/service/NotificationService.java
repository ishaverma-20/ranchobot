package com.mastercard.mpgs.innovista.ranchobot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private final String notificationFilePath;

    public NotificationService(@Value("${notifications.file.path}") String notificationFilePath) {
        this.notificationFilePath = notificationFilePath;
        logger.info("Notifications will be read from: {}", notificationFilePath);
    }

    public Flux<List<String>> getNotificationStream() {
        // Poll the file every 2 seconds
        return Flux.interval(Duration.ofSeconds(2))
                // On each interval, call the method to read the entire file
                .map(i -> readAllLines());
    }

    private List<String> readAllLines() {
        if (!Files.exists(Paths.get(notificationFilePath))) {
            logger.warn("Notification file not found at: {}", notificationFilePath);
            return Collections.emptyList();
        }

        try {
            // Read all lines from the file and return them as a List
            return Files.readAllLines(Paths.get(notificationFilePath));
        } catch (Exception e) {
            logger.error("Error reading notification file: {}", e.getMessage());
            return Collections.emptyList(); // Return an empty list on error
        }
    }
}