package com.transaction.transaction.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.transaction.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
    @Query("SELECT t FROM Transaction t WHERE t.fromAccountId = :accountId")
    List<Transaction> findOutgoingTransactions(@Param("accountId") String accountId);

    @Query("SELECT t FROM Transaction t WHERE t.toAccountId = :accountId")
    List<Transaction> findIncomingTransactions(@Param("accountId") String accountId);
}
