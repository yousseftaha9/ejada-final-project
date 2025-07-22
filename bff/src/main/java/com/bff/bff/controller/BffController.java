package com.bff.bff.controller;

import com.bff.bff.dto.DashboardResponseDto;
import com.bff.bff.service.interfaces.BffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/bff")
public class BffController {
    @Autowired
    private BffService bffService;

    @GetMapping("dashboard/{userId}")
    public ResponseEntity<?> dashboard(@PathVariable String userId){
        return bffService.dashboard(userId);
    }
}
