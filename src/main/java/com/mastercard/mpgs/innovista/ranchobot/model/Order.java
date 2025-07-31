package com.mastercard.mpgs.innovista.ranchobot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// A simple data class to represent an order from your JSON file
@JsonIgnoreProperties(ignoreUnknown = true)
public class Order {
    private double orderAmount;
    private String merchantId;
    private String orderCurrency;
    private String orderId;
    private String orderStatus;

    // Standard Getters and Setters
    public double getOrderAmount() { return orderAmount; }
    public void setOrderAmount(double orderAmount) { this.orderAmount = orderAmount; }
    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public String getOrderCurrency() { return orderCurrency; }
    public void setOrderCurrency(String orderCurrency) { this.orderCurrency = orderCurrency; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
}