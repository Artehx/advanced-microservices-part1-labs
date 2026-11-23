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

/** Starter: diagnostics and wiring are provided; students implement policies and composition. */
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
        // TODO P02.2: configure Retry with maxAttempts, wait and transient exception predicate.
        // TODO P02.3: configure CircuitBreaker count window 4, min 4, failure rate 50%, openMs, 2 half-open probes; ignore permanent failures.
        // TODO P02.4: configure Bulkhead maxConcurrent, zero wait.
        // Defaults only allow diagnostics to start; protections are not wired below.
        circuit = CircuitBreaker.ofDefaults("inventory");
        retry = Retry.ofDefaults("inventory");
        bulkhead = Bulkhead.ofDefaults("inventory");
        circuit.getEventPublisher().onStateTransition(e -> log.info("inventory circuit {}", e.getStateTransition()));
        retry.getEventPublisher().onRetry(e -> log.info("inventory retry attempt={}", e.getNumberOfRetryAttempts()));
        bulkhead.getEventPublisher().onCallRejected(e -> log.warn("inventory bulkhead rejected"));
    }

    static boolean isTransient(Throwable ex) {
        // TODO P02.2: unwrap cause; retry transport and selected 502/503/504 only.
        return false;
    }

    public <T> T execute(Supplier<T> request) {
        Supplier<T> call = () -> {
            attempts.incrementAndGet();
            return request.get();
        };
        // TODO P02.2–4: conditionally decorate call with Retry, CircuitBreaker and Bulkhead.
        // Choose order and explain which unit the circuit counts and which permit bulkhead holds.
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
