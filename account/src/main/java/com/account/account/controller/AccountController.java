package com.account.account.controller;

import com.account.account.dto.CreationRequest;
import com.account.account.service.interfaces.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    AccountService accountService;
    // PUT /accounts/transfer: Update account Balance.
    // GET /accounts/{accountId}: Retrieves details of a specific bank account.
    @GetMapping("/{id}")
    public ResponseEntity<?> getAccount(@PathVariable String id){
        return accountService.getAccount(id);
    }
    // POST /accounts: Creates a new bank account for a specified user.
    @PostMapping
    public ResponseEntity<?> createAccount(@RequestBody CreationRequest creationRequest){
        return accountService.createAccount(creationRequest);
    }
    // GET /users/{userId}/accounts: Lists all accounts associated with a given user.
    @GetMapping("/user/{userId}/accounts")
    public ResponseEntity<?> getUserAccounts(@PathVariable String userId){
        return accountService.getUserAccounts(userId);
    }

}
