package com.transaction.transaction.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.transaction.transaction.dto.AccountDto;
import com.transaction.transaction.dto.ErrorResponse;
import com.transaction.transaction.dto.ExecuteRequestDto;
import com.transaction.transaction.dto.ExecuteResponseDto;
import com.transaction.transaction.dto.InitiateRequestDto;
import com.transaction.transaction.dto.InitiateResponseDto;
import com.transaction.transaction.dto.TransferRequestDto;
import com.transaction.transaction.entity.Transaction;
import com.transaction.transaction.repository.TransactionRepository;
import com.transaction.transaction.service.interfaces.TransactionService;

@Service
public class TransactionServiceImpl implements TransactionService {

      private final WebClient.Builder webClientBuilder;
      private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionServiceImpl(WebClient.Builder webClientBuilder, TransactionRepository transactionRepository) {
        this.webClientBuilder = webClientBuilder;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public ResponseEntity<?> initiateTransaction(InitiateRequestDto initiateRequestDto) {
        if (initiateRequestDto == null) {
            ErrorResponse errorResponse = new ErrorResponse(
                400, 
                "Bad Request", 
                "Initiate request cannot be null"
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }

        AccountDto fromAccount;
        AccountDto toAccount;

        try {
            fromAccount = webClientBuilder.build()
                .get()
                .uri("http://localhost:8083/accounts/" + initiateRequestDto.getFromAccountId())
                .retrieve()
                .bodyToMono(AccountDto.class)
                .block();

            toAccount = webClientBuilder.build()
                .get()
                .uri("http://localhost:8083/accounts/" + initiateRequestDto.getToAccountId())
                .retrieve()
                .bodyToMono(AccountDto.class)
                .block();

        } catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {
            ErrorResponse errorResponse = new ErrorResponse(
                400,
                "Bad Request",
                "Failed to retrieve account information: " + e.getMessage()
            );
            return ResponseEntity.status(e.getStatusCode()).body(errorResponse);
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(
                500,
                "Internal Server Error",
                "Failed to communicate with account service: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }

        if (fromAccount == null || toAccount == null) {
            ErrorResponse errorResponse = new ErrorResponse(
                400, 
                "Bad Request", 
                "Invalid account details provided"
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
        if(fromAccount.getBalance().compareTo(initiateRequestDto.getAmount()) < 0) {
            ErrorResponse errorResponse = new ErrorResponse(
                400, 
                "Bad Request", 
                "Insufficient balance in the from account"
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }

        Transaction transaction = new Transaction();
        transaction.setId(java.util.UUID.randomUUID().toString());
        transaction.setFromAccountId(initiateRequestDto.getFromAccountId());
        transaction.setToAccountId(initiateRequestDto.getToAccountId());
        transaction.setAmount(initiateRequestDto.getAmount());
        transaction.setDescription(initiateRequestDto.getDescription());
        transaction.setStatus(Transaction.Status.INITIATED);
        transaction.setTimestamp(new java.sql.Timestamp(System.currentTimeMillis()));
        transactionRepository.save(transaction);

        InitiateResponseDto initiateResponseDto = new InitiateResponseDto();
        initiateResponseDto.setTransactionId(transaction.getId());
        initiateResponseDto.setStatus(transaction.getStatus().name());
            initiateResponseDto.setTimestamp(transaction.getTimestamp());

        
        return ResponseEntity.ok(initiateResponseDto);
    }

    @Override
    public ResponseEntity<?> executeTransaction(ExecuteRequestDto executeRequestDto) {
        Transaction transaction = transactionRepository.findById(executeRequestDto.getTransactionId())
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (transaction.getStatus() != Transaction.Status.INITIATED) {
            ErrorResponse errorResponse = new ErrorResponse(
                400, 
                "Bad Request", 
                "Transaction is not in a state that can be executed"
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
        try {
            webClientBuilder.build()
                .put()
                .uri("http://localhost:8083/accounts/transfer")
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
            return ResponseEntity.status(e.getStatusCode()).body(errorResponse);
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(
                500,
                "Internal Server Error",
                "Failed to communicate with account service: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }

        // Update transaction status to SUCCESS
        transaction.setStatus(Transaction.Status.SUCCESS);
        transactionRepository.save(transaction);

        ExecuteResponseDto responseDto = new ExecuteResponseDto();
        responseDto.setTransactionId(transaction.getId());
        responseDto.setStatus(transaction.getStatus().name());
        responseDto.setTimestamp(transaction.getTimestamp());
        return ResponseEntity.ok(responseDto);

    }

    
}
