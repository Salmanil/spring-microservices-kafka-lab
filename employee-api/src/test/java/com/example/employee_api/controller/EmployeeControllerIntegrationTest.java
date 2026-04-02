package com.example.employee_api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.employee_api.entity.Employee;
import com.example.employee_api.repository.AppUserRepository;
import com.example.employee_api.security.AppUserDetailsService;
import com.example.employee_api.security.JwtAuthenticationFilter;
import com.example.employee_api.security.JwtService;
import com.example.employee_api.service.AuthService;
import com.example.employee_api.service.EmployeeService;
import com.example.employee_api.service.KafkaProducerService;
import com.example.employee_api.service.NotificationResilienceService;
import com.example.employee_api.service.RedisDistributedLockService;
import com.example.employee_api.service.RedisLearningService;
import com.example.employee_api.service.RedisPubSubStore;
import com.example.employee_api.service.RedisPubSubSubscriber;
import com.example.employee_api.service.RedisStreamsService;
import com.example.employee_api.service.TokenBlacklistService;

@WebMvcTest(EmployeeController.class)
@Import(ApiExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class EmployeeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private KafkaProducerService kafkaProducerService;

    @MockBean
    private NotificationResilienceService notificationResilienceService;

    @MockBean
    private AuthService authService;

    @MockBean
    private RedisLearningService redisLearningService;

    @MockBean
    private RedisStreamsService redisStreamsService;

    @MockBean
    private RedisDistributedLockService redisDistributedLockService;

    @MockBean
    private RedisPubSubStore redisPubSubStore;

    @MockBean
    private RedisPubSubSubscriber redisPubSubSubscriber;

    @MockBean
    private RedisConnectionFactory redisConnectionFactory;

    @MockBean
    private RedisMessageListenerContainer redisMessageListenerContainer;

    @MockBean
    private AppUserRepository appUserRepository;

    @MockBean
    private AppUserDetailsService appUserDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    @Test
    void insertEmployeeShouldReturnCreatedEmployee() throws Exception {
        Employee savedEmployee = new Employee();
        savedEmployee.setEmpId(9911);
        savedEmployee.setFirstName("Salma");
        savedEmployee.setLastName("Create");
        savedEmployee.setDeptId(10);
        savedEmployee.setManagerId(100);
        savedEmployee.setHireDate("2026-03-28");
        savedEmployee.setSalary(9500.0);
        savedEmployee.setVersion(0);

        when(employeeService.createEmp(any(Employee.class))).thenReturn(savedEmployee);

        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "empId": 9911,
                                  "firstName": "Salma",
                                  "lastName": "Create",
                                  "deptId": 10,
                                  "managerId": 100,
                                  "hireDate": "2026-03-28",
                                  "salary": 9500
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Intercepted-By", "employee-api-request-interceptor"))
                .andExpect(jsonPath("$.empId").value(9911))
                .andExpect(jsonPath("$.firstName").value("Salma"))
                .andExpect(jsonPath("$.salary").value(9500.0));

        verify(employeeService).createEmp(any(Employee.class));
    }

    @Test
    void insertEmployeeShouldRejectInvalidPayload() throws Exception {
        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "empId": 9912,
                                  "firstName": "",
                                  "lastName": "Bad",
                                  "deptId": 10,
                                  "managerId": 100,
                                  "hireDate": "28-03-2026",
                                  "salary": -1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("X-Intercepted-By", "employee-api-request-interceptor"))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors.firstName").exists())
                .andExpect(jsonPath("$.fieldErrors.hireDate").exists())
                .andExpect(jsonPath("$.fieldErrors.salary").exists());
    }
}
