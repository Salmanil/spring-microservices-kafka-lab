package com.example.employee_api.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic employeeEventsTopic(
            @Value("${app.kafka.topic.employee-events}") String topicName,
            @Value("${app.kafka.topic.partitions}") int partitions,
            @Value("${app.kafka.topic.replicas}") short replicas) {
        return TopicBuilder.name(topicName)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }
}
