package com.example.employee_api.service;

public class InvalidSchemaPayloadException extends RuntimeException {

    public InvalidSchemaPayloadException(String message, Throwable cause) {
        super(message, cause);
    }
}
