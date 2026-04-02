package com.example.employee_api.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;

@Component
public class RedisPubSubStore {

    private final CopyOnWriteArrayList<Map<String, Object>> messages = new CopyOnWriteArrayList<>();

    public void record(String channel, String payload) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("channel", channel);
        entry.put("payload", payload);
        entry.put("receivedAt", Instant.now().toString());
        messages.add(0, entry);
        while (messages.size() > 20) {
            messages.remove(messages.size() - 1);
        }
    }

    public List<Map<String, Object>> recentMessages() {
        return new ArrayList<>(messages);
    }
}
