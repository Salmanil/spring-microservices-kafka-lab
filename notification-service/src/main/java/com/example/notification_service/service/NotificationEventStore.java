package com.example.notification_service.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.stereotype.Service;

import com.example.notification_service.model.EmployeeEvent;

@Service
public class NotificationEventStore {

    private static final int MAX_EVENTS = 20;

    private final ConcurrentLinkedDeque<EmployeeEvent> events = new ConcurrentLinkedDeque<>();

    public void add(EmployeeEvent event) {
        events.addFirst(event);
        while (events.size() > MAX_EVENTS) {
            events.removeLast();
        }
    }

    public List<EmployeeEvent> recentEvents() {
        return new ArrayList<>(events);
    }
}
