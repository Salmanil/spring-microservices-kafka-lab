# Integration Test Step By Step

This guide is for understanding **how integration testing works in this project**.

It answers these questions:

- do I test in Postman or in code?
- do I run something in terminal?
- which URL is hit?
- does it call real DB or fake dependencies?
- how is it different from unit testing?

## 1. Simple Meaning

Integration testing means:

- multiple parts of the application work together
- not just one class in isolation

Examples:

- controller + validation + JSON mapping
- controller + security
- service + repository + DB
- producer + Kafka
- consumer + DB update

## 2. Two Different Ways To Test

In this project, you can test in **two ways**.

### A. Manual integration testing

This means:

- start the application
- use Postman or browser
- hit a real running endpoint
- verify DB / Kafka / Redis / Elasticsearch manually

Example:

1. start `employee-api`
2. call `POST http://localhost:8081/employees`
3. check DB table
4. check Kafka
5. check consumer

This is **real runtime manual integration testing**.

### B. Automated integration testing

This means:

- write a test class under `src/test/java`
- run `mvn test`
- Spring starts test context
- test sends request internally using `MockMvc`
- verify response automatically

This is **code-based automated integration testing**.

## 3. Do I Use Postman Or Terminal?

### For manual integration testing

Use:

- Postman
- browser
- Kafka UI
- Kibana
- Grafana
- DB tool like DBeaver

### For automated integration testing

Use:

- Java test classes
- terminal command:

```powershell
mvn test
```

So:

- Postman = manual testing
- `mvn test` = automated testing

## 4. In Automated Integration Test, Which URL Is Hit?

Usually, **no real external URL is hit**.

Example:

- in `MockMvc`, you may write:

```java
mockMvc.perform(post("/auth/login"))
```

This does **not** call:

- `http://localhost:8081/auth/login`

through the network.

Instead, Spring handles the request **inside the test context**.

So it behaves like an API call, but it stays inside the test runtime.

That is why these tests are fast and stable.

## 5. Does Automated Integration Test Call Real DB?

It depends on how the test is written.

### In your current controller integration tests

Mostly:

- request handling is real
- validation is real
- JSON mapping is real
- security flow can be real
- but service/repository/Kafka dependencies are usually mocked

So these are **web-layer integration tests**, not full DB integration tests.

Examples:

- [AuthControllerIntegrationTest.java](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/employee-api/src/test/java/com/example/employee_api/controller/AuthControllerIntegrationTest.java)
- [EmployeeControllerIntegrationTest.java](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/employee-api/src/test/java/com/example/employee_api/controller/EmployeeControllerIntegrationTest.java)
- [EmployeeSecurityIntegrationTest.java](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/employee-api/src/test/java/com/example/employee_api/controller/EmployeeSecurityIntegrationTest.java)

These do **not** run against the real DB in the way Postman runtime testing does.

## 6. Then What Is Unit Test?

Unit test means:

- only one class is tested
- dependencies are mocked
- no real Spring MVC request flow
- no real application wiring

Examples:

- [JwtServiceTest.java](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/employee-api/src/test/java/com/example/employee_api/security/JwtServiceTest.java)
- [EmployeeServiceTest.java](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/employee-api/src/test/java/com/example/employee_api/service/EmployeeServiceTest.java)

So:

- unit test = one class
- integration test = multiple layers/framework parts together

## 7. Current Automated Integration Style In This Project

Right now, the easiest integration testing style in your project is:

- create a test class under `controller`
- use Spring test support
- use `MockMvc`
- hit controller endpoint
- verify response

This is a good starting point for learning.

## 8. How To Create One Here

### Step 1

Create the test file in:

- `src/test/java/.../controller`

Example:

- `EmployeeControllerIntegrationTest.java`

### Step 2

Choose the style:

- `@WebMvcTest` for web/controller-focused testing
- `@SpringBootTest` for larger application context testing

### Step 3

Inject `MockMvc`

Example:

```java
@Autowired
private MockMvc mockMvc;
```

### Step 4

Mock the dependencies that should not run for real

Examples:

- service
- repository
- Kafka producer
- Redis listener

### Step 5

Call the endpoint

Example:

```java
mockMvc.perform(post("/employees")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
```

### Step 6

Verify result

Examples:

- status code
- response body
- JSON fields
- headers

## 9. Example: What Happens In EmployeeControllerIntegrationTest

When the test runs:

1. Spring creates test MVC context
2. `MockMvc` simulates HTTP request internally
3. request goes through controller logic
4. validation runs
5. interceptor/header behavior can run
6. mocked service responds
7. test checks status and JSON

So yes, it is like API flow, but not through Postman.

## 10. If I Want Real DB Integration Test

Then the test should be written differently.

That would mean:

- use real repository
- use real test DB or test profile DB
- do not mock repository
- possibly use `@SpringBootTest`
- optionally use `@Transactional`

That is a deeper integration test than the current controller tests.

## 11. If I Want Full End-To-End Test

That means:

- start real app
- hit real URL from Postman or test client
- real DB
- real Kafka
- real Redis
- real downstream integrations

This is closest to production behavior.

Examples in your project:

- `POST http://localhost:8081/employees`
- then verify DB
- then verify Kafka
- then verify consumer

That is **manual end-to-end integration testing**.

## 12. What To Use When

Use **unit test** when:

- testing one class logic only
- fast logic validation is enough

Use **automated integration test** when:

- testing controller flow
- testing validation
- testing security
- testing response contracts

Use **manual Postman/infrastructure testing** when:

- testing DB + Kafka + consumer + Elasticsearch together
- testing full runtime behavior

## 13. Best Practical Learning Rule

For this project:

- unit test = service/helper class behavior
- automated integration test = controller + validation + security + interceptor flow
- Postman/manual integration test = real DB + Kafka + Redis + Elasticsearch flow

## 14. Current Test Files In This Project

### Unit tests

- [JwtServiceTest.java](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/employee-api/src/test/java/com/example/employee_api/security/JwtServiceTest.java)
- [EmployeeServiceTest.java](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/employee-api/src/test/java/com/example/employee_api/service/EmployeeServiceTest.java)

### Automated integration tests

- [AuthControllerIntegrationTest.java](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/employee-api/src/test/java/com/example/employee_api/controller/AuthControllerIntegrationTest.java)
- [EmployeeControllerIntegrationTest.java](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/employee-api/src/test/java/com/example/employee_api/controller/EmployeeControllerIntegrationTest.java)
- [EmployeeSecurityIntegrationTest.java](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/employee-api/src/test/java/com/example/employee_api/controller/EmployeeSecurityIntegrationTest.java)

## 15. Very Short Answer

If you are asking:

“Do I need Postman for integration testing?”

Answer:

- for manual testing, yes
- for automated test classes, no

If you are asking:

“Does the integration test call the real DB?”

Answer:

- not necessarily
- your current controller integration tests mostly test request flow with mocked dependencies
- full DB/Kafka integration is usually done either in deeper integration tests or manual runtime tests

## 16. Good Next Learning Steps

If you want stronger testing practice later, the next good additions are:

1. repository integration test with real test DB
2. service + repository integration test
3. Kafka integration test
4. full authenticated controller integration test for create/update flow
