package com.account.account.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreationResponse {
    private String accountId;
    private String accountNumber;
    private String message;

}
