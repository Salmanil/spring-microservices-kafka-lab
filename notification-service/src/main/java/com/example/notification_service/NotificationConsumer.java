package com.example.notification_service;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.example.notification_service.model.EmployeeEvent;
import com.example.notification_service.model.FailedKafkaMessage;
import com.example.notification_service.service.FailedMessageStore;
import com.example.notification_service.service.NotificationEventStore;

import jakarta.annotation.PostConstruct;

@Component
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    @Value("${app.kafka.topic.employee-events}")
    private String topicName;

    private final FailedMessageStore failedMessageStore;

    public NotificationConsumer(FailedMessageStore failedMessageStore) {
        this.failedMessageStore = failedMessageStore;
    }

    @PostConstruct
    public void init() {
        log.info("Notification service started and waiting for topic {}", topicName);
    }

    @KafkaListener(
            topics = "${app.kafka.topic.error-events}",
            groupId = "${app.kafka.topic.error-events}-monitor",
            containerFactory = "deadLetterKafkaListenerContainerFactory")
    public void consumeFailedMessage(
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(value = KafkaHeaders.DLT_EXCEPTION_FQCN, required = false) String exceptionClass,
            @Header(value = KafkaHeaders.DLT_EXCEPTION_MESSAGE, required = false) String exceptionMessage,
            @Header(value = KafkaHeaders.DLT_ORIGINAL_TOPIC, required = false) String originalTopic,
            @Header(value = KafkaHeaders.DLT_ORIGINAL_PARTITION, required = false) Integer originalPartition,
            @Header(value = KafkaHeaders.DLT_ORIGINAL_OFFSET, required = false) Long originalOffset,
            byte[] payload) {
        FailedKafkaMessage failedMessage = new FailedKafkaMessage(
                key,
                payload == null ? null : new String(payload, StandardCharsets.UTF_8),
                exceptionClass,
                exceptionMessage,
                originalTopic,
                originalPartition,
                originalOffset);
        failedMessageStore.add(failedMessage);
        log.warn("Moved failed Kafka record to error topic key={} originalTopic={} partition={} offset={} reason={}",
                key, originalTopic, originalPartition, originalOffset, exceptionMessage);
    }
}

@Component
@KafkaListener(
        id = "notification-consumer-1",
        topics = "${app.kafka.topic.employee-events}",
        groupId = "${spring.kafka.consumer.group-id}",
        clientIdPrefix = "notification-consumer-1",
        containerFactory = "kafkaListenerContainerFactory")
class NotificationConsumerOne {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumerOne.class);

    private final NotificationEventStore notificationEventStore;

    NotificationConsumerOne(NotificationEventStore notificationEventStore) {
        this.notificationEventStore = notificationEventStore;
    }

    @KafkaHandler
    public void handleEmployeeEvent(
            EmployeeEvent message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        notificationEventStore.add(message);
        log.info("consumer-1 received empId={} partition={} offset={}", message.getEmpId(), partition, offset);
    }

    @KafkaHandler(isDefault = true)
    public void handleUnexpectedPayload(Object payload) {
        log.warn("consumer-1 received unexpected payload type={}", payload == null ? "null" : payload.getClass().getName());
    }
}

@Component
@KafkaListener(
        id = "notification-consumer-2",
        topics = "${app.kafka.topic.employee-events}",
        groupId = "${spring.kafka.consumer.group-id}",
        clientIdPrefix = "notification-consumer-2",
        containerFactory = "kafkaListenerContainerFactory")
class NotificationConsumerTwo {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumerTwo.class);

    private final NotificationEventStore notificationEventStore;

    NotificationConsumerTwo(NotificationEventStore notificationEventStore) {
        this.notificationEventStore = notificationEventStore;
    }

    @KafkaHandler
    public void handleEmployeeEvent(
            EmployeeEvent message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        notificationEventStore.add(message);
        log.info("consumer-2 received empId={} partition={} offset={}", message.getEmpId(), partition, offset);
    }

    @KafkaHandler(isDefault = true)
    public void handleUnexpectedPayload(Object payload) {
        log.warn("consumer-2 received unexpected payload type={}", payload == null ? "null" : payload.getClass().getName());
    }
}
