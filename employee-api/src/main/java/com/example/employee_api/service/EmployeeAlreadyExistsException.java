package com.example.employee_api.service;

public class EmployeeAlreadyExistsException extends RuntimeException {

    public EmployeeAlreadyExistsException(Integer empId) {
        super("Employee with empId " + empId + " already exists");
    }
}
