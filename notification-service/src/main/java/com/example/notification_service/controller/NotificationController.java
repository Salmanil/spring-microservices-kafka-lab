package com.example.notification_service.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.notification_service.model.EmployeeEvent;
import com.example.notification_service.model.FailedKafkaMessage;
import com.example.notification_service.service.FailedMessageStore;
import com.example.notification_service.service.NotificationEventStore;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationEventStore notificationEventStore;
    private final FailedMessageStore failedMessageStore;
    private final long defaultDelayMs;

    public NotificationController(
            NotificationEventStore notificationEventStore,
            FailedMessageStore failedMessageStore,
            @org.springframework.beans.factory.annotation.Value("${app.notification.demo.default-delay-ms}") long defaultDelayMs) {
        this.notificationEventStore = notificationEventStore;
        this.failedMessageStore = failedMessageStore;
        this.defaultDelayMs = defaultDelayMs;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("service", "notification-service", "status", "UP");
    }

    @GetMapping("/events")
    public List<EmployeeEvent> recentEvents() {
        return notificationEventStore.recentEvents();
    }

    @GetMapping("/errors")
    public List<FailedKafkaMessage> recentFailedEvents() {
        return failedMessageStore.recentMessages();
    }

    @GetMapping("/demo/ok")
    public Map<String, Object> demoOk() {
        return Map.of("status", "UP", "service", "notification-service", "mode", "ok");
    }

    @GetMapping("/demo/slow")
    public Map<String, Object> demoSlow(@RequestParam(required = false) Long delayMs) throws InterruptedException {
        long effectiveDelay = delayMs == null ? defaultDelayMs : delayMs;
        Thread.sleep(effectiveDelay);
        return Map.of("status", "UP", "service", "notification-service", "mode", "slow", "delayMs", effectiveDelay);
    }

    @GetMapping("/demo/fail")
    public Map<String, Object> demoFail() {
        throw new IllegalStateException("Simulated notification-service failure");
    }
}
