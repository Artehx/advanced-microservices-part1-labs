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
        try {
            var req=QuoteRequest.newBuilder().setDestination(destination).setWeightGrams(weightGrams).build();
            // TODO P05: change the deadline policy and compare with the server delay.
            var q=stub.withDeadlineAfter(deadlineMs,TimeUnit.MILLISECONDS).quote(req);
            return ResponseEntity.ok(Map.of("destination",q.getDestination(),"weightGrams",q.getWeightGrams(),
                "priceCents",q.getPriceCents(),"currency",q.getCurrency(),"estimatedDays",q.getEstimatedDays(),"provider",q.getProvider(),"grpcCode","OK"));
        } catch(StatusRuntimeException e){
            var code=e.getStatus().getCode();
            var http=switch(code){
                case DEADLINE_EXCEEDED -> HttpStatus.GATEWAY_TIMEOUT;
                case INVALID_ARGUMENT -> HttpStatus.BAD_REQUEST;
                case UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
                default -> HttpStatus.BAD_GATEWAY;
            };
            return ResponseEntity.status(http).body(Map.of("grpcCode",code.name(),"message","RPC failed"));
        }
    }
    @PreDestroy void stop(){channel.shutdownNow();}
}
