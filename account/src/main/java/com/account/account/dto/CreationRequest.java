package com.account.account.dto;

import com.account.account.deserializer.AccountTypeDeserializer;
import com.account.account.enums.AccountType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreationRequest {
    @NotBlank(message = "User ID is required")
    private String userId;

    @NotNull(message = "Account type is required")
    @JsonDeserialize(using = AccountTypeDeserializer.class)
    private AccountType accountType;

    @NotNull(message = "Initial balance is required")
    @Positive(message = "Initial balance must be positive")
    private BigDecimal initialBalance;
}
