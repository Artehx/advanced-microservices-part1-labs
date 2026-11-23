package com.advancedorders.orders.api;

import com.advancedorders.orders.client.InventoryUnavailableException;
import com.advancedorders.orders.client.ShippingUnavailableException;
import org.springframework.http.HttpStatus;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiError("INVALID_REQUEST", ex.getMessage()));
    }

    @ExceptionHandler(CallNotPermittedException.class)
    ResponseEntity<ApiError> handleOpenCircuit(CallNotPermittedException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ApiError("CIRCUIT_OPEN", "Inventory circuit is open"));
    }

    @ExceptionHandler(BulkheadFullException.class)
    ResponseEntity<ApiError> handleBulkhead(BulkheadFullException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ApiError("BULKHEAD_FULL", "Inventory capacity exhausted"));
    }

    @ExceptionHandler(ShippingUnavailableException.class)
    ResponseEntity<ApiError> handleShippingUnavailable(ShippingUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new ApiError("SHIPPING_UNAVAILABLE",ex.getMessage()));
    }

    @ExceptionHandler(InventoryUnavailableException.class)
    ResponseEntity<ApiError> handleInventoryUnavailable(InventoryUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ApiError("INVENTORY_UNAVAILABLE", ex.getMessage()));
    }
}
