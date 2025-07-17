package com.account.account.dto;

import com.account.account.entity.Account;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    private String accountID;
    private String accountNumber;
    private Account.AccountType accountType;
    private BigDecimal balance;
    private Account.AccountStatus accountStatus;

}
