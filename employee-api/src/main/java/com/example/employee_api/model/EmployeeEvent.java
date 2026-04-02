package com.example.employee_api.model;

public class EmployeeEvent {

    private String action;
    private Integer empId;
    private String name;
    private Integer deptId;
    private Double salary;
    private String eventTimestamp;

    public EmployeeEvent() {
    }

    public EmployeeEvent(String action, Integer empId, String name, Integer deptId, Double salary, String eventTimestamp) {
        this.action = action;
        this.empId = empId;
        this.name = name;
        this.deptId = deptId;
        this.salary = salary;
        this.eventTimestamp = eventTimestamp;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Integer getEmpId() {
        return empId;
    }

    public void setEmpId(Integer empId) {
        this.empId = empId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDeptId() {
        return deptId;
    }

    public void setDeptId(Integer deptId) {
        this.deptId = deptId;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }

    public String getEventTimestamp() {
        return eventTimestamp;
    }

    public void setEventTimestamp(String eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }
}
