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
import com.user.user.dto.ErrorResponse;
import com.user.user.entity.Users;
import com.user.user.repository.UserRepository;
import com.user.user.service.interfaces.UserService;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private KafkaLogger kafkaLogger;
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, WebClient.Builder webClientBuilder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public ResponseEntity<?> registerUser(RegisterRequestDto registerRequestDto) {
        try {
            kafkaLogger.log(registerRequestDto, "Request");

            if (registerRequestDto == null) {
                ErrorResponse errorResponse = new ErrorResponse(
                    400, 
                    "Bad Request", 
                    "Registration request cannot be null"
                );
                kafkaLogger.log(errorResponse, "Response");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            if (registerRequestDto.getUsername() == null || registerRequestDto.getUsername().trim().isEmpty()) {
                ErrorResponse errorResponse = new ErrorResponse(
                    400, 
                    "Bad Request", 
                    "Username is required"
                );
                kafkaLogger.log(errorResponse, "Response");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            if (registerRequestDto.getEmail() == null || registerRequestDto.getEmail().trim().isEmpty()) {
                ErrorResponse errorResponse = new ErrorResponse(
                    400, 
                    "Bad Request", 
                    "Email is required"
                );
                kafkaLogger.log(errorResponse, "Response");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            Users existingUserByUsername = userRepository.findByUsername(registerRequestDto.getUsername());
            if (existingUserByUsername != null) {
                ErrorResponse errorResponse = new ErrorResponse(
                    409, 
                    "Conflict", 
                    "Username or email already exists."
                );
                kafkaLogger.log(errorResponse, "Response");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            }
            
            Users existingUserByEmail = userRepository.findByEmail(registerRequestDto.getEmail());
            if (existingUserByEmail != null) {
                ErrorResponse errorResponse = new ErrorResponse(
                    409, 
                    "Conflict", 
                    "Username or email already exists."
                );
                kafkaLogger.log(errorResponse, "Response");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            }

            Users user = new Users();
            user.setUsername(registerRequestDto.getUsername());
            user.setFirstName(registerRequestDto.getFirstName());
            user.setLastName(registerRequestDto.getLastName());
            user.setEmail(registerRequestDto.getEmail());
            
            String hashedPassword = passwordEncoder.encode(registerRequestDto.getPassword());
            user.setPasswordHash(hashedPassword);
            
            user.setCreatedAt(LocalDateTime.now());
            user.setId(UUID.randomUUID().toString()); 
            Users savedUser = userRepository.save(user);
            
            if (savedUser == null) {
                ErrorResponse errorResponse = new ErrorResponse(
                    500, 
                    "Internal Server Error", 
                    "User registration failed - saved user is null"
                );
                kafkaLogger.log(errorResponse, "Response");
                return ResponseEntity.internalServerError().body(errorResponse);
            }
            
            RegisterResponseDto response = new RegisterResponseDto(savedUser.getId(), savedUser.getUsername(), "User registered successfully");
            kafkaLogger.log(response, "Response");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(
                500, 
                "Internal Server Error", 
                "User registration failed: " + e.getMessage()
            );
            kafkaLogger.log(errorResponse, "Response");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @Override
    public ResponseEntity<?> loginUser(LoginRequestDto loginRequestDto) {
        try {
            kafkaLogger.log(loginRequestDto, "Request");

            String username = loginRequestDto.getUsername();
            String password = loginRequestDto.getPassword();

            if (username == null || username.trim().isEmpty()) {
                ErrorResponse errorResponse = new ErrorResponse(
                    400,
                    "Bad Request",
                    "Username is required for login"
                );
                kafkaLogger.log(errorResponse, "Response");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            if (password == null || password.trim().isEmpty()) {
                ErrorResponse errorResponse = new ErrorResponse(    
                    400,    
                     "Bad Request",
                     "Password is required for login"
                 );
                 kafkaLogger.log(errorResponse, "Response");
                 return ResponseEntity.badRequest().body(errorResponse);
             }

            Users user = userRepository.findByUsername(username);
            if (user == null) {
                ErrorResponse errorResponse = new ErrorResponse(
                    404, 
                    "Not Found", 
                    "User not found with the provided username"
                );
                kafkaLogger.log(errorResponse, "Response");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            if (!passwordEncoder.matches(password, user.getPasswordHash())) {
                ErrorResponse errorResponse = new ErrorResponse(
                    401, 
                    "Unauthorized", 
                    "Invalid password"
                );
                kafkaLogger.log(errorResponse, "Response");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            LoginResponseDto loginResponse = new LoginResponseDto(user.getId(), user.getUsername());
            kafkaLogger.log(loginResponse, "Response");
            return ResponseEntity.ok(loginResponse);

        }
        catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(
                500, 
                "Internal Server Error", 
                "Login failed: " + e.getMessage()
            );
            kafkaLogger.log(errorResponse, "Response");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @Override
    public ResponseEntity<?> userProfile(String id) {
        try {
            // Log the request
            String requestJson = "{\"userId\":\"" + id + "\"}";
            kafkaLogger.log(requestJson, "Request");

            Users user = userRepository.findById(id).orElse(null);
            if (user == null) {
                ErrorResponse errorResponse = new ErrorResponse(
                    404, 
                    "Not Found", 
                    "User not found with the ID: " + id
                );
                kafkaLogger.log(errorResponse, "Response");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            ProfileResponseDto profileResponse = new ProfileResponseDto(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName()
                );
            kafkaLogger.log(profileResponse, "Response");
            return ResponseEntity.ok(profileResponse);
        }
        catch (Exception e){
            ErrorResponse errorResponse = new ErrorResponse(
                500, 
                "Internal Server Error", 
                "User profile retrieval failed: " + e.getMessage()
            );
            kafkaLogger.log(errorResponse, "Response");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
