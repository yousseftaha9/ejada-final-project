package com.account.account.service.impl;

import com.account.account.dto.*;
import com.account.account.entity.Account;
import com.account.account.repository.AccountRepository;
import com.account.account.service.interfaces.AccountService;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {
    @Autowired
    private KafkaLogger kafkaLogger;
    
    private final AccountRepository accountRepository;
    private final WebClient.Builder webClientBuilder;

    public AccountServiceImpl(AccountRepository accountRepository, WebClient.Builder webClientBuilder){
        this.accountRepository = accountRepository;
        this.webClientBuilder = webClientBuilder;
    }
    // PUT /accounts/transfer: Update account Balance.
    @Override
    public ResponseEntity<?> updateBalance(TransferRequest transferRequest){
        try{
            kafkaLogger.log(transferRequest, "Request");
            Account fromAccount = accountRepository.findById(transferRequest.getFromAccountId()).orElse(null);
            Account toAccount = accountRepository.findById(transferRequest.getToAccountId()).orElse(null);
           if (fromAccount == null || toAccount == null) {
               ErrorResponse errorResponse = new ErrorResponse(
                       404,
                       "Not Found",
                       "Either one of the two account is not found"
               );
               kafkaLogger.log(errorResponse, "Response");
               return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
           }

           if(fromAccount.getBalance().compareTo(transferRequest.getAmount()) < 0){
               ErrorResponse errorResponse = new ErrorResponse(
                       400,
                       "Bad Request",
                       "Insufficient funds"
               );
                kafkaLogger.log(errorResponse, "Response");
               return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
           }

            fromAccount.setStatus(Account.AccountStatus.ACTIVE);
            toAccount.setBalance(toAccount.getBalance().add(transferRequest.getAmount()));
            fromAccount.setBalance((fromAccount.getBalance().subtract(transferRequest.getAmount())));
            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Transfer done successfully");
            kafkaLogger.log(response, "Response");
            return ResponseEntity.ok(response);
        } catch (Exception e){
            ErrorResponse errorResponse = new ErrorResponse(
                    500,
                    "Internal Server Error",
                    "Account retrieval failed: " + e.getMessage()
            );
            kafkaLogger.log(errorResponse, "Response");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    // Get /accounts/{accountID}
    @Override
    public ResponseEntity<?> getAccount(String id) {
        try {
            kafkaLogger.log(id, "Request");
            Account account = accountRepository.findById(id).orElse(null);
            if (account == null) {
                ErrorResponse errorResponse = new ErrorResponse(
                        404,
                        "Not Found",
                        "Account with ID " + id + " not Found"
                );
                kafkaLogger.log(errorResponse, "Response");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            AccountResponse profileResponse = new AccountResponse(
                    account.getId(),
                    account.getAccountNumber(),
                    account.getAccountType(),
                    account.getBalance(),
                    account.getStatus()
            );
            kafkaLogger.log(profileResponse, "Response");
            return ResponseEntity.ok(profileResponse);
        }
        catch (Exception e){
            ErrorResponse errorResponse = new ErrorResponse(
                    500,
                    "Internal Server Error",
                    "Account retrieval failed: " + e.getMessage()
            );
            kafkaLogger.log(errorResponse, "Response");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    // POST /accounts: Creates a new bank account for a specified user.
    @Override
    public ResponseEntity<?> createAccount(CreationRequest creationRequest) {
        // Validate request first
        kafkaLogger.log(creationRequest, "Request");
        if (creationRequest.getUserId() == null) {
            ErrorResponse errorResponse = new ErrorResponse(
                    400,
                    "Bad Request",
                    "User ID should be provided"
            );
            kafkaLogger.log(errorResponse, "Response");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        if (creationRequest.getInitialBalance().compareTo(BigDecimal.ZERO) < 0) {
            ErrorResponse errorResponse = new ErrorResponse(
                    400,
                    "Bad Request",
                    "Invalid account balance"
            );
            kafkaLogger.log(errorResponse, "Response");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            Account.AccountType.valueOf(creationRequest.getAccountType().toString());
        } catch (IllegalArgumentException e) {
            ErrorResponse errorResponse = new ErrorResponse(
                    400,
                    "Bad Request",
                    "Invalid account type"
            );
            kafkaLogger.log(errorResponse, "Response");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // Try to get user profile with proper error handling
        try {
            ProfileResponseDto user = webClientBuilder.build()
                    .get()
                    .uri("http://localhost:8081/users/" + creationRequest.getUserId() + "/profile")
                    .retrieve()
                    .onStatus(status -> status == HttpStatus.NOT_FOUND, response -> {
                        // Consume the response body but don't include it in the error
                        return response.bodyToMono(String.class)
                                .then(Mono.error(new RuntimeException("User not found with ID: " + creationRequest.getUserId())));
                    })
                    .bodyToMono(ProfileResponseDto.class)
                    .block();

            Account account = buildAccount(creationRequest);
            Account savedAccount = accountRepository.save(account);

            CreationResponse response = new CreationResponse(
                    savedAccount.getId(),
                    savedAccount.getAccountNumber(),
                    "Account created successfully"
            );
            kafkaLogger.log(response, "Response");

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
            ErrorResponse errorResponse = new ErrorResponse(
                    500,
                    "Internal Server Error",
                    "Error while creating account: " + e.getMessage()
            );
            kafkaLogger.log(errorResponse, "Response");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    private Account buildAccount(CreationRequest request) {
        Account account = new Account();
        account.setId(UUID.randomUUID().toString()); // Generate a unique ID
        account.setAccountType(request.getAccountType());
        account.setStatus(Account.AccountStatus.ACTIVE);
        account.setBalance(request.getInitialBalance());
        account.setUserId(request.getUserId());
        account.setAccountNumber(generateAccountNumber());
        return account;
    }
    private String generateAccountNumber() {
        // Implement proper account number generation
        return String.format("%010d", new Random().nextInt(1_000_000_000));
    }
    // GET /users/{userId}/accounts: Lists all accounts associated with a given user.
    @Override
    public ResponseEntity<?> getUserAccounts(String userId){
        kafkaLogger.log(userId, "Request");
        if (userId == null || userId.isBlank()) {
            ErrorResponse errorResponse = new ErrorResponse(
                    400,
                    "Bad Request",
                    "User ID must be provided"
            );
            kafkaLogger.log(errorResponse, "Response");
            return ResponseEntity.badRequest().body(errorResponse);

        }
        try {
            ProfileResponseDto user = webClientBuilder.build()
                    .get()
                    .uri("http://localhost:8081/users/" + userId + "/profile")
                    .retrieve()
                    .onStatus(status -> status == HttpStatus.NOT_FOUND, response -> {
                        // Consume the response body but don't include it in the error

                        return response.bodyToMono(String.class)
                                .then(Mono.error(new RuntimeException("User not found with ID: " + userId)));
                    })
                    .bodyToMono(ProfileResponseDto.class)
                    .block();

            List<Account> accounts = accountRepository.findByUserId(userId);
            List<AccountResponse> response = accounts.stream()
                    .map(account -> new AccountResponse(
                            account.getId(),
                            account.getAccountNumber(),
                            account.getAccountType(),
                            account.getBalance(),
                            account.getStatus()
                    ))
                    .collect(Collectors.toList());
             String summary = "Retrieved " + response.size() + " accounts for user " + userId;

            kafkaLogger.log(response, "Response");

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
            ErrorResponse errorResponse = new ErrorResponse(
                    500,
                    "Internal Server Error",
                    "Account Retrieval failed: " + e.getMessage()
            );
            kafkaLogger.log(errorResponse, "Response");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @Scheduled(fixedRate = 3600000)
    @Async
    @Transactional
    public void refreshPricingParameters() {
        List<Account> accounts = accountRepository.findByStatus(Account.AccountStatus.ACTIVE);
        accounts.forEach(account -> {
            webClientBuilder.build()
                    .get()
                    .uri("http://localhost:8082/accounts/{accountId}/transactions", account.getId())
                    .retrieve()
                    .onStatus(
                            status -> status == HttpStatus.NOT_FOUND,
                            response -> {
                                deactivateAccountWithNoTransactions(account);
                                return Mono.empty();
                            }
                    )
                    .onStatus(
                            HttpStatusCode::isError,  // Fixed: Using lambda instead of method reference
                            response -> Mono.empty()
                    )
                    .bodyToFlux(TransactionDto.class)
                    .collectList()
                    .timeout(Duration.ofSeconds(30))
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                    .onErrorResume(e -> Mono.empty())
                    .subscribe(transactions -> {
                        if (transactions != null && !transactions.isEmpty()) {
                            List<TransactionDto> deliveredTransactions = transactions.stream()
                                    .filter(t -> "Delivered".equals(t.getType()))
                                    .collect(Collectors.toList());

                            if (!deliveredTransactions.isEmpty()) {
                                checkLastDeliveredTransaction(account, deliveredTransactions);
                            } else {
                                deactivateAccountWithNoTransactions(account);
                            }
                        }
                    });
        });
    }

    private void checkLastDeliveredTransaction(Account account, List<TransactionDto> transactions) {
        TransactionDto lastTransaction = transactions.stream()
                .max(Comparator.comparing(TransactionDto::getTimestamp))
                .orElseThrow();

        Instant twentyFourHoursAgo = Instant.now().minus(24, ChronoUnit.HOURS);

        if (lastTransaction.getTimestamp().before(Timestamp.from(twentyFourHoursAgo))) {
            account.setStatus(Account.AccountStatus.INACTIVE);
            accountRepository.save(account);
        }
    }

    private void deactivateAccountWithNoTransactions(Account account) {
        account.setStatus(Account.AccountStatus.INACTIVE);
        accountRepository.save(account);
    }
}
