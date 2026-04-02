package com.example.employee_api.service;

public class StaleEmployeeUpdateException extends RuntimeException {

    public StaleEmployeeUpdateException(Integer empId, Integer expectedVersion, Integer actualVersion) {
        super("Employee " + empId + " update rejected because the request version " + expectedVersion
                + " is stale. Current version is " + actualVersion);
    }
}
