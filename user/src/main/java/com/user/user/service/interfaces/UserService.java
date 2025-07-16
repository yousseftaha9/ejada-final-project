package com.user.user.service.interfaces;

import org.springframework.http.ResponseEntity;

import com.user.user.dto.RegisterRequestDto;
import com.user.user.dto.RegisterResponseDto;
import com.user.user.dto.LoginRequestDto;
import com.user.user.dto.RegisterErrorResponse;
import com.user.user.entity.User;

public interface UserService {
    ResponseEntity<?> registerUser(RegisterRequestDto registerRequestDto);
    ResponseEntity<?> loginUser(LoginRequestDto loginRequestDto);
    ResponseEntity<?> userProfile(long id);
}
