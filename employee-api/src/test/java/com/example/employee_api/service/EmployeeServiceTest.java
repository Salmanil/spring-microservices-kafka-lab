package com.example.employee_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.employee_api.entity.Employee;
import com.example.employee_api.repository.EmployeeRepository;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private KafkaProducerService kafkaProducerService;

    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService(employeeRepository, kafkaProducerService);
    }

    @Test
    void createEmpShouldSaveAndPublishCreatedEvent() {
        Employee employee = buildEmployee(9702, "Salma", "Test", 10, 100, "2026-03-27", 9500.0, 0);

        when(employeeRepository.existsById(9702)).thenReturn(false);
        when(employeeRepository.save(employee)).thenReturn(employee);

        Employee saved = employeeService.createEmp(employee);

        assertEquals(9702, saved.getEmpId());
        verify(employeeRepository).save(employee);
        verify(kafkaProducerService).sendEmployeeEvent("CREATED", employee);
    }

    @Test
    void createEmpShouldRejectDuplicateEmployeeId() {
        Employee employee = buildEmployee(9702, "Salma", "Duplicate", 10, 100, "2026-03-27", 9500.0, 0);

        when(employeeRepository.existsById(9702)).thenReturn(true);

        EmployeeAlreadyExistsException exception = assertThrows(
                EmployeeAlreadyExistsException.class,
                () -> employeeService.createEmp(employee));

        assertTrue(exception.getMessage().contains("9702"));
        verify(employeeRepository, never()).save(employee);
        verify(kafkaProducerService, never()).sendEmployeeEvent("CREATED", employee);
    }

    @Test
    void updateEmpShouldRejectStaleVersion() {
        Employee existing = buildEmployee(9702, "Salma", "Current", 10, 100, "2026-03-27", 9500.0, 2);
        Employee request = buildEmployee(9702, "Salma", "Updated", 12, 101, "2026-03-27", 9800.0, 1);

        when(employeeRepository.findById(9702)).thenReturn(Optional.of(existing));

        StaleEmployeeUpdateException exception = assertThrows(
                StaleEmployeeUpdateException.class,
                () -> employeeService.updatEmp(9702, request));

        assertTrue(exception.getMessage().contains("stale"));
        verify(employeeRepository, never()).save(existing);
        verify(kafkaProducerService, never()).sendEmployeeEvent("UPDATED", existing);
    }

    private Employee buildEmployee(int empId,
            String firstName,
            String lastName,
            int deptId,
            int managerId,
            String hireDate,
            double salary,
            Integer version) {
        Employee employee = new Employee();
        employee.setEmpId(empId);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setDeptId(deptId);
        employee.setManagerId(managerId);
        employee.setHireDate(hireDate);
        employee.setSalary(salary);
        employee.setVersion(version);
        return employee;
    }
}
