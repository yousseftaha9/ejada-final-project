package com.user.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.user.user.entity.Users;
import java.util.List;

public interface UserRepository extends JpaRepository<Users, String> {

    Users findByUsername(String username);
    
    Users findByEmail(String email);
    
    @Query("SELECT u FROM Users u WHERE u.username = :username OR u.email = :email")
    List<Users> findByUsernameOrEmail(@Param("username") String username, @Param("email") String email);
    
}
    