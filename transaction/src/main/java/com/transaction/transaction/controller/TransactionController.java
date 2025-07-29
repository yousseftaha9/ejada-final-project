package com.transaction.transaction.controller;


import com.transaction.transaction.dto.ExecuteRequestDto;
import com.transaction.transaction.dto.ExecuteResponseDto;
import com.transaction.transaction.dto.InitiateRequestDto;
import com.transaction.transaction.dto.InitiateResponseDto;
import com.transaction.transaction.dto.TransactionResponseWithType;
import com.transaction.transaction.service.interfaces.TransactionService;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
public class TransactionController {
    @Autowired
    private  TransactionService transactionService;
 
    @PostMapping("/transactions/transfer/initiation")
    public ResponseEntity<InitiateResponseDto> initiateTransaction(@Valid @RequestBody InitiateRequestDto initiateRequestDto) {
        return ResponseEntity.ok(transactionService.initiateTransaction(initiateRequestDto));
    }
    @PostMapping("/transactions/transfer/execution")
    public ResponseEntity<ExecuteResponseDto> executeTransaction(@Valid @RequestBody ExecuteRequestDto executeRequestDto) {
        return ResponseEntity.ok(transactionService.executeTransaction(executeRequestDto));
    }

    @GetMapping("/accounts/{accountId}/transactions")
    public ResponseEntity<List<TransactionResponseWithType>> getAccountTransactions(@PathVariable @NotBlank(message = "Account ID cannot be blank") String accountId){
        return ResponseEntity.ok(transactionService.getAccountTransactions(accountId));
    }

}
    