package com.example.employee_api.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;

@Service
public class NotificationResilienceService {

    private final RestClient restClient;
    private final String notificationServiceBaseUrl;
    private final long defaultDelayMs;

    public NotificationResilienceService(
            RestClient.Builder loadBalancedRestClientBuilder,
            @Value("${app.notification-service.base-url}") String notificationServiceBaseUrl,
            @Value("${app.notification-service.slow-call-default-delay-ms}") long defaultDelayMs) {
        this.restClient = loadBalancedRestClientBuilder.build();
        this.notificationServiceBaseUrl = notificationServiceBaseUrl;
        this.defaultDelayMs = defaultDelayMs;
    }

    @Retry(name = "notificationService", fallbackMethod = "notificationFallback")
    @CircuitBreaker(name = "notificationService", fallbackMethod = "notificationFallback")
    @TimeLimiter(name = "notificationService", fallbackMethod = "notificationFallback")
    public CompletableFuture<Map<String, Object>> probeNotificationService(String mode, Long delayMs) {
        return CompletableFuture.supplyAsync(() -> invokeNotificationDemo(mode, delayMs));
    }

    private Map<String, Object> invokeNotificationDemo(String mode, Long delayMs) {
        String normalizedMode = mode == null ? "ok" : mode.toLowerCase();
        String path = switch (normalizedMode) {
            case "slow" -> "/notifications/demo/slow?delayMs=" + (delayMs == null ? defaultDelayMs : delayMs);
            case "fail" -> "/notifications/demo/fail";
            default -> "/notifications/demo/ok";
        };

        return restClient.get()
                .uri(notificationServiceBaseUrl + path)
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        (request, response) -> {
                            throw new IllegalStateException(
                                    "Notification service returned " + response.getStatusCode().value());
                        })
                .body(Map.class);
    }

    public CompletableFuture<Map<String, Object>> notificationFallback(String mode, Long delayMs, Throwable throwable) {
        Map<String, Object> fallback = new LinkedHashMap<>();
        fallback.put("status", "FALLBACK");
        fallback.put("mode", mode == null ? "ok" : mode.toLowerCase());
        fallback.put("delayMs", delayMs == null ? defaultDelayMs : delayMs);
        fallback.put("reason", throwable.getClass().getSimpleName());
        fallback.put("message", throwable.getMessage());
        fallback.put("service", "employee-api");
        return CompletableFuture.completedFuture(fallback);
    }
}
