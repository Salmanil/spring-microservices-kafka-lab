package com.example.employee_api.service;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisLearningService {

    private final StringRedisTemplate stringRedisTemplate;

    public RedisLearningService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public Map<String, Object> putValue(String key, String value, long ttlSeconds) {
        stringRedisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttlSeconds));
        return getValue(key);
    }

    public Map<String, Object> getValue(String key) {
        String value = stringRedisTemplate.opsForValue().get(key);
        Long ttlSeconds = stringRedisTemplate.getExpire(key);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("key", key);
        response.put("value", value);
        response.put("ttlSeconds", ttlSeconds);
        response.put("exists", value != null);
        return response;
    }

    public Map<String, Object> incrementCounter(String key, long ttlSeconds) {
        Long value = stringRedisTemplate.opsForValue().increment(key);
        if (ttlSeconds > 0 && value != null && value == 1L) {
            stringRedisTemplate.expire(key, Duration.ofSeconds(ttlSeconds));
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("key", key);
        response.put("value", value);
        response.put("ttlSeconds", stringRedisTemplate.getExpire(key));
        return response;
    }

    public void deleteKey(String key) {
        stringRedisTemplate.delete(key);
    }

    public Map<String, Object> publishMessage(String channel, String payload) {
        Long subscribers = stringRedisTemplate.convertAndSend(channel, payload);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("channel", channel);
        response.put("payload", payload);
        response.put("subscribers", subscribers);
        response.put("publishedAt", Instant.now().toString());
        return response;
    }
}
