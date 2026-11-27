package com.advancedorders.notifications.webhook;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/notifications/training/webhooks")
@ConditionalOnProperty(name="training.webhook.enabled",havingValue="true")
public class WebhookController {
    private final ObjectMapper json;
    private final String secret;
    private final Map<String,AtomicInteger> received=new ConcurrentHashMap<>();
    private final Map<String,AtomicInteger> effects=new ConcurrentHashMap<>();
    public WebhookController(ObjectMapper json,@Value("${training.webhook.secret:}") String secret){
        this.json=json;this.secret=secret;
    }
    public record Event(String eventId,String type,String orderId){}
    @PostMapping("/delivery")
    public Map<String,Object> delivery(@RequestBody byte[] raw,
        @RequestHeader(name="X-Lab-Signature",required=false) String signature){
        if(raw.length>8192)throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,"Training payload limit");
        if(!secret.isBlank() && !validSignature(raw,signature))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid signature");
        Event e;
        try{e=json.readValue(raw,Event.class);}catch(Exception ex){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid JSON");}
        if(e==null || e.eventId()==null || !e.eventId().matches("[A-Za-z0-9-]{1,64}") ||
            !"shipping.delivered".equals(e.type()) || e.orderId()==null || e.orderId().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid event");
        received.computeIfAbsent(e.eventId(),k->new AtomicInteger()).incrementAndGet();
        effects.putIfAbsent(e.eventId(),new AtomicInteger(1));
        return stats(e.eventId());
    }
    @GetMapping("/{id}")
    public Map<String,Object> stats(@PathVariable String id){
        return Map.of("eventId",id,"received",count(received,id),"effects",count(effects,id));
    }
    private int count(Map<String,AtomicInteger> m,String id){var n=m.get(id);return n==null?0:n.get();}
    private boolean validSignature(byte[] raw,String signature){
        if(signature==null || !signature.matches("[0-9a-fA-F]{64}"))return false;
        try{
            Mac mac=Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256"));
            return MessageDigest.isEqual(mac.doFinal(raw),HexFormat.of().parseHex(signature));
        }catch(Exception e){throw new IllegalStateException("Signature verification failed",e);}
    }
}
