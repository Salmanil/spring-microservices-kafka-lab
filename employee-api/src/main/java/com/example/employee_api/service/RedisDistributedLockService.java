package com.example.employee_api.service;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
public class RedisDistributedLockService {

    private static final String RELEASE_LOCK_SCRIPT = """
            if redis.call('get', KEYS[1]) == ARGV[1] then
              return redis.call('del', KEYS[1])
            end
            return 0
            """;

    private final StringRedisTemplate stringRedisTemplate;
    private final DefaultRedisScript<Long> releaseLockScript;

    public RedisDistributedLockService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.releaseLockScript = new DefaultRedisScript<>(RELEASE_LOCK_SCRIPT, Long.class);
    }

    public Map<String, Object> acquireLock(String lockName, long ttlSeconds) {
        String lockKey = lockKey(lockName);
        String ownerToken = UUID.randomUUID().toString();
        Boolean acquired = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, ownerToken, Duration.ofSeconds(ttlSeconds));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("lockName", lockName);
        response.put("lockKey", lockKey);
        response.put("ownerToken", ownerToken);
        response.put("acquired", Boolean.TRUE.equals(acquired));
        response.put("ttlSeconds", stringRedisTemplate.getExpire(lockKey));
        response.put("timestamp", Instant.now().toString());
        return response;
    }

    public Map<String, Object> releaseLock(String lockName, String ownerToken) {
        String lockKey = lockKey(lockName);
        Long released = stringRedisTemplate.execute(releaseLockScript, java.util.List.of(lockKey), ownerToken);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("lockName", lockName);
        response.put("lockKey", lockKey);
        response.put("ownerToken", ownerToken);
        response.put("released", released != null && released > 0);
        response.put("ttlSeconds", stringRedisTemplate.getExpire(lockKey));
        response.put("timestamp", Instant.now().toString());
        return response;
    }

    public Map<String, Object> inspectLock(String lockName) {
        String lockKey = lockKey(lockName);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("lockName", lockName);
        response.put("lockKey", lockKey);
        response.put("ownerToken", stringRedisTemplate.opsForValue().get(lockKey));
        response.put("ttlSeconds", stringRedisTemplate.getExpire(lockKey));
        response.put("locked", Boolean.TRUE.equals(stringRedisTemplate.hasKey(lockKey)));
        return response;
    }

    public Map<String, Object> simulateCriticalSection(String lockName, long ttlSeconds, long holdMs) {
        Map<String, Object> lock = acquireLock(lockName, ttlSeconds);
        if (!Boolean.TRUE.equals(lock.get("acquired"))) {
            return lock;
        }

        String ownerToken = (String) lock.get("ownerToken");
        try {
            Thread.sleep(holdMs);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while holding Redis lock", interruptedException);
        } finally {
            releaseLock(lockName, ownerToken);
        }

        Map<String, Object> response = new LinkedHashMap<>(lock);
        response.put("heldMs", holdMs);
        response.put("released", true);
        return response;
    }

    private String lockKey(String lockName) {
        return "lock:" + lockName;
    }
}
