package com.bff.bff.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Value("${user_service}")
    private String userServiceUrl;
    @Value("${account_service}")
    private String accountServiceUrl;
    @Value("${transaction_service}")
    private String transactionServiceUrl;
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    // Service-specific clients
    @Bean
    public WebClient accountServiceClient(WebClient.Builder builder) {
        return builder.baseUrl(accountServiceUrl).build();
    }

    @Bean
    public WebClient userServiceClient(WebClient.Builder builder) {
        return builder.baseUrl(userServiceUrl).build();
    }

    @Bean
    public WebClient transactionServiceClient(WebClient.Builder builder) {
        return builder.baseUrl(transactionServiceUrl).build();
    }
}