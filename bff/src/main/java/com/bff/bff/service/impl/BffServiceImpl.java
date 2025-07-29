package com.bff.bff.service.impl;

import com.bff.bff.dto.*;
import com.bff.bff.exception.AccountNotFoundException;
import com.bff.bff.exception.ServiceUnavailableException;
import com.bff.bff.exception.UserNotFoundException;
import com.bff.bff.service.interfaces.BffService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
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
//    public ResponseEntity<?> dashboard(String userId) {
//        // Print incoming headers
//        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
//        // Print and use the app-name header
//        String appName = request.getHeader("app-name");
//        System.out.println("===== Detected Application =====");
//        System.out.println("app-name: " + appName); // Will print "MOBILE" from your logs
//        System.out.println("===============================");
//
//        kafkaLogger.log(userId, "Request");
//        try {
//            // Step 1: Get user profile (blocking call)
//            UserProfileDto userProfile = userServiceClient.get()
//                    .uri("/users/{userId}/profile", userId)
//                    .retrieve()
//                    .onStatus(status -> status == HttpStatus.NOT_FOUND, response -> {
//
//                        return response.bodyToMono(String.class)
//                                .then(Mono.error(new RuntimeException("User not found with ID: " + userId)));
//                    })
//                    .bodyToMono(UserProfileDto.class)
//                    .block();
//            // Step 2: Get user accounts (blocking call)
//            List<AccountDto> accounts = accountServiceClient.get()
//                    .uri("/users/{userId}/accounts", userId)
//                    .retrieve()
//                    .onStatus(status -> status == HttpStatus.NOT_FOUND, response -> {
//                        return response.bodyToMono(String.class)
//                                .then(Mono.error(new RuntimeException("The user has no accounts.")));
//                    })
//                    .bodyToFlux(AccountDto.class)
//                    .collectList()
//                    .block();
//            if (accounts != null) {
//                // Step 3: For each account, get transactions (blocking calls)
//                for (AccountDto account : accounts) {
//                    List<TransactionDto> transactions = transactionServiceClient.get()
//                            .uri("/accounts/{accountId}/transactions", account.getAccountID())
//                            .retrieve()
//                            .onStatus(
//                                    status -> status == HttpStatus.NOT_FOUND,
//                                    response -> Mono.empty() // Treat 404 as empty, not an error
//                            )
//                            .bodyToFlux(TransactionDto.class)
//                            .collectList()
//                            .block();
//                    if (transactions.getFirst().getTransactionId() != null){
//                        account.setTransactions(transactions);
//                    } else {
//                        account.setTransactions(Collections.emptyList());
//                    }
//                }
//            }
//
//            // Build response
//            DashboardResponseDto response = new DashboardResponseDto();
//            response.setUserId(userId);
//            response.setUsername(userProfile.getUsername());
//            response.setEmail(userProfile.getEmail());
//            response.setFirstName(userProfile.getFirstName());
//            response.setLastName(userProfile.getLastName());
//            response.setAccounts(accounts);
//            // System.out.println(response);
//            // kafkaLogger.log(response, "Response");
//
//            return ResponseEntity.ok(response);
//        } catch (RuntimeException e) {
//            ErrorResponse errorResponse = new ErrorResponse(
//                    404,
//                    "Not Found",
//                    e.getMessage()
//            );
//            kafkaLogger.log(errorResponse, "Response");
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
    public DashboardResponseDto dashboard(String userId) {
        // Print incoming headers
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String appName = request.getHeader("app-name");
        System.out.println("===== Detected Application =====");
        System.out.println("app-name: " + appName);
        System.out.println("===============================");

        kafkaLogger.log(userId, "Request");

        try {
            // Step 1: Get user profile (blocking call)
            UserProfileDto userProfile = userServiceClient.get()
                    .uri("/users/{userId}/profile", userId)
                    .retrieve()
                    .onStatus(
                            status -> status == HttpStatus.NOT_FOUND,
                            response -> Mono.error(new UserNotFoundException("User not found with ID: " + userId)))
                    .onStatus(
                            HttpStatusCode::is5xxServerError,  // Changed to instance method call
                            response -> Mono.error(new ServiceUnavailableException("User service is unavailable")))
                    .bodyToMono(UserProfileDto.class)
                    .block();

            // Step 2: Get user accounts (blocking call)
            List<AccountDto> accounts = accountServiceClient.get()
                    .uri("/users/{userId}/accounts", userId)
                    .retrieve()
                    .onStatus(
                            status -> status == HttpStatus.NOT_FOUND,
                            response -> Mono.error(new AccountNotFoundException("The user has no accounts.")))
                    .onStatus(
                            HttpStatusCode::is5xxServerError,
                            response -> Mono.error(new ServiceUnavailableException("Account service is unavailable")))
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
                                    HttpStatusCode::is5xxServerError,
                                    response -> Mono.error(new ServiceUnavailableException("Transaction service is unavailable")))
                            .bodyToFlux(TransactionDto.class)
                            .collectList()
                            .block();

                    account.setTransactions(transactions != null && !transactions.isEmpty() ?
                            transactions : Collections.emptyList());
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
            kafkaLogger.log(response, "Response");

            return response;
        } catch (org.springframework.web.reactive.function.client.WebClientRequestException e) {
            throw new ServiceUnavailableException("Failed to connect with service : " + e.getMessage());
        } catch (RuntimeException e) {
            throw e; // Let the global exception handler handle it
        }
    }
}