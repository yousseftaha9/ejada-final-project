package com.user.user.service.interfaces;

import org.springframework.http.ResponseEntity;

import com.user.user.dto.RegisterRequestDto;
import com.user.user.dto.LoginRequestDto;
import reactor.core.publisher.Mono;

public interface UserService {
    ResponseEntity<?> registerUser(RegisterRequestDto registerRequestDto);
    ResponseEntity<?> loginUser(LoginRequestDto loginRequestDto);
    ResponseEntity<?> userProfile(String id);
}
