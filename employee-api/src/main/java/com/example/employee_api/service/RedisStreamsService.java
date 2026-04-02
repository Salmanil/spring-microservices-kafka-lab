package com.example.employee_api.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisStreamsService {

    private static final String STREAM_KEY = "stream:employee-events";

    private final StringRedisTemplate stringRedisTemplate;

    public RedisStreamsService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public Map<String, Object> append(String eventType, String employeeId, String payload) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("eventType", eventType);
        fields.put("employeeId", employeeId);
        fields.put("payload", payload);
        fields.put("createdAt", Instant.now().toString());

        RecordId recordId = stringRedisTemplate.opsForStream().add(STREAM_KEY, fields);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("streamKey", STREAM_KEY);
        response.put("recordId", recordId == null ? null : recordId.getValue());
        response.put("fields", fields);
        return response;
    }

    public List<Map<String, Object>> readLatest(int count) {
        List<MapRecord<String, Object, Object>> records = stringRedisTemplate.opsForStream()
                .reverseRange(STREAM_KEY, Range.unbounded(), Limit.limit().count(count));
        return toResponse(records);
    }

    public Map<String, Object> createConsumerGroup(String groupName) {
        try {
            stringRedisTemplate.opsForStream().createGroup(STREAM_KEY, ReadOffset.from("0-0"), groupName);
            return Map.of("streamKey", STREAM_KEY, "groupName", groupName, "created", true);
        } catch (Exception exception) {
            return Map.of("streamKey", STREAM_KEY, "groupName", groupName, "created", false,
                    "message", exception.getMessage());
        }
    }

    public List<Map<String, Object>> readWithConsumerGroup(String groupName, String consumerName, int count) {
        List<MapRecord<String, Object, Object>> records = stringRedisTemplate.opsForStream().read(
                Consumer.from(groupName, consumerName),
                StreamReadOptions.empty().count(count),
                StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed()));
        return toResponse(records);
    }

    private List<Map<String, Object>> toResponse(List<MapRecord<String, Object, Object>> records) {
        List<Map<String, Object>> response = new ArrayList<>();
        for (MapRecord<String, Object, Object> record : records) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("streamKey", record.getStream());
            entry.put("recordId", record.getId().getValue());
            entry.put("fields", record.getValue());
            response.add(entry);
        }
        return response;
    }
}
