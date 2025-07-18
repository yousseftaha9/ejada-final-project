package com.transaction.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponseWithType {
    private String transactionId;
    private String accountId;
    private BigDecimal amount;
    private String description;
    private Timestamp timestamp;
    private String type;
}
