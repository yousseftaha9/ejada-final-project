package com.user.user.service.interfaces;

import com.user.user.dto.RegisterRequestDto;
import com.user.user.dto.RegisterResponseDto;
import com.user.user.entity.User;

public interface UserService {
    RegisterResponseDto registerUser(RegisterRequestDto registerRequestDto);
    User loginUser(String email, String password);  
    User userProfile(long id);

}
