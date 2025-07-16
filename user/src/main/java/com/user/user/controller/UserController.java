package com.user.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import com.user.user.dto.RegisterRequestDto;
import com.user.user.dto.RegisterResponseDto;
import com.user.user.service.impl.UserServiceImpl;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Controller
@RestController("/user")
public class UserController {
    @Autowired
    private UserServiceImpl userService;

    @PostMapping("/register")
    public RegisterResponseDto postMethodName(@RequestBody RegisterRequestDto registerRequestDto) {
        return this.userService.registerUser(registerRequestDto);
    }
    
}
