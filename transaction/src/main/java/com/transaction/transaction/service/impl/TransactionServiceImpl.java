package com.transaction.transaction.service.impl;

import com.transaction.transaction.dto.*;
import com.transaction.transaction.enums.Status;
import com.transaction.transaction.exception.AccountNotFoundException;
import com.transaction.transaction.exception.NoTransactionsFoundException;
import com.transaction.transaction.exception.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> initiateTransaction(InitiateRequestDto initiateRequestDto) {
        kafkaLogger.log(initiateRequestDto, "Request");
        if (initiateRequestDto == null) {
            ErrorResponse errorResponse = new ErrorResponse(
                400, 
                "Bad Request", 
                "Initiate request cannot be null"
            );
            kafkaLogger.log(errorResponse, "Response");
            return ResponseEntity.badRequest().body(errorResponse);
        }

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
            ErrorResponse errorResponse = new ErrorResponse(
                400,
                "Bad Request",
                "Failed to retrieve account information: " + e.getMessage()
            );
            kafkaLogger.log(errorResponse, "Response");
            return ResponseEntity.status(e.getStatusCode()).body(errorResponse);
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(
                500,
                "Internal Server Error",
                "Failed to communicate with account service: " + e.getMessage()
            );
            kafkaLogger.log(errorResponse, "Response");
            return ResponseEntity.internalServerError().body(errorResponse);
        }

        if (fromAccount == null || toAccount == null) {
            ErrorResponse errorResponse = new ErrorResponse(
                400, 
                "Bad Request", 
                "Invalid account details provided"
            );
            kafkaLogger.log(errorResponse, "Response");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        if(fromAccount.getBalance().compareTo(initiateRequestDto.getAmount()) < 0) {
            ErrorResponse errorResponse = new ErrorResponse(
                400, 
                "Bad Request", 
                "Insufficient balance in the from account"
            );
            kafkaLogger.log(errorResponse, "Response");
            return ResponseEntity.badRequest().body(errorResponse);
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
        return ResponseEntity.ok(initiateResponseDto);
    }

    @Override
    public ResponseEntity<?> executeTransaction(ExecuteRequestDto executeRequestDto) {
        kafkaLogger.log(executeRequestDto, "Request");
        Transactions transaction = transactionRepository.findById(executeRequestDto.getTransactionId())
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (transaction.getStatus() != Status.INITIATED) {
            ErrorResponse errorResponse = new ErrorResponse(
                400, 
                "Bad Request", 
                "Transaction is not in a state that can be executed"
            );
            kafkaLogger.log(errorResponse, "Response");
            return ResponseEntity.badRequest().body(errorResponse);
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
            ErrorResponse errorResponse = new ErrorResponse(
                400,
                "Bad Request",
                "Failed to execute transaction: " + e.getMessage()
            );
            kafkaLogger.log(errorResponse, "Response");
            return ResponseEntity.status(e.getStatusCode()).body(errorResponse);
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(
                500,
                "Internal Server Error",
                "Failed to communicate with account service: " + e.getMessage()
            );
            kafkaLogger.log(errorResponse, "Response");
            return ResponseEntity.internalServerError().body(errorResponse);
        }

        transaction.setStatus(Status.SUCCESS);
        transactionRepository.save(transaction);

        ExecuteResponseDto responseDto = new ExecuteResponseDto();
        responseDto.setTransactionId(transaction.getId());
        responseDto.setStatus(transaction.getStatus().name());
        responseDto.setTimestamp(transaction.getTimestamp().toString());

        kafkaLogger.log(responseDto, "Response");
        return ResponseEntity.ok(responseDto);

    }

//    // First approach that is how to know the type from the amount positive or negative
//    @Override
//    public ResponseEntity<?> getAccountTransactions(String accountId) {
//        // Input validation
//        if (accountId == null || accountId.isBlank()) {
//            return ResponseEntity.badRequest().body(
//                    new ErrorResponse(400, "Bad Request", "Account ID must be provided")
//            );
//        }
//
//        try {
//            // 1. Verify account exists
//            webClientBuilder.build()
//                    .get()
//                    .uri("http://localhost:8083/accounts/" + accountId)
//                    .retrieve()
//                    .onStatus(status -> status == HttpStatus.NOT_FOUND, response -> {
//                        return Mono.error(new RuntimeException("Account not found with ID: " + accountId));
//                    })
//                    .bodyToMono(Void.class)
//                    .block();
//
//            // 2. Get transactions for account (both incoming and outgoing)
//            List<Transaction> outgoingTransactions = transactionRepository.findOutgoingTransactions(accountId);
//            List<Transaction> incomingTransactions = transactionRepository.findIncomingTransactions(accountId);
//
//            if (outgoingTransactions.isEmpty() && incomingTransactions.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
//                        new ErrorResponse(404, "Not Found",
//                                "No transactions found for account ID " + accountId)
//                );
//            }
//
//            // 3. Process and combine transactions
//            List<TransactionResponse> response = new ArrayList<>();
//
//            // Outgoing transactions (negative amounts)
//            outgoingTransactions.stream()
//                    .map(transaction -> new TransactionResponse(
//                            transaction.getId(),
//                            transaction.getToAccountId(),
//                            transaction.getAmount().negate(),
//                            transaction.getDescription(), // Make amount negative
//                            transaction.getTimestamp()
//                    ))
//                    .forEach(response::add);
//
//            // Incoming transactions (positive amounts)
//            incomingTransactions.stream()
//                    .map(transaction -> new TransactionResponse(
//                            transaction.getId(),
//                            transaction.getFromAccountId(),
//                            transaction.getAmount(), // Keep amount positive
//                            transaction.getDescription(),
//                            transaction.getTimestamp()
//                    ))
//                    .forEach(response::add);
//
//            // Sort by timestamp (most recent first)
//            response.sort(Comparator.comparing(TransactionResponse::getTimestamp).reversed());
//
//            return ResponseEntity.ok(response);
//
//        } catch (RuntimeException e) {
//            if (e.getMessage().contains("Account not found")) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
//                        new ErrorResponse(404, "Not Found", e.getMessage())
//                );
//            }
//            return ResponseEntity.internalServerError().body(
//                    new ErrorResponse(500, "Internal Server Error",
//                            "Failed to retrieve transactions: " + e.getMessage())
//            );
//        }
//    }

    // Second approach that is how to know the type from the type field
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

//        if (outgoingTransactions.isEmpty() && incomingTransactions.isEmpty()) {
//            throw new NoTransactionsFoundException("No transactions found for account ID " + accountId);
//        }
//      if needed I will add it
        // 3. Process and combine transactions
        List<TransactionResponseWithType> response = new ArrayList<>();

        // Outgoing transactions (negative amounts)
        outgoingTransactions.stream()
                .map(transaction -> new TransactionResponseWithType(
                        transaction.getId(),
                        transaction.getToAccountId(),
                        transaction.getAmount(),
                        transaction.getDescription(), // Make amount negative
                        transaction.getTimestamp().toString(),
                        "Sent"
                ))
                .forEach(response::add);

        // Incoming transactions (positive amounts)
        incomingTransactions.stream()
                .map(transaction -> new TransactionResponseWithType(
                        transaction.getId(),
                        transaction.getFromAccountId(),
                        transaction.getAmount(), // Keep amount positive
                        transaction.getDescription(),
                        transaction.getTimestamp().toString(),
                        "Delivered" //Received
                ))
                .forEach(response::add);

        response.sort(Comparator.comparing(TransactionResponseWithType::getTimestamp).reversed());

        kafkaLogger.log(response, "Response");

        return response;
    }


//    // Third approach that is we will send the two from and to
//    @Override
//    public ResponseEntity<?> getAccountTransactions(String accountId) {
//        // Input validation
//        if (accountId == null || accountId.isBlank()) {
//            return ResponseEntity.badRequest().body(
//                    new ErrorResponse(400, "Bad Request", "Account ID must be provided")
//            );
//        }
//
//        try {
//            // 1. Verify account exists
//            webClientBuilder.build()
//                    .get()
//                    .uri("http://localhost:8083/accounts/" + accountId)
//                    .retrieve()
//                    .onStatus(status -> status == HttpStatus.NOT_FOUND, response -> {
//                        return Mono.error(new RuntimeException("Account not found with ID: " + accountId));
//                    })
//                    .bodyToMono(Void.class)
//                    .block();
//
//            // 2. Get transactions for account (both incoming and outgoing)
//            List<Transaction> outgoingTransactions = transactionRepository.findOutgoingTransactions(accountId);
//            List<Transaction> incomingTransactions = transactionRepository.findIncomingTransactions(accountId);
//
//            if (outgoingTransactions.isEmpty() && incomingTransactions.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
//                        new ErrorResponse(404, "Not Found",
//                                "No transactions found for account ID " + accountId)
//                );
//            }
//
//            // 3. Process and combine transactions
//            List<TransactionResponseWithTheTwo> response = new ArrayList<>();
//
//            // Outgoing transactions (negative amounts)
//            outgoingTransactions.stream()
//                    .map(transaction -> new TransactionResponseWithTheTwo(
//                            transaction.getId(),
//                            transaction.getToAccountId(),
//                            transaction.getFromAccountId(),
//                            transaction.getAmount(),
//                            transaction.getDescription(), // Make amount negative
//                            transaction.getTimestamp()
//                    ))
//                    .forEach(response::add);
//
//            // Incoming transactions (positive amounts)
//            incomingTransactions.stream()
//                    .map(transaction -> new TransactionResponseWithTheTwo(
//                            transaction.getId(),
//                            transaction.getToAccountId(),
//                            transaction.getFromAccountId(),
//                            transaction.getAmount(), // Keep amount positive
//                            transaction.getDescription(),
//                            transaction.getTimestamp()
//                    ))
//                    .forEach(response::add);
//
//            // Sort by timestamp (most recent first)
//            response.sort(Comparator.comparing(TransactionResponseWithTheTwo::getTimestamp).reversed());
//
//            return ResponseEntity.ok(response);
//
//        } catch (RuntimeException e) {
//            if (e.getMessage().contains("Account not found")) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
//                        new ErrorResponse(404, "Not Found", e.getMessage())
//                );
//            }
//            return ResponseEntity.internalServerError().body(
//                    new ErrorResponse(500, "Internal Server Error",
//                            "Failed to retrieve transactions: " + e.getMessage())
//            );
//        }
//    }
}

