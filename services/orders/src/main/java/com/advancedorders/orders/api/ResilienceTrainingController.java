package com.advancedorders.orders.api;

import java.util.Map;
import com.advancedorders.orders.client.InventoryResilience;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders/training")
@ConditionalOnProperty(name="training.resilience.diagnostics", havingValue="true")
public class ResilienceTrainingController {
    private final InventoryResilience resilience;
    public ResilienceTrainingController(InventoryResilience resilience) { this.resilience = resilience; }
    @GetMapping("/resilience") public Map<String, Object> stats() { return resilience.snapshot(); }
    @GetMapping("/ping") public Map<String, String> ping() { return Map.of("status", "ok"); }
}
