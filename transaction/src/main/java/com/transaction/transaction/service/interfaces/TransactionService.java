package com.transaction.transaction.service.interfaces;

import org.springframework.http.ResponseEntity;

import com.transaction.transaction.dto.ExecuteRequestDto;
import com.transaction.transaction.dto.InitiateRequestDto;

public interface TransactionService {
    ResponseEntity<?> initiateTransaction(InitiateRequestDto initiateRequestDto);
    ResponseEntity<?> executeTransaction(ExecuteRequestDto executeRequestDto);
   
}