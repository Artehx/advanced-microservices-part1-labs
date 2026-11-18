package com.advancedorders.orders.client;

public class ShippingUnavailableException extends RuntimeException {
    public ShippingUnavailableException(String message, Throwable cause) { super(message, cause); }
}
