package com.account.account.service.impl;

import com.account.account.dto.AccountResponse;
import com.account.account.dto.CreationRequest;
import com.account.account.dto.CreationResponse;
import com.account.account.dto.ErrorResponse;
import com.account.account.entity.Account;
import com.account.account.entity.AccountStatus;
import com.account.account.repository.AccountRepository;
import com.account.account.service.interfaces.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository){
        this.accountRepository = accountRepository;
    }
    public ResponseEntity<?> getAccount(String id) {
        try {
            Account account = accountRepository.findById(id).orElse(null);
            if (account == null) {
                ErrorResponse errorResponse = new ErrorResponse(
                        404,
                        "Not Found",
                        "Account not found with the provided ID"
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            AccountResponse profileResponse = new AccountResponse(
                    account.getId(),
                    account.getAccountNumber(),
                    account.getAccountType(),
                    account.getBalance(),
                    account.getStatus()
            );
            return ResponseEntity.ok(profileResponse);
        }
        catch (Exception e){
            ErrorResponse errorResponse = new ErrorResponse(
                    500,
                    "Internal Server Error",
                    "Account retrieval failed: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    // POST /accounts: Creates a new bank account for a specified user.
    public ResponseEntity<?> createAccount(CreationRequest creationRequest){
//        User user = userRepository.findById(request.getUserId())
//                .orElseThrow(() -> new UserNotFoundException(request.getUserId()));

        // Create and save account
        if(creationRequest.getUserId() == null){
            ErrorResponse errorResponse = new ErrorResponse(
                    500,
                    "Internal Server Error",
                    "the error is here"
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
        Account account = buildAccount(creationRequest);
        Account savedAccount = accountRepository.save(account);

        // Return response
        return ResponseEntity.ok( new CreationResponse(
                savedAccount.getId(),
                savedAccount.getAccountNumber(),
                "Account created successfully"
        )) ;
    }
    private Account buildAccount(CreationRequest request) {
        Account account = new Account();
        account.setId(UUID.randomUUID().toString()); // Generate a unique ID
        account.setAccountType(request.getAccountType());
        account.setStatus(AccountStatus.ACTIVE);
        account.setBalance(request.getInitialBalance());

        account.setUserId(request.getUserId());

        account.setAccountNumber(generateAccountNumber());
        return account;
    }
    private String generateAccountNumber() {
        // Implement proper account number generation
        return String.format("%010d", new Random().nextInt(1_000_000_000));
    }
    // GET /users/{userId}/accounts: Lists all accounts associated with a given user.
    public ResponseEntity<?> getUserAccounts(String userId){
        try {
            // check user first
            // User
//            if (accounts == null) {
//                ErrorResponse errorResponse = new ErrorResponse(
//                        404,
//                        "Not Found",
//                        "User not found with the provided ID"
//                );
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
//            }
            List<Account> accounts = accountRepository.findByUserId(userId);
            List<AccountResponse> response = accounts.stream()
                    .map(account -> new AccountResponse(
                            account.getId(),
                            account.getAccountNumber(),
                            account.getAccountType(),
                            account.getBalance(),
                            account.getStatus()
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        }
        catch (Exception e){
            ErrorResponse errorResponse = new ErrorResponse(
                    500,
                    "Internal Server Error",
                    "Account retrieval failed: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
