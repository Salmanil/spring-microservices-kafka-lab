package com.example.employee_api.controller;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.employee_api.entity.Employee;
import com.example.employee_api.model.EmployeeEvent;
import com.example.employee_api.service.EmployeeService;
import com.example.employee_api.service.KafkaProducerService;
import com.example.employee_api.service.NotificationResilienceService;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    private final KafkaProducerService producerService;
    private final EmployeeService employeeService;
    private final NotificationResilienceService notificationResilienceService;

    public EmployeeController(
            KafkaProducerService producerService,
            EmployeeService employeeService,
            NotificationResilienceService notificationResilienceService) {
        this.producerService = producerService;
        this.employeeService = employeeService;
        this.notificationResilienceService = notificationResilienceService;
    }

    @GetMapping()
    public List<Employee> fetchEmployees(){
        return employeeService.getEmp();
    }

    @GetMapping("/{id}")
    public Employee fetchById(@PathVariable int id){
        return employeeService.getByid(id);
    }

    @PostMapping
    public Employee insertEmp(@Valid @RequestBody Employee employee){
        return employeeService.createEmp(employee);
    }

    @PutMapping("/{id}")
    public Employee updateEmployee(@PathVariable int id, @Valid @RequestBody Employee emp){
        return employeeService.updatEmp(id,emp);
    }

    @DeleteMapping("/{id}")
    public String removeEmp(@PathVariable int id){
        return employeeService.deleteEmp(id);
    }

    @PostMapping("/send")
    public String send(
            @RequestBody EmployeeEvent message,
            @RequestParam(required = false, defaultValue = "manual-demo") String key) {
        producerService.sendMessage(key, message);
        return "Message sent to Kafka topic";
    }

    @PostMapping("/send/schema-check")
    public String sendSchemaChecked(
            @RequestBody Map<String, Object> payload,
            @RequestParam(required = false, defaultValue = "schema-check") String key) {
        producerService.sendGenericSchemaMessage(key, payload);
        return "Schema-valid message sent to Kafka topic";
    }

    @PostMapping("/send/raw")
    public String sendRaw(
            @RequestParam(required = false, defaultValue = "raw-demo") String key,
            @RequestBody String message) {
        producerService.sendRawMessage(key, message);
        return "Raw message sent to Kafka topic";
    }

    @PostMapping("/send/demo")
    public String sendDemo(@RequestParam(defaultValue = "6") int count) {
        for (int i = 0; i < count; i++) {
            int partition = i % 3;
            int employeeId = 1000 + i + 1;
            String key = "demo-emp-" + employeeId;
            EmployeeEvent message = new EmployeeEvent(
                    "DEMO",
                    employeeId,
                    "Demo Employee " + (i + 1),
                    partition + 10,
                    3500.0 + (i * 100),
                    null);
            producerService.sendMessageToPartition(partition, key, message);
        }
        return count + " demo messages sent across 3 partitions";
    }

    @Value("${spring.kafka.bootstrap-servers}")
    private String servers;

    @GetMapping("/check")
    public String check() {
        return servers;
    }

    @GetMapping("/resilience/notifications")
    public CompletableFuture<Map<String, Object>> testNotificationResilience(
            @RequestParam(defaultValue = "ok") String mode,
            @RequestParam(required = false) Long delayMs) {
        return notificationResilienceService.probeNotificationService(mode, delayMs);
    }
}
