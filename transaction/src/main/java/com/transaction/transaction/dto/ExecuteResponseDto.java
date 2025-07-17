package com.transaction.transaction.dto;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteResponseDto {
    private String transactionId;
    private String status;
    private java.sql.Timestamp timestamp;

}
