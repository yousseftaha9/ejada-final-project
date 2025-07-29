package com.account.account.controller;

import com.account.account.dto.AccountResponse;
import com.account.account.dto.CreationRequest;
import com.account.account.dto.CreationResponse;
import com.account.account.dto.TransferRequest;
import com.account.account.service.interfaces.AccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
public class AccountController {

    @Autowired
    AccountService accountService;
    // PUT /accounts/transfer: Update account Balance.
    @PutMapping("/accounts/transfer")
    public ResponseEntity<Map<String, String>> updateBalance(@Valid @RequestBody TransferRequest transferRequest){
        return ResponseEntity.ok(accountService.updateBalance(transferRequest));
    }
    // GET /accounts/{accountId}: Retrieves details of a specific bank account.
    @GetMapping("/accounts/{id}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable @NotBlank(message = "Account ID cannot be blank") String id){
        return ResponseEntity.ok(accountService.getAccount(id));
    }
    // POST /accounts: Creates a new bank account for a specified user.
    @PostMapping("/accounts")
    public ResponseEntity<CreationResponse> createAccount(@Valid @RequestBody CreationRequest creationRequest){
        return ResponseEntity.ok(accountService.createAccount(creationRequest));
    }
    // GET /users/{userId}/accounts: Lists all accounts associated with a given user.
    @GetMapping("/users/{userId}/accounts")
    public ResponseEntity<List<AccountResponse>> getUserAccounts(@PathVariable @NotBlank(message = "User ID cannot be blank") String userId){
        return ResponseEntity.ok(accountService.getUserAccounts(userId));
    }

}
