// package com.example.employee_api.service;

// import org.springframework.kafka.annotation.KafkaListener;
// import org.springframework.stereotype.Service;

// import jakarta.annotation.PostConstruct;

// @Service
// public class KafkaConsumerService {

//     @PostConstruct
// public void init() {
//     System.out.println("🔥 Consumer bean loaded!");
// }


// @KafkaListener(
//     topics = "test-topic",
//     groupId = "employee-group-FIXED",
//     containerFactory = "kafkaListenerContainerFactory"
// )
// public void consume(String message) {
//     if (message == null || message.trim().isEmpty()) {
//         System.out.println("⚠️ Received empty message");
//         return;
//     }
//     System.out.println("🔥🔥🔥 RECEIVED: " + message);
// }


// }

