package com.advancedorders.orders.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ShippingClient {
    private final RestClient client;
    public ShippingClient(RestClient.Builder builder,
            @Value("${training.shipping.base-url:http://localhost:8084}") String baseUrl) {
        this.client = builder.clone().baseUrl(baseUrl).build();
    }

    public ShippingQuote quote(String destination, int weightGrams) {
        // TODO P01: GET /api/shipping/quote with destination and weightGrams.
        // Deserialize ShippingQuote; reject empty response and translate remote failure.
        throw new UnsupportedOperationException("P01: implement ShippingClient.quote");
    }
}
