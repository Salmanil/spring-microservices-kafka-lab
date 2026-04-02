package com.example.employee_api.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.test.web.servlet.MockMvc;

import com.example.employee_api.entity.Employee;
import com.example.employee_api.service.EmployeeService;

@SpringBootTest(properties = {
        "eureka.client.enabled=false"
})
@AutoConfigureMockMvc
class EmployeeSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RedisMessageListenerContainer redisMessageListenerContainer;

    @MockBean
    private EmployeeService employeeService;

    @Test
    void protectedEmployeeEndpointShouldRejectUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/employees/9702"))
                .andExpect(status().isForbidden());
    }

    @Test
    void protectedEmployeeEndpointShouldAllowAuthenticatedRequest() throws Exception {
        Employee employee = new Employee();
        employee.setEmpId(9702);
        employee.setFirstName("Salma");
        employee.setLastName("Secure");
        employee.setDeptId(10);
        employee.setManagerId(100);
        employee.setHireDate("2026-03-28");
        employee.setSalary(9800.0);
        employee.setVersion(1);

        when(employeeService.getByid(9702)).thenReturn(employee);

        mockMvc.perform(get("/employees/9702")
                        .with(user("salmauser").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empId").value(9702))
                .andExpect(jsonPath("$.firstName").value("Salma"));
    }
}
