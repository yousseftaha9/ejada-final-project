package com.user.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.user.user.entity.Users;

public interface UserRepository extends JpaRepository<Users, String> {

    Users findByUsername(String username);
    
    Users findByEmail(String email);
    
}
    