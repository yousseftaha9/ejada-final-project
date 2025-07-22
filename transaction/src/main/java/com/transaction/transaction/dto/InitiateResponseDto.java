package com.transaction.transaction.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InitiateResponseDto {
    private String transactionId;
    private String status;
    private String timestamp;

}
