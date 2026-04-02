package com.example.employee_api.service;

public class InvalidEmployeeVersionException extends RuntimeException {

    public InvalidEmployeeVersionException(String message) {
        super(message);
    }
}
