package com.bff.bff.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    // Service-specific clients
    @Bean
    public WebClient accountServiceClient(WebClient.Builder builder) {
        return builder.baseUrl("http://localhost:8083").build();
    }

    @Bean
    public WebClient userServiceClient(WebClient.Builder builder) {
        return builder.baseUrl("http://localhost:8081").build();
    }

    @Bean
    public WebClient transactionServiceClient(WebClient.Builder builder) {
        return builder.baseUrl("http://localhost:8082").build();
    }
}