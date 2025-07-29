package com.user.user.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.user.user.dto.RegisterRequestDto;
import com.user.user.dto.RegisterResponseDto;
import com.user.user.dto.LoginRequestDto;
import com.user.user.dto.LoginResponseDto;
import com.user.user.dto.ProfileResponseDto;
import com.user.user.entity.Users;
import com.user.user.repository.UserRepository;
import com.user.user.service.interfaces.UserService;
import com.user.user.exception.UserRegistrationException;
import com.user.user.exception.UserNotFoundException;
import com.user.user.exception.InvalidCredentialsException;
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
    public RegisterResponseDto registerUser(RegisterRequestDto registerRequestDto) {
        kafkaLogger.log(registerRequestDto, "Request");

        List<Users> existingUsers = userRepository.findByUsernameOrEmail(
            registerRequestDto.getUsername(), 
            registerRequestDto.getEmail()
        );
        
        if (!existingUsers.isEmpty()) {
            throw new UserRegistrationException("Username or email already exists.");
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
            throw new UserRegistrationException("User registration failed - saved user is null");
        }
        
        RegisterResponseDto response = new RegisterResponseDto(savedUser.getId(), savedUser.getUsername(), "User registered successfully");
        kafkaLogger.log(response, "Response");
        return response;
    }

    @Override
    public LoginResponseDto loginUser(LoginRequestDto loginRequestDto) {
        kafkaLogger.log(loginRequestDto, "Request");

        String username = loginRequestDto.getUsername();
        String password = loginRequestDto.getPassword();

        Users user = userRepository.findByUsername(username);
        if (user == null) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        LoginResponseDto loginResponse = new LoginResponseDto(user.getId(), user.getUsername());
        kafkaLogger.log(loginResponse, "Response");
        return loginResponse;
    }

    @Override
    public ProfileResponseDto userProfile(String id) {
        String requestJson = "{\"userId\":\"" + id + "\"}";
        kafkaLogger.log(requestJson, "Request");

        Users user = userRepository.findById(id).orElse(null);
        if (user == null) {
            throw new UserNotFoundException("User not found with the ID: " + id);
        }

        ProfileResponseDto profileResponse = new ProfileResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
            );
        kafkaLogger.log(profileResponse, "Response");
        return profileResponse;
    }
}
