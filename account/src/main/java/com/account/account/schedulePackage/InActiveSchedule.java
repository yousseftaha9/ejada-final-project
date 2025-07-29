package com.account.account.schedulePackage;

import com.account.account.dto.TransactionDto;
import com.account.account.entity.Account;
import com.account.account.enums.AccountStatus;
import com.account.account.repository.AccountRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class InActiveSchedule {
    @Autowired
    private  AccountRepository accountRepository;
    @Autowired
    private  WebClient.Builder webClientBuilder;
    @Scheduled(fixedRate = 3600000)
    @Async
    @Transactional
    public void refreshPricingParameters() {
        List<Account> accounts = accountRepository.findByStatus(AccountStatus.ACTIVE);
        accounts.forEach(account -> {
            webClientBuilder.build()
                    .get()
                    .uri("http://localhost:8082/accounts/{accountId}/transactions", account.getId())
                    .retrieve()
                    .onStatus(
                            status -> status == HttpStatus.NOT_FOUND,
                            response -> {
                                deactivateAccountWithNoTransactions(account);
                                return Mono.empty();
                            }
                    )
                    .onStatus(
                            HttpStatusCode::isError,  // Fixed: Using lambda instead of method reference
                            response -> Mono.empty()
                    )
                    .bodyToFlux(TransactionDto.class)
                    .collectList()
                    .timeout(Duration.ofSeconds(30))
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                    .onErrorResume(e -> Mono.empty())
                    .subscribe(transactions -> {
                        if (transactions != null && !transactions.isEmpty()) {
                            List<TransactionDto> deliveredTransactions = transactions.stream()
                                    .filter(t -> "Sent".equals(t.getType()))
                                    .collect(Collectors.toList());

                            if (!deliveredTransactions.isEmpty()) {
                                checkLastDeliveredTransaction(account, deliveredTransactions);
                            } else {
                                deactivateAccountWithNoTransactions(account);
                            }
                        }
                    });
        });
    }

    private void checkLastDeliveredTransaction(Account account, List<TransactionDto> transactions) {
        TransactionDto lastTransaction = transactions.stream()
                .max(Comparator.comparing(TransactionDto::getTimestamp))
                .orElseThrow();

        Instant twentyFourHoursAgo = Instant.now().minus(24, ChronoUnit.HOURS);

        if (lastTransaction.getTimestamp().before(Timestamp.from(twentyFourHoursAgo))) {
            account.setStatus(AccountStatus.INACTIVE);
            accountRepository.save(account);
        }
    }

    private void deactivateAccountWithNoTransactions(Account account) {
        account.setStatus(AccountStatus.INACTIVE);
        accountRepository.save(account);
    }
}
