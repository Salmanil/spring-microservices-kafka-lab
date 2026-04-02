package com.example.employee_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

import jakarta.annotation.PostConstruct;


@SpringBootApplication
// @EnableKafka
@EnableCaching
@ComponentScan(basePackages="com.example.employee_api")
public class EmployeeApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmployeeApiApplication.class, args);
	}

	@PostConstruct
public void printDbPath() {
    System.out.println("USING DB FILE: " + new java.io.File("practice.db").getAbsolutePath());
}


}
