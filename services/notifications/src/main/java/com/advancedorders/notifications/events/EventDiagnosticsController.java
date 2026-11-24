package com.advancedorders.notifications.events;

import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications/training/events")
@ConditionalOnProperty(name="training.events.enabled", havingValue="true")
public class EventDiagnosticsController {
    private final OrderEventConsumer consumer;
    public EventDiagnosticsController(OrderEventConsumer consumer) { this.consumer = consumer; }
    @GetMapping("/{eventId}") public Map<String, Object> stats(@PathVariable String eventId) {
        return consumer.snapshot(eventId);
    }
}
