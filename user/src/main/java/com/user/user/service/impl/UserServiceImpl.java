package com.user.user.service.impl;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.user.user.dto.RegisterRequestDto;
import com.user.user.dto.RegisterResponseDto;
import com.user.user.entity.User;
import com.user.user.exception.UserRegistrationException;
import com.user.user.repository.UserRepository;
import com.user.user.service.interfaces.UserService;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        }

    @Override
    public RegisterResponseDto  registerUser(RegisterRequestDto registerRequestDto) {
        try {

            User user = new User();
            user.setUsername(registerRequestDto.getUsername());
            user.setFirstName(registerRequestDto.getFirstName());
            user.setLastName(registerRequestDto.getLastName());
            user.setEmail(registerRequestDto.getEmail());
            user.setCreatedAt(LocalDateTime.now());
    
            String hashedPassword = passwordEncoder.encode(registerRequestDto.getPassword());
            user.setPassword(hashedPassword);
            User savedUser =  userRepository.save(user);
            if (savedUser == null) {
                throw new UserRegistrationException("User registration failed");
            }
            return new RegisterResponseDto(savedUser.getId(), savedUser.getUsername(), "User registered successfully");
        } catch(Exception e) {
            throw new UserRegistrationException("User registration failed: " + e.getMessage());
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
