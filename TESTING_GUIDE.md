# Testing Guide

This file explains the testing examples added to the project so you can clearly see the difference between unit testing and integration testing.

## What Was Added

### Unit tests

- [JwtServiceTest.java](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/employee-api/src/test/java/com/example/employee_api/security/JwtServiceTest.java)
- [EmployeeServiceTest.java](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/employee-api/src/test/java/com/example/employee_api/service/EmployeeServiceTest.java)

### Integration tests

- [AuthControllerIntegrationTest.java](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/employee-api/src/test/java/com/example/employee_api/controller/AuthControllerIntegrationTest.java)
- [EmployeeControllerIntegrationTest.java](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/employee-api/src/test/java/com/example/employee_api/controller/EmployeeControllerIntegrationTest.java)
- [EmployeeSecurityIntegrationTest.java](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/employee-api/src/test/java/com/example/employee_api/controller/EmployeeSecurityIntegrationTest.java)

## 1. Unit Test Meaning

Unit test means:

- test one class in isolation
- do not depend on the full application running
- usually mock other collaborators

### Example: `JwtServiceTest`

What it tests:

- access token generation
- refresh token generation
- token type detection
- username extraction
- expiry extraction

Why it is a unit test:

- it creates `JwtService` directly
- no Spring Boot app is started
- no DB
- no Kafka
- no HTTP request

### Example: `EmployeeServiceTest`

What it tests:

- create employee success
- duplicate employee rejection
- stale version rejection on update

Why it is a unit test:

- `EmployeeRepository` is mocked
- `KafkaProducerService` is mocked
- only `EmployeeService` logic is tested

So this test answers:

- "Does this service method behave correctly by itself?"

## 2. Integration Test Meaning

Integration test means:

- test how multiple real parts work together
- often start Spring context
- may use real controller, validation, security, JSON binding, exception handling

### Example: `AuthControllerIntegrationTest`

What it tests:

- HTTP `POST /auth/register`
- request body binding
- validation behavior
- response JSON
- controller wiring

Why it is an integration test:

- Spring MVC starts
- `MockMvc` sends a real HTTP-style request through Spring MVC
- validation and exception handling are exercised
- controller and framework integration are real

What is still mocked:

- `AuthService`

That keeps the test focused on web integration without needing DB setup.

So this test answers:

- "Does this HTTP endpoint work correctly inside the Spring application?"

### Example: `EmployeeControllerIntegrationTest`

What it tests:

- HTTP `POST /employees`
- request body validation
- employee create JSON response
- invalid payload rejection

Why it is an integration test:

- `MockMvc` sends a real request through Spring MVC
- `@Valid` validation is exercised
- JSON binding and response serialization are exercised
- `EmployeeService` is mocked so the test stays focused on the web layer

### Example: `EmployeeSecurityIntegrationTest`

What it tests:

- protected endpoint rejects anonymous request
- protected endpoint allows authenticated request

Why it is an integration test:

- the Spring Security filter chain is active
- access control is tested as part of the application behavior
- `MockMvc` runs the request through real security configuration

## 3. API Testing Meaning

API testing means:

- testing endpoints from outside the app
- usually using Postman or similar tools

In your project, examples are:

- login via Postman
- call `/employees`
- call Kafka demo endpoints
- call Redis endpoints
- verify responses and status codes manually

This is different from automated Java test classes, but it is still valid API testing.

## 4. Very Short Difference

### Unit test

- one class
- isolated
- mocks dependencies

### Integration test

- multiple parts together
- Spring wiring involved
- request/response or bean interaction involved

### API test

- external client call
- manual or automated through HTTP

## 5. What We Still Have Not Added Yet

Good future additions:

- repository integration test
- Kafka consumer integration test
- gateway integration test
- full DB -> Kafka -> consumer -> downstream side-effect integration test
- security integration test with a real bearer token instead of test authentication helpers

## 6. How To Run

From:

- `C:\Users\Salma\OneDrive\Documents\Java_Laranings\Unit_test_learning\src\employee-api`

Run:

```powershell
mvn test
```

## 7. Best Way To Learn From These Tests

Read them in this order:

1. [JwtServiceTest.java](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/employee-api/src/test/java/com/example/employee_api/security/JwtServiceTest.java)
2. [EmployeeServiceTest.java](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/employee-api/src/test/java/com/example/employee_api/service/EmployeeServiceTest.java)
3. [AuthControllerIntegrationTest.java](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/employee-api/src/test/java/com/example/employee_api/controller/AuthControllerIntegrationTest.java)
4. [EmployeeControllerIntegrationTest.java](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/employee-api/src/test/java/com/example/employee_api/controller/EmployeeControllerIntegrationTest.java)
5. [EmployeeSecurityIntegrationTest.java](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/employee-api/src/test/java/com/example/employee_api/controller/EmployeeSecurityIntegrationTest.java)

That order goes from simplest to more real application behavior.
