package com.example.employee_api.service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.example.employee_api.entity.Employee;
import com.example.employee_api.model.EmployeeEvent;

@Service
public class KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);

    private final KafkaTemplate<String, EmployeeEvent> kafkaTemplate;
    private final KafkaTemplate<String, Object> genericSchemaKafkaTemplate;
    private final KafkaTemplate<String, String> rawKafkaTemplate;
    private final String topicName;

    public KafkaProducerService(
            KafkaTemplate<String, EmployeeEvent> kafkaTemplate,
            KafkaTemplate<String, Object> genericSchemaKafkaTemplate,
            KafkaTemplate<String, String> rawKafkaTemplate,
            @Value("${app.kafka.topic.employee-events}") String topicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.genericSchemaKafkaTemplate = genericSchemaKafkaTemplate;
        this.rawKafkaTemplate = rawKafkaTemplate;
        this.topicName = topicName;
    }

    public void sendMessage(String key, EmployeeEvent message) {
        kafkaTemplate.send(topicName, key, normalize(message))
                .whenComplete((SendResult<String, EmployeeEvent> result, Throwable ex) -> logSendResult(result, ex, key, message));
    }

    public void sendMessageToPartition(int partition, String key, EmployeeEvent message) {
        kafkaTemplate.send(topicName, partition, key, normalize(message))
                .whenComplete((SendResult<String, EmployeeEvent> result, Throwable ex) -> logSendResult(result, ex, key, message));
    }

    public void sendEmployeeEvent(String action, Employee employee) {
        String key = "emp-" + employee.getEmpId();
        EmployeeEvent payload = new EmployeeEvent(
                action,
                employee.getEmpId(),
                safe(employee.getFirstName()) + " " + safe(employee.getLastName()),
                employee.getDeptId(),
                employee.getSalary() == null ? null : employee.getSalary().doubleValue(),
                Instant.now().toString());
        sendMessage(key, payload);
    }

    public void sendRawMessage(String key, String payload) {
        rawKafkaTemplate.send(topicName, key, payload)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish raw record with key {}", key, ex);
                        return;
                    }

                    var metadata = result.getRecordMetadata();
                    log.warn("Produced RAW message to topic={} partition={} offset={} key={} payload={}",
                            metadata.topic(), metadata.partition(), metadata.offset(), key, payload);
                });
    }

    public void sendGenericSchemaMessage(String key, Map<String, Object> payload) {
        normalize(payload);
        try {
            SendResult<String, Object> result = genericSchemaKafkaTemplate.send(topicName, key, payload)
                    .get(10, TimeUnit.SECONDS);
            var metadata = result.getRecordMetadata();
            log.info("Produced generic schema message to topic={} partition={} offset={} key={} payload={}",
                    metadata.topic(), metadata.partition(), metadata.offset(), key, payload);
        } catch (Exception exception) {
            throw new InvalidSchemaPayloadException("Payload does not match the registered employee event schema", exception);
        }
    }

    private void logSendResult(SendResult<String, EmployeeEvent> result, Throwable ex, String key, EmployeeEvent message) {
        if (ex != null) {
            log.error("Failed to publish record with key {}", key, ex);
            return;
        }

        var metadata = result.getRecordMetadata();
        log.info(
                "Produced message to topic={} partition={} offset={} key={} payload={}",
                metadata.topic(),
                metadata.partition(),
                metadata.offset(),
                key,
                message);
    }

    private String safe(String value) {
        return value == null ? "" : value.replace("\"", "'");
    }

    private EmployeeEvent normalize(EmployeeEvent event) {
        if (event.getEventTimestamp() == null || event.getEventTimestamp().isBlank()) {
            event.setEventTimestamp(Instant.now().toString());
        }
        return event;
    }

    private void normalize(Map<String, Object> payload) {
        Object timestamp = payload.get("eventTimestamp");
        if (timestamp == null || String.valueOf(timestamp).isBlank()) {
            payload.put("eventTimestamp", Instant.now().toString());
        }
    }
}
