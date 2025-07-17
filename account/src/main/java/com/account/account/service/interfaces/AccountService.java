package com.account.account.service.interfaces;


import com.account.account.dto.CreationRequest;
import com.account.account.dto.TransferRequest;
import org.springframework.http.ResponseEntity;

public interface AccountService {

    // PUT /accounts/transfer: Update account Balance.
    public ResponseEntity<?> updateBalance(TransferRequest transferRequest);
    // GET /accounts/{accountId}: Retrieves details of a specific bank account.
    public ResponseEntity<?> getAccount(String id);
    // POST /accounts: Creates a new bank account for a specified user.
    public ResponseEntity<?> createAccount(CreationRequest creationRequest);
    // GET /users/{userId}/accounts: Lists all accounts associated with a given user.
    public ResponseEntity<?> getUserAccounts(String userId);

}
