package com.advancedorders.orders.rpc;

import com.advancedorders.shipping.rpc.*;
import io.grpc.*;
import jakarta.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders/training/grpc")
@ConditionalOnProperty(name="training.grpc.enabled",havingValue="true")
public class ShippingRpcController {
    private final ManagedChannel channel;
    private final ShippingQuotesGrpc.ShippingQuotesBlockingStub stub;
    public ShippingRpcController(@Value("${training.grpc.host:shipping}") String host,
        @Value("${training.grpc.port:9090}") int port) {
        channel=ManagedChannelBuilder.forAddress(host,port).usePlaintext().build();
        stub=ShippingQuotesGrpc.newBlockingStub(channel);
    }
    @GetMapping("/quote")
    public ResponseEntity<Map<String,Object>> quote(@RequestParam(defaultValue="ES") String destination,
        @RequestParam(defaultValue="500") int weightGrams,
        @RequestParam(defaultValue="1500") long deadlineMs){
        if(deadlineMs<50 || deadlineMs>5000)
            return ResponseEntity.badRequest().body(Map.of("grpcCode","INVALID_ARGUMENT","message","deadlineMs must be 50..5000"));
        // TODO P05: build QuoteRequest, call stub with deadline, map reply and gRPC errors to HTTP.
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(Map.of("grpcCode","UNIMPLEMENTED","message","P05: implement RPC client"));
    }
    @PreDestroy void stop(){channel.shutdownNow();}
}
