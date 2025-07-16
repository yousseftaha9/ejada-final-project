package com.user.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.user.user.entity.User;

public interface UserRepository extends JpaRepository<User, String> {

    User findByUsername(String username);
    
    User findByEmail(String email);
    
}
    