package com.bff.bff.service.impl;

import com.bff.bff.dto.*;
import com.bff.bff.service.interfaces.BffService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class BffServiceImpl implements BffService {
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

    public Mono<ResponseEntity<DashboardResponseDto>> dashboard(String userId) {
        // Step 1: Get user profile
        Mono<UserProfileDto> userProfileMono = userServiceClient.get()
                .uri("/user/{userId}/profile", userId)
                .retrieve()
                .bodyToMono(UserProfileDto.class);

        // Step 2: Get user accounts
        Mono<List<AccountDto>> accountsMono = accountServiceClient.get()
                .uri("/users/{userId}/accounts", userId)
                .retrieve()
                .bodyToFlux(AccountDto.class)
                .collectList()
                .flatMap(accounts -> {
                    // Step 3: For each account, get transactions asynchronously
                    List<CompletableFuture<AccountDto>> futures = accounts.stream()
                            .map(account -> getTransactionsForAccount(account).toFuture())
                            .collect(Collectors.toList());

                    CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                            futures.toArray(new CompletableFuture[0]));

                    return Mono.fromFuture(allFutures.thenApply(v ->
                            futures.stream()
                                    .map(CompletableFuture::join)
                                    .collect(Collectors.toList())));
                });

        // Combine results
        return Mono.zip(userProfileMono, accountsMono)
                .map(tuple -> {
                    UserProfileDto userProfile = tuple.getT1();
                    List<AccountDto> accounts = tuple.getT2();

                    DashboardResponseDto response = new DashboardResponseDto();
                    response.setUserId(userId);
                    response.setUsername(userProfile.getUsername());
                    response.setEmail(userProfile.getEmail());
                    response.setFirstName(userProfile.getFirstName());
                    response.setLastName(userProfile.getLastName());
                    response.setAccounts(accounts);

                    return ResponseEntity.ok(response);
                });
    }

    public Mono<AccountDto> getTransactionsForAccount(AccountDto account) {
        System.out.println(account.getAccountID());
        return transactionServiceClient.get()
                .uri("/accounts/{accountId}/transactions", account.getAccountID())
                .retrieve()
                .bodyToFlux(TransactionDto.class)
                .collectList()
                .map(transactions -> {
                    account.setTransactions(transactions);
                    return account;
                });
    }
}