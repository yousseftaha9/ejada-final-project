package com.transaction.transaction.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InitiateRequestDto {
    private String fromAccountId;
    private String toAccountId;
    private BigDecimal amount;
    private String description;

   
}
