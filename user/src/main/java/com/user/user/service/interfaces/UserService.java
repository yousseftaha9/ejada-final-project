package com.user.user.service.interfaces;


import com.user.user.dto.RegisterRequestDto;
import com.user.user.dto.RegisterResponseDto;
import com.user.user.dto.LoginRequestDto;
import com.user.user.dto.LoginResponseDto;
import com.user.user.dto.ProfileResponseDto;

public interface UserService {
    RegisterResponseDto registerUser(RegisterRequestDto registerRequestDto);
    LoginResponseDto loginUser(LoginRequestDto loginRequestDto);
    ProfileResponseDto userProfile(String id);
}
