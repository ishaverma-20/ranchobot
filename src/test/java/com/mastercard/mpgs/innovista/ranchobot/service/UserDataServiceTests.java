package com.mastercard.mpgs.innovista.ranchobot.service;

import com.mastercard.mpgs.innovista.ranchobot.model.Order;
import com.mastercard.mpgs.innovista.ranchobot.util.AppConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the UserDataService.
 * These tests verify the data processing and calculation logic in isolation.
 */
class UserDataServiceTests {

    private UserDataService userDataService;

    @BeforeEach
    void setUp() {
        // 1. Create a fresh instance of the service for each test
        userDataService = new UserDataService();

        // 2. Create a controlled list of mock orders for our tests
        Order order1 = new Order();
        order1.setMerchantId(AppConstants.HARDCODED_MERCHANT_ID);
        order1.setOrderStatus("CAPTURE");
        order1.setOrderAmount(100.0);

        Order order2 = new Order();
        order2.setMerchantId(AppConstants.HARDCODED_MERCHANT_ID);
        order2.setOrderStatus("CAPTURE");
        order2.setOrderAmount(150.50);

        Order order3 = new Order();
        order3.setMerchantId(AppConstants.HARDCODED_MERCHANT_ID);
        order3.setOrderStatus("FAILED");
        order3.setOrderAmount(50.0);

        Order order4 = new Order();
        order4.setMerchantId("ANOTHER_MERCHANT"); // Order from a different merchant
        order4.setOrderStatus("CAPTURE");
        order4.setOrderAmount(1000.0);

        // 3. Manually inject our mock data into the private 'allOrders' field.
        // This bypasses the need for the @PostConstruct 'init' method and the file system.
        ReflectionTestUtils.setField(userDataService, "allOrders", List.of(order1, order2, order3, order4));
    }

    @Test
    @DisplayName("Should correctly count orders by status for the hardcoded merchant")
    void testCountOrdersByStatus() {
        // Test counting 'CAPTURE' orders
        UserDataService.StatusResponse captureResponse = userDataService.countOrdersByStatus("CAPTURE");
        assertEquals(2, captureResponse.count(), "Should find 2 CAPTURE orders for the specified merchant");

        // Test counting 'FAILED' orders
        UserDataService.StatusResponse failedResponse = userDataService.countOrdersByStatus("FAILED");
        assertEquals(1, failedResponse.count(), "Should find 1 FAILED order for the specified merchant");

        // Test a status with no orders
        UserDataService.StatusResponse authorizeResponse = userDataService.countOrdersByStatus("AUTHORIZE");
        assertEquals(0, authorizeResponse.count(), "Should find 0 AUTHORIZE orders");
    }

    @Test
    @DisplayName("Should correctly calculate the total order amount for the hardcoded merchant")
    void testCalculateTotalAmount() {
        // Execute the tool method
        UserDataService.TotalAmountResponse response = userDataService.calculateTotalAmount();

        // Verify the sum is correct (100.0 + 150.50 + 50.0) and ignores the other merchant's order
        assertEquals(300.50, response.totalAmount(), "Total amount should be the sum of orders for the specified merchant only");
    }
}