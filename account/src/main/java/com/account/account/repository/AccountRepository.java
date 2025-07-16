package com.account.account.repository;

import com.account.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account,String> {
    public List<Account> findByUserId(String userID);
}
