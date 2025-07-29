package com.bff.bff.service.interfaces;

import com.bff.bff.dto.AccountDto;
import com.bff.bff.dto.DashboardResponseDto;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public interface BffService {
    DashboardResponseDto dashboard(String userId);
}
