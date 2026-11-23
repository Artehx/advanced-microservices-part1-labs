package com.advancedorders.orders.config;

import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(InventoryClientProperties.class)
public class HttpClientConfig {
    @Bean
    RestClient inventoryRestClient(RestClient.Builder builder, InventoryClientProperties properties,
            @Value("${training.resilience.connect-ms:1000}") long connectMs,
            @Value("${training.resilience.read-ms:5000}") long readMs) {
        // TODO P02.1: build JDK HttpClient with connect timeout, configure request factory
        // with read timeout and attach to RestClient.Builder.
        return builder.baseUrl(properties.baseUrl().toString()).build();
    }
}
