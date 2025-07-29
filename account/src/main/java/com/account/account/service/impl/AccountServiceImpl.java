package com.account.account.service.impl;

import com.account.account.dto.*;
import com.account.account.entity.Account;
import com.account.account.enums.AccountStatus;
import com.account.account.exception.*;
import com.account.account.repository.AccountRepository;
import com.account.account.service.interfaces.AccountService;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {
    @Autowired
    private KafkaLogger kafkaLogger;
    
    private final AccountRepository accountRepository;
    private final WebClient.Builder webClientBuilder;
    @Value("${user_service}")
    private String userServiceUrl;
    public AccountServiceImpl(AccountRepository accountRepository, WebClient.Builder webClientBuilder){
        this.accountRepository = accountRepository;
        this.webClientBuilder = webClientBuilder;
    }
    // PUT /accounts/transfer: Update account Balance.
    @Override
    @Transactional(rollbackOn = {AccountNotFoundException.class,
            InsufficientFundsException.class,
            TransferFailedException.class})
    public Map<String, String> updateBalance(TransferRequest transferRequest) {
        // Log request
        kafkaLogger.log(transferRequest, "Request");

        // Validate accounts exist
        Account fromAccount = accountRepository.findById(transferRequest.getFromAccountId())
                .orElseThrow(() -> new AccountNotFoundException("From account not found"));

        Account toAccount = accountRepository.findById(transferRequest.getToAccountId())
                .orElseThrow(() -> new AccountNotFoundException("To account not found"));

        // Validate sufficient funds
        if (fromAccount.getBalance().compareTo(transferRequest.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds in account " + fromAccount.getId());
        }

        // Update balances
        fromAccount.setBalance(fromAccount.getBalance().subtract(transferRequest.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(transferRequest.getAmount()));

        // Save changes
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // Prepare response
        Map<String, String> response = new HashMap<>();
        response.put("message", "Transfer completed successfully");
        kafkaLogger.log(response, "Response");

        return response;
    }
    // Get /accounts/{accountID}
    @Override
    public AccountResponse getAccount(String id){
        kafkaLogger.log(id, "Request");
        Account account = accountRepository.findById(id).orElseThrow(() -> new AccountNotFoundException("From account not found"));
       
        AccountResponse profileResponse = new AccountResponse(
                    account.getId(),
                    account.getAccountNumber(),
                    account.getAccountType(),
                    account.getBalance(),
                    account.getStatus()
            );
            kafkaLogger.log(profileResponse, "Response");
            return profileResponse;
    }
    // POST /accounts: Creates a new bank account for a specified user.
    @Override
    @Transactional
    public CreationResponse createAccount(CreationRequest creationRequest) {
        // Validate request first
        kafkaLogger.log(creationRequest, "Request");

        // Try to get user profile with proper error handling
        try {
            ProfileResponseDto user = webClientBuilder.build()
                    .get()
                    .uri(userServiceUrl+"/users/" + creationRequest.getUserId() + "/profile")
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .flatMap(body -> {
                                        if (response.statusCode() == HttpStatus.NOT_FOUND) {
                                            return Mono.error(new UserNotFoundException("User not found with ID: " + creationRequest.getUserId()));
                                        }
                                        return Mono.error(new ServiceUnavailableException("User service error: " + body));
                                    }))
                    .bodyToMono(ProfileResponseDto.class)
                    .block();

        } catch (Exception e) {
            throw new ServiceUnavailableException("Failed to communicate with user service: " + e.getMessage());
        }
        Account account = buildAccount(creationRequest);
        Account savedAccount = accountRepository.save(account);

        CreationResponse response = new CreationResponse(
                savedAccount.getId(),
                savedAccount.getAccountNumber(),
                "Account created successfully"
        );
        kafkaLogger.log(response, "Response");
        return response;
    }
  
    private Account buildAccount(CreationRequest request) {
        Account account = new Account();
        account.setId(UUID.randomUUID().toString()); // Generate a unique ID
        account.setAccountType(request.getAccountType());
        account.setStatus(AccountStatus.ACTIVE);
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
    public List<AccountResponse> getUserAccounts(String userId){
        kafkaLogger.log(userId, "Request");
        try {
            ProfileResponseDto user = webClientBuilder.build()
                    .get()
                    .uri(userServiceUrl+"/users/" + userId + "/profile")
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .flatMap(body -> {
                                        if (response.statusCode() == HttpStatus.NOT_FOUND) {
                                            return Mono.error(new UserNotFoundException("User not found with ID: " + userId));
                                        }
                                        return Mono.error(new ServiceUnavailableException("User service error: " + body));
                                    }))
                    .bodyToMono(ProfileResponseDto.class)
                    .block();
        } catch (Exception e) {
            throw new ServiceUnavailableException("Failed to communicate with user service: " + e.getMessage());
        }
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

        kafkaLogger.log(response, "Response");

        return response;
    }


}
