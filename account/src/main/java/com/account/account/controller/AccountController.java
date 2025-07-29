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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
public class AccountController {

    @Autowired
    AccountService accountService;
    // PUT /accounts/transfer: Update account Balance.
    @PutMapping("/accounts/transfer")
    public Map<String, String>  updateBalance(@Valid @RequestBody TransferRequest transferRequest){
        return accountService.updateBalance(transferRequest);
    }
    // GET /accounts/{accountId}: Retrieves details of a specific bank account.
    @GetMapping("/accounts/{id}")
    public AccountResponse getAccount(@PathVariable @NotBlank(message = "Account ID cannot be blank") String id){
        return accountService.getAccount(id);
    }
    // POST /accounts: Creates a new bank account for a specified user.
    @PostMapping("/accounts")
    public CreationResponse createAccount(@Valid @RequestBody CreationRequest creationRequest){
        return accountService.createAccount(creationRequest);
    }
    // GET /users/{userId}/accounts: Lists all accounts associated with a given user.
    @GetMapping("/users/{userId}/accounts")
    public List<AccountResponse> getUserAccounts(@PathVariable @NotBlank(message = "User ID cannot be blank") String userId){
        return accountService.getUserAccounts(userId);
    }

}
