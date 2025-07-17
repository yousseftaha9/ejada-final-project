package com.account.account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponseDto {
    private String userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
}
