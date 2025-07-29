package com.transaction.transaction.service.impl;

import com.transaction.transaction.dto.*;
import com.transaction.transaction.enums.Status;
import com.transaction.transaction.exception.AccountNotFoundException;
import com.transaction.transaction.exception.ServiceUnavailableException;
import com.transaction.transaction.exception.TransactionNotFoundException;
import com.transaction.transaction.exception.TransactionExecutionException;
import com.transaction.transaction.exception.InsufficientBalanceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.transaction.transaction.entity.Transactions;
import com.transaction.transaction.repository.TransactionRepository;
import com.transaction.transaction.service.interfaces.TransactionService;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {
    @Autowired
    private KafkaLogger kafkaLogger;
    
    private final WebClient.Builder webClientBuilder;
    private final TransactionRepository transactionRepository;
    @Value("${account_service}")
    private String accountServiceUrl;

    @Autowired
    public TransactionServiceImpl(WebClient.Builder webClientBuilder, TransactionRepository transactionRepository) {
        this.webClientBuilder = webClientBuilder;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public InitiateResponseDto initiateTransaction(InitiateRequestDto initiateRequestDto) {
        kafkaLogger.log(initiateRequestDto, "Request");

        AccountDto fromAccount;
        AccountDto toAccount;

        try {
            fromAccount = webClientBuilder.build()
                .get()
                .uri(accountServiceUrl+"/accounts/" + initiateRequestDto.getFromAccountId())
                .retrieve()
                .bodyToMono(AccountDto.class)
                .block();

            toAccount = webClientBuilder.build()
                .get()
                .uri(accountServiceUrl+"/accounts/" + initiateRequestDto.getToAccountId())
                .retrieve()
                .bodyToMono(AccountDto.class)
                .block();

        } catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {
            throw new ServiceUnavailableException("Failed to retrieve account information: " + e.getMessage());
        } catch (Exception e) {
            throw new ServiceUnavailableException("Failed to communicate with account service: " + e.getMessage());
        }

        if (fromAccount == null || toAccount == null) {
            throw new AccountNotFoundException("Invalid account details provided");
        }
        
        if(fromAccount.getBalance().compareTo(initiateRequestDto.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance in the from account");
        }

        Transactions transaction = new Transactions();
        transaction.setId(java.util.UUID.randomUUID().toString());
        transaction.setFromAccountId(initiateRequestDto.getFromAccountId());
        transaction.setToAccountId(initiateRequestDto.getToAccountId());
        transaction.setAmount(initiateRequestDto.getAmount());
        transaction.setDescription(initiateRequestDto.getDescription());
        transaction.setStatus(Status.INITIATED);
        transaction.setTimestamp(java.time.LocalDateTime.now()); 
        transactionRepository.save(transaction);

        InitiateResponseDto initiateResponseDto = new InitiateResponseDto();
        initiateResponseDto.setTransactionId(transaction.getId());
        initiateResponseDto.setStatus(transaction.getStatus().name());
        initiateResponseDto.setTimestamp(transaction.getTimestamp().toString());

        kafkaLogger.log(initiateResponseDto, "Response");
        return initiateResponseDto;
    }

    @Override
    public ExecuteResponseDto executeTransaction(ExecuteRequestDto executeRequestDto) {
        kafkaLogger.log(executeRequestDto, "Request");
        
        Transactions transaction = transactionRepository.findById(executeRequestDto.getTransactionId())
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));

        if (transaction.getStatus() == Status.SUCCESS) {
            throw new TransactionExecutionException("Transaction is not in a state that can be executed");
        }
        
        try {
            webClientBuilder.build()
                .put()
                .uri(accountServiceUrl+"/accounts/transfer")
                .bodyValue(new TransferRequestDto(
                    transaction.getFromAccountId(),
                    transaction.getToAccountId(),
                    transaction.getAmount().doubleValue()
                ))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
        } catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {
            throw new TransactionExecutionException("Failed to execute transaction: " + e.getMessage());
        } catch (Exception e) {
            throw new ServiceUnavailableException("Failed to communicate with account service: " + e.getMessage());
        }

        transaction.setStatus(Status.SUCCESS);
        transactionRepository.save(transaction);

        ExecuteResponseDto responseDto = new ExecuteResponseDto();
        responseDto.setTransactionId(transaction.getId());
        responseDto.setStatus(transaction.getStatus().name());
        responseDto.setTimestamp(transaction.getTimestamp().toString());

        kafkaLogger.log(responseDto, "Response");
        return responseDto;
    }

    @Override
    public List<TransactionResponseWithType> getAccountTransactions(String accountId) {
        kafkaLogger.log(accountId, "Request");

        try {
            // 1. Verify account exists
            webClientBuilder.build()
                    .get()
                    .uri(accountServiceUrl+"/accounts/" + accountId)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .flatMap(body -> {
                                        if (response.statusCode() == HttpStatus.NOT_FOUND) {
                                            return Mono.error(new AccountNotFoundException("Account not found with ID: " + accountId));
                                        }
                                        return Mono.error(new ServiceUnavailableException("Account service error: " + body));
                                    }))
                    .bodyToMono(Void.class)
                    .block();

        } catch (RuntimeException e) {
            throw e; // Re-throw as is if it's already one of our custom exceptions
        }
        
        // 2. Get transactions for account (both incoming and outgoing)
        List<Transactions> outgoingTransactions = transactionRepository.findOutgoingTransactions(accountId);
        List<Transactions> incomingTransactions = transactionRepository.findIncomingTransactions(accountId);

        // 3. Process and combine transactions
        List<TransactionResponseWithType> response = new ArrayList<>();

        // Outgoing transactions (negative amounts)
        outgoingTransactions.stream()
                .map(transaction -> new TransactionResponseWithType(
                        transaction.getId(),
                        transaction.getToAccountId(),
                        transaction.getAmount(),
                        transaction.getDescription(),
                        transaction.getTimestamp().toString(),
                        "Sent"
                ))
                .forEach(response::add);

        // Incoming transactions (positive amounts)
        incomingTransactions.stream()
                .map(transaction -> new TransactionResponseWithType(
                        transaction.getId(),
                        transaction.getFromAccountId(),
                        transaction.getAmount(),
                        transaction.getDescription(),
                        transaction.getTimestamp().toString(),
                        "Delivered"
                ))
                .forEach(response::add);

        response.sort(Comparator.comparing(TransactionResponseWithType::getTimestamp).reversed());

        kafkaLogger.log(response, "Response");
        return response;
    }
}

