package com.user.user.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.user.user.dto.RegisterRequestDto;
import com.user.user.dto.RegisterResponseDto;
import com.user.user.dto.LoginRequestDto;
import com.user.user.dto.LoginResponseDto;
import com.user.user.dto.ProfileResponseDto;
import com.user.user.dto.RegisterErrorResponse;
import com.user.user.entity.User;
import com.user.user.exception.UserRegistrationException;
import com.user.user.repository.UserRepository;
import com.user.user.service.interfaces.UserService;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public ResponseEntity<?> registerUser(RegisterRequestDto registerRequestDto) {
        try {
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
            
            User existingUserByEmail = userRepository.findByEmail(registerRequestDto.getEmail());
            if (existingUserByEmail != null) {
                RegisterErrorResponse errorResponse = new RegisterErrorResponse(
                    409, 
                    "Conflict", 
                    "Username or email already exists."
                );
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            }

            User user = new User();
            user.setUsername(registerRequestDto.getUsername());
            user.setFirstName(registerRequestDto.getFirstName());
            user.setLastName(registerRequestDto.getLastName());
            user.setEmail(registerRequestDto.getEmail());
            
            String hashedPassword = passwordEncoder.encode(registerRequestDto.getPassword());
            user.setPassword(hashedPassword);
            
            user.setCreatedAt(LocalDateTime.now());
            user.setId(UUID.randomUUID().toString()); // Generate a unique ID
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
    public ResponseEntity<?> loginUser(LoginRequestDto loginRequestDto) {
        try {
            String username = loginRequestDto.getUsername();
            String password = loginRequestDto.getPassword();

            if (username == null || username.trim().isEmpty()) {
                RegisterErrorResponse errorResponse = new RegisterErrorResponse(
                    400,
                    "Bad Request",
                    "Username is required for login"
                );
                return ResponseEntity.badRequest().body(errorResponse);
            }
                        if (password == null || password.trim().isEmpty()) {
                RegisterErrorResponse errorResponse = new RegisterErrorResponse(    
                    400,    
                     "Bad Request",
                     "Password is required for login"
                 );
                 return ResponseEntity.badRequest().body(errorResponse);
             }

            User user = userRepository.findByUsername(username);
            if (user == null) {
                RegisterErrorResponse errorResponse = new RegisterErrorResponse(
                    404, 
                    "Not Found", 
                    "User not found with the provided username"
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            if (!passwordEncoder.matches(password, user.getPassword())) {
                RegisterErrorResponse errorResponse = new RegisterErrorResponse(
                    401, 
                    "Unauthorized", 
                    "Invalid password"
                );
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            LoginResponseDto loginResponse = new LoginResponseDto(user.getId(), user.getUsername());
            return ResponseEntity.ok(loginResponse);




        }
        catch (Exception e) {
            RegisterErrorResponse errorResponse = new RegisterErrorResponse(
                500, 
                "Internal Server Error", 
                "Login failed: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @Override
    public ResponseEntity<?> userProfile(String id) {
        try {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                RegisterErrorResponse errorResponse = new RegisterErrorResponse(
                    404, 
                    "Not Found", 
                    "User not found with the provided ID"
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            ProfileResponseDto profileResponse = new ProfileResponseDto(
                user.getId(), 
                user.getUsername(), 
                user.getEmail(), 
                user.getFirstName(), 
                user.getLastName()
            );
            return ResponseEntity.ok(profileResponse);
        }
        catch (Exception e){
            RegisterErrorResponse errorResponse = new RegisterErrorResponse(
                500, 
                "Internal Server Error", 
                "User profile retrieval failed: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
