package com.bff.bff.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponseDto {
    private String userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private List<AccountDto> accounts;
}
