package com.advancedorders.orders.client;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

/** Explicit decorators: no Spring AOP, no implicit annotation order. */
@Component
public class InventoryResilience {
    private static final Logger log = LoggerFactory.getLogger(InventoryResilience.class);
    private final CircuitBreaker circuit;
    private final Retry retry;
    private final Bulkhead bulkhead;
    private final boolean retryEnabled, circuitEnabled, bulkheadEnabled;
    private final AtomicLong attempts = new AtomicLong();

    public InventoryResilience(
            @Value("${training.resilience.retry-enabled:false}") boolean retryEnabled,
            @Value("${training.resilience.circuit-enabled:false}") boolean circuitEnabled,
            @Value("${training.resilience.bulkhead-enabled:false}") boolean bulkheadEnabled,
            @Value("${training.resilience.max-attempts:3}") int maxAttempts,
            @Value("${training.resilience.retry-wait-ms:100}") long retryWait,
            @Value("${training.resilience.open-ms:5000}") long openMs,
            @Value("${training.resilience.max-concurrent:2}") int maxConcurrent) {
        this.retryEnabled = retryEnabled;
        this.circuitEnabled = circuitEnabled;
        this.bulkheadEnabled = bulkheadEnabled;
        circuit = CircuitBreaker.of("inventory", CircuitBreakerConfig.custom()
                .slidingWindowSize(4).minimumNumberOfCalls(4).failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofMillis(openMs))
                .permittedNumberOfCallsInHalfOpenState(2)
                .ignoreException(ex -> !isTransient(ex))
                .build());
        retry = Retry.of("inventory", RetryConfig.custom()
                .maxAttempts(maxAttempts).waitDuration(Duration.ofMillis(retryWait))
                .retryOnException(InventoryResilience::isTransient).build());
        bulkhead = Bulkhead.of("inventory", BulkheadConfig.custom()
                .maxConcurrentCalls(maxConcurrent).maxWaitDuration(Duration.ZERO).build());
        circuit.getEventPublisher().onStateTransition(e -> log.info("inventory circuit {}", e.getStateTransition()));
        retry.getEventPublisher().onRetry(e -> log.info("inventory retry attempt={}", e.getNumberOfRetryAttempts()));
        bulkhead.getEventPublisher().onCallRejected(e -> log.warn("inventory bulkhead rejected"));
    }

    static boolean isTransient(Throwable ex) {
        if (!(ex instanceof InventoryUnavailableException)) return false;
        Throwable cause = ex.getCause();
        if (cause instanceof ResourceAccessException) return true;
        if (cause instanceof HttpServerErrorException http) {
            int status = http.getStatusCode().value();
            return status == 502 || status == 503 || status == 504;
        }
        return false;
    }

    public <T> T execute(Supplier<T> request) {
        Supplier<T> call = () -> {
            attempts.incrementAndGet();
            return request.get();
        };
        // Inner circuit sees each actual attempt; outer retry may try again.
        // Outer bulkhead holds a permit for the entire logical operation.
        if (circuitEnabled) call = CircuitBreaker.decorateSupplier(circuit, call);
        if (retryEnabled) call = Retry.decorateSupplier(retry, call);
        if (bulkheadEnabled) call = Bulkhead.decorateSupplier(bulkhead, call);
        return call.get();
    }

    public Map<String, Object> snapshot() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("attempts", attempts.get());
        m.put("retryEnabled", retryEnabled);
        m.put("circuitEnabled", circuitEnabled);
        m.put("bulkheadEnabled", bulkheadEnabled);
        m.put("circuitState", circuit.getState().name());
        m.put("bufferedCalls", circuit.getMetrics().getNumberOfBufferedCalls());
        m.put("failedCalls", circuit.getMetrics().getNumberOfFailedCalls());
        m.put("notPermittedCalls", circuit.getMetrics().getNumberOfNotPermittedCalls());
        m.put("availablePermits", bulkhead.getMetrics().getAvailableConcurrentCalls());
        return m;
    }
}
