package com.bff.bff.service.impl;

import com.bff.bff.dto.*;
import com.bff.bff.service.interfaces.BffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.Collections;
import java.util.List;

@Service
public class BffServiceImpl implements BffService {
    @Autowired
    private KafkaLogger kafkaLogger;
    private final WebClient accountServiceClient;
    private final WebClient userServiceClient;
    private final WebClient transactionServiceClient;

    public BffServiceImpl(
            @Qualifier("accountServiceClient") WebClient accountServiceClient,
            @Qualifier("userServiceClient") WebClient userServiceClient,
            @Qualifier("transactionServiceClient") WebClient transactionServiceClient) {
        this.accountServiceClient = accountServiceClient;
        this.userServiceClient = userServiceClient;
        this.transactionServiceClient = transactionServiceClient;
    }
    public ResponseEntity<?> dashboard(String userId) {
        kafkaLogger.log(userId, "Request");
        try {
            // Step 1: Get user profile (blocking call)
            UserProfileDto userProfile = userServiceClient.get()
                    .uri("/user/{userId}/profile", userId)
                    .retrieve()
                    .onStatus(status -> status == HttpStatus.NOT_FOUND, response -> {

                        return response.bodyToMono(String.class)
                                .then(Mono.error(new RuntimeException("User not found with ID: " + userId)));
                    })
                    .bodyToMono(UserProfileDto.class)
                    .block();
            // Step 2: Get user accounts (blocking call)
            List<AccountDto> accounts = accountServiceClient.get()
                    .uri("/users/{userId}/accounts", userId)
                    .retrieve()
                    .onStatus(status -> status == HttpStatus.NOT_FOUND, response -> {
                        return response.bodyToMono(String.class)
                                .then(Mono.error(new RuntimeException("The user has no accounts.")));
                    })
                    .bodyToFlux(AccountDto.class)
                    .collectList()
                    .block();
            if (accounts != null) {
                // Step 3: For each account, get transactions (blocking calls)
                for (AccountDto account : accounts) {
                    List<TransactionDto> transactions = transactionServiceClient.get()
                            .uri("/accounts/{accountId}/transactions", account.getAccountID())
                            .retrieve()
                            .onStatus(
                                    status -> status == HttpStatus.NOT_FOUND,
                                    response -> Mono.empty() // Treat 404 as empty, not an error
                            )
                            .bodyToFlux(TransactionDto.class)
                            .collectList()
                            .block();
                    if (transactions.getFirst().getTransactionId() != null){
                        account.setTransactions(transactions);
                    } else {
                        account.setTransactions(Collections.emptyList());
                    }
                }
            }

            // Build response
            DashboardResponseDto response = new DashboardResponseDto();
            response.setUserId(userId);
            response.setUsername(userProfile.getUsername());
            response.setEmail(userProfile.getEmail());
            response.setFirstName(userProfile.getFirstName());
            response.setLastName(userProfile.getLastName());
            response.setAccounts(accounts);
            System.out.println(response);
            //kafkaLogger.log(response, "Response");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ErrorResponse errorResponse = new ErrorResponse(
                    404,
                    "Not Found",
                    e.getMessage()
            );
            kafkaLogger.log(errorResponse, "Response");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}