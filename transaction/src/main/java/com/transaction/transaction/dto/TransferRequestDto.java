package com.transaction.transaction.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequestDto {
    private String fromAccountId;
    private String toAccountId;
    private double amount;

  
}
