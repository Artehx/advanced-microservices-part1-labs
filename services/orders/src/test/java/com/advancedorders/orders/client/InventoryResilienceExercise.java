package com.advancedorders.orders.client;

import java.util.concurrent.atomic.AtomicInteger;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;
import static org.assertj.core.api.Assertions.*;

class InventoryResilienceExercise {
    private InventoryUnavailableException failure() {
        return new InventoryUnavailableException("test", new ResourceAccessException("timeout"));
    }
    @Test void retryCountsInitialAttemptAndRetries() {
        var p = new InventoryResilience(true,false,false,3,1,5000,2);
        var n = new AtomicInteger();
        assertThatThrownBy(() -> p.execute(() -> { n.incrementAndGet(); throw failure(); }))
            .isInstanceOf(InventoryUnavailableException.class);
        assertThat(n.get()).isEqualTo(3);
    }
    @Test void circuitOpensAndPreventsDownstreamCall() {
        var p = new InventoryResilience(false,true,false,3,1,5000,2);
        for(int i=0;i<4;i++) assertThatThrownBy(() -> p.execute(() -> {throw failure();}))
            .isInstanceOf(InventoryUnavailableException.class);
        assertThatThrownBy(() -> p.execute(() -> "never")).isInstanceOf(CallNotPermittedException.class);
        assertThat(p.snapshot()).containsEntry("circuitState","OPEN").containsEntry("attempts",4L);
    }
    @Test void selectedHttpErrorsOnly() {
        var p = new InventoryResilience(true,false,false,3,1,5000,2);
        assertThatThrownBy(() -> p.execute(() -> { throw new InventoryUnavailableException("bad",new org.springframework.web.client.HttpServerErrorException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)); }))
            .isInstanceOf(InventoryUnavailableException.class);
        assertThat(p.snapshot()).containsEntry("attempts",1L);
        assertThatThrownBy(() -> p.execute(() -> { throw new InventoryUnavailableException("busy",new org.springframework.web.client.HttpServerErrorException(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE)); }))
            .isInstanceOf(InventoryUnavailableException.class);
        assertThat(p.snapshot()).containsEntry("attempts",4L);
    }
    @Test void permanentErrorIsNotRetried() {
        var p = new InventoryResilience(true,true,false,3,1,5000,2);
        assertThatThrownBy(() -> p.execute(() -> {throw new InventoryUnavailableException("invalid",null);}))
            .isInstanceOf(InventoryUnavailableException.class);
        assertThat(p.snapshot()).containsEntry("attempts",1L).containsEntry("bufferedCalls",0);
    }
}
