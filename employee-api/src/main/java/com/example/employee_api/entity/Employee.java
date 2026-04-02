package com.example.employee_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
@Table(name = "Employee")
public class Employee {

    @Id
    @Column(name = "EmpID")
    @NotNull(message = "empId is required")
    @Positive(message = "empId must be greater than 0")
    private Integer empId;

    @Column(name = "FirstName", nullable = false)
    @NotBlank(message = "firstName is required")
    private String firstName;

    @Column(name = "LastName")
    private String lastName;

    @Column(name = "DeptID")
    @NotNull(message = "deptId is required")
    @Positive(message = "deptId must be greater than 0")
    private Integer deptId;

    @Column(name = "ManagerID")
    @PositiveOrZero(message = "managerId must be 0 or greater")
    private Integer managerId;

    @Column(name = "HireDate")
    @Pattern(
            regexp = "^\\d{4}-\\d{2}-\\d{2}$",
            message = "hireDate must be in yyyy-MM-dd format")
    private String hireDate;

    @Column(name = "Salary")
    @NotNull(message = "salary is required")
    @PositiveOrZero(message = "salary must be 0 or greater")
    private Double salary;

    @Version
    @Column(name = "Version")
    private Integer version;

    public Integer getEmpId() {
        return empId;
    }

    public void setEmpId(Integer empId) {
        this.empId = empId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Integer getDeptId() {
        return deptId;
    }

    public void setDeptId(Integer deptId) {
        this.deptId = deptId;
    }

    public Integer getManagerId() {
        return managerId;
    }

    public void setManagerId(Integer managerId) {
        this.managerId = managerId;
    }

    public String getHireDate() {
        return hireDate;
    }

    public void setHireDate(String hireDate) {
        this.hireDate = hireDate;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }



    
   

    



}
