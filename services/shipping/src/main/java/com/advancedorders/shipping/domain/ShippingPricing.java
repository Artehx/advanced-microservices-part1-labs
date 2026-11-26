package com.advancedorders.shipping.domain;
public final class ShippingPricing {
    private ShippingPricing() {}
    public record Quote(String destination,int weightGrams,long priceCents,String currency,int estimatedDays,String provider) {}
    public static Quote quote(String destination,int weightGrams) {
        if(destination==null || !destination.matches("[A-Z]{2}") || weightGrams<1 || weightGrams>10000)
            throw new IllegalArgumentException("Invalid destination or weightGrams");
        boolean domestic=destination.equals("ES");
        long price=domestic?(weightGrams<=2000?499:899):1499;
        return new Quote(destination,weightGrams,price,"EUR",domestic?3:7,"SIMULATED");
    }
}
