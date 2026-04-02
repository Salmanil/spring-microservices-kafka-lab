package com.example.employee_api.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.employee_api.service.RedisDistributedLockService;
import com.example.employee_api.service.RedisPubSubStore;
import com.example.employee_api.service.RedisLearningService;
import com.example.employee_api.service.RedisStreamsService;

@RestController
@RequestMapping("/employees/redis")
public class RedisLearningController {

    private final RedisLearningService redisLearningService;
    private final RedisPubSubStore redisPubSubStore;
    private final RedisStreamsService redisStreamsService;
    private final RedisDistributedLockService redisDistributedLockService;

    public RedisLearningController(
            RedisLearningService redisLearningService,
            RedisPubSubStore redisPubSubStore,
            RedisStreamsService redisStreamsService,
            RedisDistributedLockService redisDistributedLockService) {
        this.redisLearningService = redisLearningService;
        this.redisPubSubStore = redisPubSubStore;
        this.redisStreamsService = redisStreamsService;
        this.redisDistributedLockService = redisDistributedLockService;
    }

    @PostMapping("/values")
    public Map<String, Object> putValue(
            @RequestParam String key,
            @RequestParam String value,
            @RequestParam(defaultValue = "60") long ttlSeconds) {
        return redisLearningService.putValue(key, value, ttlSeconds);
    }

    @GetMapping("/values/{key}")
    public Map<String, Object> getValue(@PathVariable String key) {
        return redisLearningService.getValue(key);
    }

    @PostMapping("/counters/{key}/increment")
    public Map<String, Object> incrementCounter(
            @PathVariable String key,
            @RequestParam(defaultValue = "60") long ttlSeconds) {
        return redisLearningService.incrementCounter(key, ttlSeconds);
    }

    @DeleteMapping("/values/{key}")
    public void deleteValue(@PathVariable String key) {
        redisLearningService.deleteKey(key);
    }

    @PostMapping("/pubsub/publish")
    public Map<String, Object> publish(
            @RequestParam String channel,
            @RequestParam String payload) {
        return redisLearningService.publishMessage(channel, payload);
    }

    @GetMapping("/pubsub/messages")
    public java.util.List<Map<String, Object>> recentPubSubMessages() {
        return redisPubSubStore.recentMessages();
    }

    @PostMapping("/streams/events")
    public Map<String, Object> appendToStream(
            @RequestParam String eventType,
            @RequestParam String employeeId,
            @RequestParam String payload) {
        return redisStreamsService.append(eventType, employeeId, payload);
    }

    @GetMapping("/streams/events")
    public java.util.List<Map<String, Object>> readLatestStreamEvents(
            @RequestParam(defaultValue = "10") int count) {
        return redisStreamsService.readLatest(count);
    }

    @PostMapping("/streams/groups/{groupName}")
    public Map<String, Object> createConsumerGroup(@PathVariable String groupName) {
        return redisStreamsService.createConsumerGroup(groupName);
    }

    @GetMapping("/streams/groups/{groupName}/consumers/{consumerName}")
    public java.util.List<Map<String, Object>> readFromConsumerGroup(
            @PathVariable String groupName,
            @PathVariable String consumerName,
            @RequestParam(defaultValue = "5") int count) {
        return redisStreamsService.readWithConsumerGroup(groupName, consumerName, count);
    }

    @PostMapping("/locks/{lockName}/acquire")
    public Map<String, Object> acquireLock(
            @PathVariable String lockName,
            @RequestParam(defaultValue = "30") long ttlSeconds) {
        return redisDistributedLockService.acquireLock(lockName, ttlSeconds);
    }

    @GetMapping("/locks/{lockName}")
    public Map<String, Object> inspectLock(@PathVariable String lockName) {
        return redisDistributedLockService.inspectLock(lockName);
    }

    @PostMapping("/locks/{lockName}/release")
    public Map<String, Object> releaseLock(
            @PathVariable String lockName,
            @RequestParam String ownerToken) {
        return redisDistributedLockService.releaseLock(lockName, ownerToken);
    }

    @PostMapping("/locks/{lockName}/critical-section")
    public Map<String, Object> simulateCriticalSection(
            @PathVariable String lockName,
            @RequestParam(defaultValue = "30") long ttlSeconds,
            @RequestParam(defaultValue = "3000") long holdMs) {
        return redisDistributedLockService.simulateCriticalSection(lockName, ttlSeconds, holdMs);
    }
}
