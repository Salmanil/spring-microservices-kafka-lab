# Interceptor Guide

This guide explains the Spring MVC interceptor added for learning in `employee-api`.

## What Was Added

- [RequestAuditInterceptor.java](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/employee-api/src/main/java/com/example/employee_api/interceptor/RequestAuditInterceptor.java)
- [WebMvcConfig.java](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/employee-api/src/main/java/com/example/employee_api/config/WebMvcConfig.java)

## What It Does

The interceptor:

- runs before the controller method
- adds a response header:
  - `X-Intercepted-By: employee-api-request-interceptor`
- stores request start time
- logs total request duration after the request completes

## Why We Added It

You already have filters in the project.

This interceptor helps you compare:

- filter = request pipeline level
- interceptor = Spring MVC handler level

## Flow Position

Simple order:

1. filter runs
2. security/filter chain runs
3. interceptor `preHandle()` runs
4. controller method runs
5. interceptor `afterCompletion()` runs

## Where It Applies

It is registered for most application routes and excludes:

- `/swagger-ui/**`
- `/swagger-ui.html`
- `/v3/api-docs/**`
- `/actuator/**`

## How To Test

Call a normal endpoint such as:

```text
POST http://localhost:8081/employees
```

If the interceptor ran, the response should include:

```text
X-Intercepted-By: employee-api-request-interceptor
```

## Automated Proof

This was also verified in:

- [EmployeeControllerIntegrationTest.java](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/employee-api/src/test/java/com/example/employee_api/controller/EmployeeControllerIntegrationTest.java)

That test checks the header so you can see the interceptor is part of the real MVC request flow.

## Difference From Filters

### Filter

- works at servlet/request level
- good for auth, rate limiting, idempotency, generic request processing

### Interceptor

- works at Spring MVC handler level
- good for controller timing, request logging, handler-specific checks

## Best Learning Example In This Project

- JWT auth = filter example
- rate limiting = filter example
- request audit header = interceptor example
