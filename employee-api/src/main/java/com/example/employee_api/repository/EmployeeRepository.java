package com.example.employee_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.employee_api.entity.Employee;



public interface EmployeeRepository extends JpaRepository<Employee, Integer>{

}
