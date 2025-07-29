package com.transaction.transaction.controller;


import com.transaction.transaction.dto.ExecuteRequestDto;
import com.transaction.transaction.dto.InitiateRequestDto;
import com.transaction.transaction.service.impl.TransactionServiceImpl;

import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
//@RequestMapping("/transactions")
@Validated
public class TransactionController {
    @Autowired
    private  TransactionServiceImpl transactionService;
 
    @PostMapping("/transactions/transfer/initiation")
    public ResponseEntity<?> initiateTransaction(@RequestBody InitiateRequestDto initiateRequestDto) {
        return transactionService.initiateTransaction(initiateRequestDto);
    }
    @PostMapping("/transactions/transfer/execution")
    public ResponseEntity<?> executeTransaction(@RequestBody ExecuteRequestDto executeRequestDto) {
        return transactionService.executeTransaction(executeRequestDto);
    }

    @GetMapping("/accounts/{accountId}/transactions")
    public ResponseEntity<?> getAccountTransactions(@PathVariable @NotBlank(message = "Account ID cannot be blank") String accountId){
        return ResponseEntity.ok(transactionService.getAccountTransactions(accountId));
    }

}
    