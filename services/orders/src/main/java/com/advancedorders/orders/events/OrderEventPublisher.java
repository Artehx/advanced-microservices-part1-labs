package com.advancedorders.orders.events;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/orders/training/events")
@ConditionalOnProperty(name="training.events.enabled", havingValue="true")
public class OrderEventPublisher {
    public static final String TOPIC = "orders.created.v1";
    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper json;

    public OrderEventPublisher(KafkaTemplate<String, String> kafka, ObjectMapper json) {
        this.kafka = kafka;
        this.json = json;
    }

    public record OrderCreated(String eventId, String orderId, String sku, int quantity) {}

    // Training-only event publication. This does not persist or create a real order.
    @PostMapping
    public ResponseEntity<Map<String, Object>> publish(@RequestBody OrderCreated event) {
        if (event.eventId() == null || event.eventId().isBlank() ||
            event.orderId() == null || event.orderId().isBlank() ||
            event.sku() == null || event.sku().isBlank() || event.quantity() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid event");
        }
        // TODO P03: serialize event, send to TOPIC with orderId as key, await broker ACK.
        // Return 202 with eventId, topic, partition, offset, status=BROKER_ACKNOWLEDGED.
        // Handle interruption and uncertain publication outcome without claiming no record was written.
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "P03: implement publication");
    }
}
