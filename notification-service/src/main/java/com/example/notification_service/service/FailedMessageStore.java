package com.example.notification_service.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.stereotype.Service;

import com.example.notification_service.model.FailedKafkaMessage;

@Service
public class FailedMessageStore {

    private static final int MAX_MESSAGES = 20;

    private final ConcurrentLinkedDeque<FailedKafkaMessage> failedMessages = new ConcurrentLinkedDeque<>();

    public void add(FailedKafkaMessage message) {
        failedMessages.addFirst(message);
        while (failedMessages.size() > MAX_MESSAGES) {
            failedMessages.removeLast();
        }
    }

    public List<FailedKafkaMessage> recentMessages() {
        return new ArrayList<>(failedMessages);
    }
}
