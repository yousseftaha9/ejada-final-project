package com.logging.logging.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logging.logging.entity.Logging;
import com.logging.logging.entity.MessageType;
import com.logging.logging.repository.LoggingRepository;
import com.logging.logging.dto.LogMessage;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class LoggingServiceImpl {

    @Autowired
    private LoggingRepository loggingRepository;

    @KafkaListener(topics = "logging-topic", groupId = "logging-group")
    public void consume(String messageJson) {
        try {
            System.out.println("Received message: " + messageJson);
            
            ObjectMapper objectMapper = new ObjectMapper();
            LogMessage logMessage = objectMapper.readValue(messageJson, LogMessage.class);
            
            Logging logging = new Logging();
            logging.setId(UUID.randomUUID().toString());
            logging.setMessage(logMessage.getMessage());
            logging.setMessageType(MessageType.valueOf(logMessage.getMessageType()));
            logging.setDateTime(LocalDateTime.parse(logMessage.getDateTime()));
            
            Logging savedLog = loggingRepository.save(logging);
            System.out.println("Log saved with ID: " + savedLog.getId());
        } catch (Exception e) {
            System.err.println("Error processing log message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
