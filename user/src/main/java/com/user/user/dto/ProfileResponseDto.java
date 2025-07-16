package com.user.user.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class ProfileResponseDto {
    private long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;

}
