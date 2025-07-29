package com.transaction.transaction.service.interfaces;

import com.transaction.transaction.dto.TransactionResponseWithType;

import com.transaction.transaction.dto.ExecuteRequestDto;
import com.transaction.transaction.dto.ExecuteResponseDto;
import com.transaction.transaction.dto.InitiateRequestDto;
import com.transaction.transaction.dto.InitiateResponseDto;

import java.util.List;

public interface TransactionService {
    InitiateResponseDto initiateTransaction(InitiateRequestDto initiateRequestDto);
    ExecuteResponseDto executeTransaction(ExecuteRequestDto executeRequestDto);
    List<TransactionResponseWithType> getAccountTransactions(String accountId);
   
}