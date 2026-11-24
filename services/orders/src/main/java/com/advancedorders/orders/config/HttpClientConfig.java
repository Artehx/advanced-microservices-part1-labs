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
        HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(connectMs)).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(http);
        factory.setReadTimeout(Duration.ofMillis(readMs));
        return builder.baseUrl(properties.baseUrl().toString()).requestFactory(factory).build();
    }
}
