package com.example.employee_api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

import com.example.employee_api.entity.Employee;
import com.example.employee_api.repository.EmployeeRepository;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final KafkaProducerService kafkaProducerService;

    public EmployeeService(EmployeeRepository employeeRepository, KafkaProducerService kafkaProducerService){
        this.employeeRepository=employeeRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    @Cacheable(cacheNames = "employeeList")
    public List<Employee> getEmp(){
        return employeeRepository.findAll();
    }

    @Cacheable(cacheNames = "employeeById", key = "#id")
    public Employee getByid(Integer id){
        return employeeRepository.findById(id).orElse(null);
    }

    @Transactional
    @Caching(
            put = @CachePut(cacheNames = "employeeById", key = "#result.empId", unless = "#result == null"),
            evict = @CacheEvict(cacheNames = "employeeList", allEntries = true))
    public Employee createEmp(Employee emp){
        if (employeeRepository.existsById(emp.getEmpId())) {
            throw new EmployeeAlreadyExistsException(emp.getEmpId());
        }
        Employee savedEmployee = employeeRepository.save(emp);
        kafkaProducerService.sendEmployeeEvent("CREATED", savedEmployee);
        return savedEmployee;
    }

    @Transactional
    @Caching(
            put = @CachePut(cacheNames = "employeeById", key = "#id", unless = "#result == null"),
            evict = @CacheEvict(cacheNames = "employeeList", allEntries = true))
    public Employee updatEmp(Integer id, Employee newEmp){
        Employee existing = employeeRepository.findById(id).orElse(null);

        if(existing == null) return null;
        if (newEmp.getVersion() == null) {
            throw new InvalidEmployeeVersionException("version is required for updates");
        }
        if (!newEmp.getVersion().equals(existing.getVersion())) {
            throw new StaleEmployeeUpdateException(id, newEmp.getVersion(), existing.getVersion());
        }

        existing.setFirstName(newEmp.getFirstName());
        existing.setLastName(newEmp.getLastName());
        existing.setDeptId(newEmp.getDeptId());
        existing.setManagerId(newEmp.getManagerId());
        existing.setHireDate(newEmp.getHireDate());
        existing.setSalary(newEmp.getSalary());

        Employee savedEmployee = employeeRepository.save(existing);
        kafkaProducerService.sendEmployeeEvent("UPDATED", savedEmployee);
        return savedEmployee;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "employeeById", key = "#id"),
            @CacheEvict(cacheNames = "employeeList", allEntries = true)
    })
    public String deleteEmp(Integer id){

        Employee existing = employeeRepository.findById(id).orElse(null);
        if(existing == null){
            return "Employee not found";
        }

        employeeRepository.deleteById(id);
        kafkaProducerService.sendEmployeeEvent("DELETED", existing);
        return "Employee deleted successfully";
    }

}
