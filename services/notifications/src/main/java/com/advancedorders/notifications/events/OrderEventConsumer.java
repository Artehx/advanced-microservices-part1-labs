package com.advancedorders.notifications.events;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@ConditionalOnProperty(name="training.events.enabled", havingValue="true")
public class OrderEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);
    private final ObjectMapper json;
    private final Map<String, AtomicInteger> received = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> effects = new ConcurrentHashMap<>();

    public OrderEventConsumer(ObjectMapper json) { this.json = json; }
    public record OrderCreated(String eventId, String orderId, String sku, int quantity) {}

    @KafkaListener(topics="orders.created.v1", groupId="notifications-p03")
    public void consume(ConsumerRecord<String, String> record) {
        OrderCreated event = json.readValue(record.value(), OrderCreated.class);
        int deliveries = received.computeIfAbsent(event.eventId(), k -> new AtomicInteger()).incrementAndGet();
        log.info("eventId={} key={} partition={} offset={} deliveries={}", event.eventId(), record.key(),
                record.partition(), record.offset(), deliveries);
        // Idempotent simulated effect within this process; not durable deduplication.
        effects.putIfAbsent(event.eventId(), new AtomicInteger(1));
    }

    public Map<String, Object> snapshot(String eventId) {
        return Map.of("eventId", eventId, "received", count(received, eventId),
                "effects", count(effects, eventId));
    }
    private int count(Map<String, AtomicInteger> map, String key) {
        AtomicInteger value = map.get(key);
        return value == null ? 0 : value.get();
    }
}
