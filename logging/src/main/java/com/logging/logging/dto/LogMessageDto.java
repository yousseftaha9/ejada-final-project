package com.logging.logging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogMessageDto {
    private String message;
    private String messageType;
    private String dateTime;
} 