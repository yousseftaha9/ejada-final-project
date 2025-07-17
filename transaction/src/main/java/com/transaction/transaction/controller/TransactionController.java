package com.transaction.transaction.controller;


import com.transaction.transaction.dto.ExecuteRequestDto;
import com.transaction.transaction.dto.InitiateRequestDto;
import com.transaction.transaction.service.impl.TransactionServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
//@RequestMapping("/transactions")
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
    public ResponseEntity<?> getAccountTransactions(@PathVariable String accountId){
        return transactionService.getAccountTransactions(accountId);
    }

}
    