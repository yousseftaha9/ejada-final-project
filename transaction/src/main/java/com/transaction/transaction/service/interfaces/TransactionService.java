package com.transaction.transaction.service.interfaces;

import com.transaction.transaction.dto.TransactionResponseWithType;
import org.springframework.http.ResponseEntity;

import com.transaction.transaction.dto.ExecuteRequestDto;
import com.transaction.transaction.dto.InitiateRequestDto;

import java.util.List;

public interface TransactionService {
    ResponseEntity<?> initiateTransaction(InitiateRequestDto initiateRequestDto);
    ResponseEntity<?> executeTransaction(ExecuteRequestDto executeRequestDto);
    List<TransactionResponseWithType> getAccountTransactions(String accountId);
   
}