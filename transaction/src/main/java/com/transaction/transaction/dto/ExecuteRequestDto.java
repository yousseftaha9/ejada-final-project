package com.transaction.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteRequestDto {
    @NotBlank(message = "Transaction ID is required")
    private String transactionId;
}
