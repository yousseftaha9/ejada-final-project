package com.transaction.transaction.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InitiateRequestDto {
    @NotBlank(message = "From account ID is required")
    private String fromAccountId;
    
    @NotBlank(message = "To account ID is required")
    private String toAccountId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    private String description;
}
