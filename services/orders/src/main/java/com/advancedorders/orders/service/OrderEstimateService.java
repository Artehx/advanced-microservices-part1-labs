package com.advancedorders.orders.service;

import com.advancedorders.orders.api.OrderEstimateResponse;
import com.advancedorders.orders.client.InventoryClient;
import com.advancedorders.orders.client.ShippingClient;
import org.springframework.stereotype.Service;

@Service
public class OrderEstimateService {
    private final InventoryClient inventory;
    private final ShippingClient shipping;
    public OrderEstimateService(InventoryClient inventory, ShippingClient shipping) {
        this.inventory = inventory;
        this.shipping = shipping;
    }

    public OrderEstimateResponse estimate(String sku, int quantity, String destination, int weightGrams) {
        // TODO P01: validate inputs before remote calls.
        // Query Inventory. If insufficient, return a negative result without calling Shipping.
        // Otherwise obtain Shipping quote and compose the response. Never invent a quote.
        throw new UnsupportedOperationException("P01: implement estimate");
    }
}
