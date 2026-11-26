package com.advancedorders.shipping.rpc;

import com.advancedorders.shipping.domain.ShippingPricing;
import io.grpc.*;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name="training.grpc.enabled",havingValue="true")
public class ShippingRpcServer {
    private final int port,delayMs;
    private Server server;
    public ShippingRpcServer(@Value("${training.grpc.port:9090}") int port,
        @Value("${training.grpc.delay-ms:0}") int delayMs) {
        this.port=port;this.delayMs=Math.max(0,Math.min(3000,delayMs));
    }
    @PostConstruct void start() throws IOException {
        server=NettyServerBuilder.forPort(port).addService(new ShippingQuotesGrpc.ShippingQuotesImplBase(){
            @Override public void quote(QuoteRequest request,StreamObserver<QuoteReply> observer){
                // TODO P05: validate/calculate with ShippingPricing, construct reply, onNext/onCompleted.
                // Honor simulated delay and cancellation; translate invalid argument.
                observer.onError(Status.UNIMPLEMENTED.withDescription("P05: implement Quote").asRuntimeException());
            }
        }).build().start();
    }
    @PreDestroy void stop() {
        if(server!=null){server.shutdownNow();try{server.awaitTermination(3,TimeUnit.SECONDS);}catch(InterruptedException e){Thread.currentThread().interrupt();}}
    }
}
