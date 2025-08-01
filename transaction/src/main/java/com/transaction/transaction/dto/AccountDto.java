package com.transaction.transaction.dto;

import com.transaction.transaction.enums.AccountStatus;
import com.transaction.transaction.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
    private String accountID;
    private String accountNumber;
    private AccountType accountType;
    private BigDecimal balance;
    private AccountStatus accountStatus;

}
