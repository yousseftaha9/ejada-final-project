package com.transaction.transaction.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.transaction.transaction.entity.Transactions;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transactions, String> {
    @Query("SELECT t FROM Transactions t WHERE t.fromAccountId = :accountId and t.status = 'success'")
    List<Transactions> findOutgoingTransactions(@Param("accountId") String accountId);

    @Query("SELECT t FROM Transactions t WHERE t.toAccountId = :accountId and t.status = 'success'")
    List<Transactions> findIncomingTransactions(@Param("accountId") String accountId);
}
