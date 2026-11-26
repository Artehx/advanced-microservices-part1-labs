package com.advancedorders.shipping.api;

import org.springframework.http.HttpStatus;
import com.advancedorders.shipping.domain.ShippingPricing;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Deterministic quote simulator. Real carrier integration is introduced later. */
@RestController
@RequestMapping("/api/shipping")
public class ShippingController {
    public record ShippingQuote(String destination, int weightGrams, long priceCents,
                                String currency, int estimatedDays, String provider) {}

    @GetMapping("/quote")
    public ShippingQuote quote(@RequestParam(defaultValue = "ES") String destination,
                                @RequestParam(defaultValue = "500") int weightGrams) {
        try {
            var q=ShippingPricing.quote(destination,weightGrams);
            return new ShippingQuote(q.destination(),q.weightGrams(),q.priceCents(),q.currency(),q.estimatedDays(),q.provider());
        } catch(IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,e.getMessage());
        }
    }
}
