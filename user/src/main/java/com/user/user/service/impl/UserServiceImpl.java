package com.user.user.service.impl;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.user.user.dto.RegisterRequestDto;
import com.user.user.dto.RegisterResponseDto;
import com.user.user.dto.RegisterErrorResponse;
import com.user.user.entity.User;
import com.user.user.exception.UserRegistrationException;
import com.user.user.repository.UserRepository;
import com.user.user.service.interfaces.UserService;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public ResponseEntity<?> registerUser(RegisterRequestDto registerRequestDto) {
        try {
            // Validate input
            if (registerRequestDto == null) {
                RegisterErrorResponse errorResponse = new RegisterErrorResponse(
                    400, 
                    "Bad Request", 
                    "Registration request cannot be null"
                );
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            if (registerRequestDto.getUsername() == null || registerRequestDto.getUsername().trim().isEmpty()) {
                RegisterErrorResponse errorResponse = new RegisterErrorResponse(
                    400, 
                    "Bad Request", 
                    "Username is required"
                );
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            if (registerRequestDto.getEmail() == null || registerRequestDto.getEmail().trim().isEmpty()) {
                RegisterErrorResponse errorResponse = new RegisterErrorResponse(
                    400, 
                    "Bad Request", 
                    "Email is required"
                );
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Check if username already exists
            User existingUserByUsername = userRepository.findByUsername(registerRequestDto.getUsername());
            if (existingUserByUsername != null) {
                RegisterErrorResponse errorResponse = new RegisterErrorResponse(
                    409, 
                    "Conflict", 
                    "Username or email already exists."
                );
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            }
            
            // Check if email already exists
            User existingUserByEmail = userRepository.findByEmail(registerRequestDto.getEmail());
            if (existingUserByEmail != null) {
                RegisterErrorResponse errorResponse = new RegisterErrorResponse(
                    409, 
                    "Conflict", 
                    "Username or email already exists."
                );
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            }

            // Create and save user
            User user = new User();
            user.setUsername(registerRequestDto.getUsername());
            user.setFirstName(registerRequestDto.getFirstName());
            user.setLastName(registerRequestDto.getLastName());
            user.setEmail(registerRequestDto.getEmail());
            user.setPassword(registerRequestDto.getPassword());
            user.setCreatedAt(LocalDateTime.now());
            
            User savedUser = userRepository.save(user);
            
            if (savedUser == null) {
                RegisterErrorResponse errorResponse = new RegisterErrorResponse(
                    500, 
                    "Internal Server Error", 
                    "User registration failed - saved user is null"
                );
                return ResponseEntity.internalServerError().body(errorResponse);
            }
            
            RegisterResponseDto response = new RegisterResponseDto(savedUser.getId(), savedUser.getUsername(), "User registered successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            RegisterErrorResponse errorResponse = new RegisterErrorResponse(
                500, 
                "Internal Server Error", 
                "User registration failed: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @Override
    public User loginUser(String email, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'loginUser'");
    }

    @Override
    public User userProfile(long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'userProfile'");
    }
}
