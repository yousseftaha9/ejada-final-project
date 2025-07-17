package com.transaction.transaction.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.transaction.transaction.dto.AccountDto;
import com.transaction.transaction.dto.ErrorResponse;
import com.transaction.transaction.dto.ExecuteRequestDto;
import com.transaction.transaction.dto.InitiateRequestDto;
import com.transaction.transaction.dto.InitiateResponseDto;
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

        AccountDto fromAccount = webClientBuilder.build()
            .get()
            .uri("http://localhost:8083/accounts/" + initiateRequestDto.getFromAccountId())
            .retrieve()
            .bodyToMono(AccountDto.class)
            .block(); // blocking because we're not reactive here

        AccountDto toAccount = webClientBuilder.build()
            .get()
            .uri("http://localhost:8083/accounts/" + initiateRequestDto.getToAccountId())
            .retrieve()
            .bodyToMono(AccountDto.class)
            .block();

            if(fromAccount == null || toAccount == null) {
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'executeTransaction'");
    }
    
}
