package com.account.account.entity;


import com.account.account.enums.AccountType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.account.account.enums.AccountStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "accounts")
public class Account {
    @Id
    private String id;
    @Column(nullable = false)
    private String userId;
    @Column(length=20, unique=true, nullable = false)
    private String accountNumber;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType;
    @Column(precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private AccountStatus status = AccountStatus.ACTIVE;
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

}





