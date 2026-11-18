package com.advancedorders.orders.service;

import com.advancedorders.orders.client.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderEstimateExercise {
    private final InventoryClient inventory=mock(InventoryClient.class);
    private final ShippingClient shipping=mock(ShippingClient.class);
    private final OrderEstimateService service=new OrderEstimateService(inventory,shipping);
    @Test void availableOrderIncludesQuote() {
        when(inventory.getInventory("SKU-001")).thenReturn(new InventorySnapshot("SKU-001",42,0,"inventory","1"));
        when(shipping.quote("ES",500)).thenReturn(new ShippingQuote("ES",500,499,"EUR",3,"SIMULATED"));
        var result=service.estimate("SKU-001",2,"ES",500);
        assertThat(result.canBeFulfilled()).isTrue();
        assertThat(result.inventoryAvailable()).isEqualTo(42);
        assertThat(result.shipping().priceCents()).isEqualTo(499);
        verify(shipping).quote("ES",500);
    }
    @Test void insufficientStockSkipsShipping() {
        when(inventory.getInventory("SKU-001")).thenReturn(new InventorySnapshot("SKU-001",42,0,"inventory","1"));
        var result=service.estimate("SKU-001",43,"ES",500);
        assertThat(result.canBeFulfilled()).isFalse();
        assertThat(result.shipping()).isNull();
        verifyNoInteractions(shipping);
    }
    @Test void invalidInputMakesNoRemoteCalls() {
        assertThatThrownBy(()->service.estimate("SKU-001",0,"ES",500)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()->service.estimate("SKU-001",1,"es",500)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()->service.estimate("SKU-001",1,"ES",10001)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()->service.estimate("",1,"ES",500)).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(inventory,shipping);
    }
    @Test void shippingFailureIsNotInventedAsQuote() {
        when(inventory.getInventory("SKU-001")).thenReturn(new InventorySnapshot("SKU-001",42,0,"inventory","1"));
        when(shipping.quote("ES",500)).thenThrow(new ShippingUnavailableException("unavailable",null));
        assertThatThrownBy(()->service.estimate("SKU-001",2,"ES",500)).isInstanceOf(ShippingUnavailableException.class);
    }
}
