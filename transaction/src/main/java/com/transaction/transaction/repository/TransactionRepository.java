package com.transaction.transaction.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.transaction.transaction.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

}
