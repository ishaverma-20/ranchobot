package com.mastercard.mpgs.innovista.ranchobot.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mastercard.mpgs.innovista.ranchobot.model.Order;
import com.mastercard.mpgs.innovista.ranchobot.util.AppConstants;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.annotation.Description;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Description("Service to handle user-specific order data and statistics")
public class UserDataService {

    private static final Logger logger = LoggerFactory.getLogger(UserDataService.class);

    // Define request and response objects for our tools
    public record StatusResponse(long count) {}
    public record TotalAmountResponse(double totalAmount) {}

    // A record to hold the results of the ratio calculation ---
    public record StatusRatioResponse(
            long authorizeCount,
            long captureCount,
            long failedCount,
            long totalCount,
            String authorizeRatio,
            String captureRatio,
            String failedRatio
    ) {
        @Override
        public String toString() {
            return String.format(
                    "Total Orders: %d\n- AUTHORIZED: %d (%.2f%%)\n- CAPTURED: %d (%.2f%%)\n- FAILED: %d (%.2f%%)",
                    totalCount,
                    authorizeCount, Double.parseDouble(authorizeRatio),
                    captureCount, Double.parseDouble(captureRatio),
                    failedCount, Double.parseDouble(failedRatio)
            );
        }
    }

    private List<Order> allOrders;

    @PostConstruct
    public void init() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream inputStream = new ClassPathResource("orderData/orders.json").getInputStream()) {
            this.allOrders = mapper.readValue(inputStream, new TypeReference<>() {});
            logger.info("Loaded {} orders from JSON for UserDataService.", allOrders.size());
        }
    }

    @Tool(description = "Get the count of orders for a specific status (e.g., CAPTURE, FAILED, AUTHORIZE).")
    public StatusResponse countOrdersByStatus(String status) {
        logger.info(">>> TOOL EXECUTED: countOrdersByStatus with status: {}", status);
        long count = allOrders.stream()
                .filter(order -> AppConstants.HARDCODED_MERCHANT_ID.equals(order.getMerchantId()))
                .filter(order -> status.equalsIgnoreCase(order.getOrderStatus()))
                .count();
        return new StatusResponse(count);
    }

    @Tool(description = "Calculate the total amount of all orders for the current user.")
    public TotalAmountResponse calculateTotalAmount() {
        logger.info(">>> TOOL EXECUTED: calculateTotalAmount for merchant: {}", AppConstants.HARDCODED_MERCHANT_ID);
        double total = allOrders.stream()
                .filter(order -> AppConstants.HARDCODED_MERCHANT_ID.equals(order.getMerchantId()))
                .mapToDouble(Order::getOrderAmount)
                .sum();
        return new TotalAmountResponse(total);
    }

    //A tool to calculate the breakdown and ratios of order statuses ---
    @Tool(description = "Get a full breakdown of order counts and their ratios for AUTHORIZE, CAPTURE, and FAILED statuses.")
    public StatusRatioResponse calculateStatusRatios() {
        logger.info(">>> TOOL EXECUTED: calculateStatusRatios for merchant: {}", AppConstants.HARDCODED_MERCHANT_ID);

        // Filter orders for the current merchant
        List<Order> merchantOrders = allOrders.stream()
                .filter(order -> AppConstants.HARDCODED_MERCHANT_ID.equals(order.getMerchantId()))
                .toList();

        // Group by status and count them
        Map<String, Long> countsByStatus = merchantOrders.stream()
                .collect(Collectors.groupingBy(Order::getOrderStatus, Collectors.counting()));

        long authorizeCount = countsByStatus.getOrDefault("AUTHORIZE", 0L);
        long captureCount = countsByStatus.getOrDefault("CAPTURE", 0L);
        long failedCount = countsByStatus.getOrDefault("FAILED", 0L);
        long totalCount = authorizeCount + captureCount + failedCount;

        // Calculate ratios and format to two decimal places
        DecimalFormat df = new DecimalFormat("0.00");
        String authorizeRatio = (totalCount == 0) ? "0.00" : df.format((double) authorizeCount / totalCount * 100);
        String captureRatio = (totalCount == 0) ? "0.00" : df.format((double) captureCount / totalCount * 100);
        String failedRatio = (totalCount == 0) ? "0.00" : df.format((double) failedCount / totalCount * 100);

        return new StatusRatioResponse(
                authorizeCount, captureCount, failedCount, totalCount,
                authorizeRatio, captureRatio, failedRatio
        );
    }
}