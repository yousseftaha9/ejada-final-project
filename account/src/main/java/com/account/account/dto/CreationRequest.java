package com.account.account.dto;

import com.account.account.entity.AccountType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreationRequest {
    private String userId;
    private AccountType accountType;
    private BigDecimal initialBalance;

}
