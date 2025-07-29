package com.account.account.service.interfaces;


import com.account.account.dto.AccountResponse;
import com.account.account.dto.CreationRequest;
import com.account.account.dto.CreationResponse;
import com.account.account.dto.TransferRequest;

import java.util.List;
import java.util.Map;

public interface AccountService {

    // PUT /accounts/transfer: Update account Balance.
    public Map<String, String>  updateBalance(TransferRequest transferRequest);
    // GET /accounts/{accountId}: Retrieves details of a specific bank account.
    public AccountResponse getAccount(String id);
    // POST /accounts: Creates a new bank account for a specified user.
    public CreationResponse createAccount(CreationRequest creationRequest);
    // GET /users/{userId}/accounts: Lists all accounts associated with a given user.
    public List<AccountResponse> getUserAccounts(String userId);

}
