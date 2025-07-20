package com.user.user.service.impl;

import java.time.LocalDateTime;

import com.google.gson.Gson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.user.user.dto.LogMessage;

@Service
public class KafkaLogger {

    @Autowired
    private KafkaTemplate<String, LogMessage> logMessageKafkaTemplate;

    public void log(Object body, String type) {
        LogMessage log = new LogMessage();
        log.setMessage(new Gson().toJson(body));
        log.setMessageType(type);
        log.setDateTime(LocalDateTime.now().toString());

        logMessageKafkaTemplate.send("logging-topic", log);
    }
}
