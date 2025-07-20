package com.account.account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {
    private String transactionId;
    private double amount;
    private String accountId;
    private String description;
    private Timestamp timestamp;
    private String type;
}
