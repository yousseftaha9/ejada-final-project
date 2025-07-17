package com.transaction.transaction.dto;

import java.security.Timestamp;
import java.sql.Time;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InitiateResponseDto {
    private String transactionId;
    private String status;
    private java.sql.Timestamp timestamp;

}
