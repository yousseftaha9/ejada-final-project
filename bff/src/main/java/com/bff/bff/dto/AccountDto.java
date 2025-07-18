package com.bff.bff.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
    private String accountID;
    private String accountNumber;
    private String accountType;
    private double balance;
    private List<TransactionDto> transactions;
}
