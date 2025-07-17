package com.transaction.transaction.controller;


import com.transaction.transaction.dto.InitiateRequestDto;
import com.transaction.transaction.service.impl.TransactionServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/transactions")
public class TransactionController {
    @Autowired
    private  TransactionServiceImpl transactionService;
 
    @PostMapping("/transfer/initiation")
    public ResponseEntity<?> initiateTransaction(@RequestBody InitiateRequestDto initiateRequestDto) {
        return transactionService.initiateTransaction(initiateRequestDto);
    }

}
    