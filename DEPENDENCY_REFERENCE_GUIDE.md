# Dependency Reference Guide

This file is a quick learning guide for the main direct Maven dependencies used in your current microservices project.

Important note:

- this guide focuses on the direct dependencies you added in your `pom.xml` files
- it does not list every transitive dependency pulled by Spring Boot
- the meaning here is project-specific, not generic textbook meaning

---

## Employee API

File:

- `C:\Users\Salma\OneDrive\Documents\Java_Laranings\Unit_test_learning\src\employee-api\pom.xml`

| Dependency | Where used in code | Why needed | What happens if removed |
|---|---|---|---|
| `spring-boot-starter-web` | controllers like [EmployeeController.java](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/employee-api/src/main/java/com/example/employee_api/controller/EmployeeController.java) and [AuthController.java](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/employee-api/src/main/java/com/example/employee_api/controller/AuthController.java) | creates REST APIs and embedded server support | app cannot expose HTTP endpoints |
| `spring-boot-starter-data-jpa` | repositories like [EmployeeRepository.java](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/employee-api/src/main/java/com/example/employee_api/repository/EmployeeRepository.java), [AppUserRepository.java](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/employee-api/src/main/java/com/example/employee_api/repository/AppUserRepository.java) | ORM mapping and repository support | entity/repository DB access breaks |
| `spring-boot-starter-jdbc` | datasource wiring for SQLite | supports JDBC connectivity under the app | DB connectivity becomes harder or fails depending on JPA setup |
| `sqlite-jdbc` | SQLite DB connection to `practice.db` | actual JDBC driver for SQLite | app cannot connect to the SQLite database |
| `hibernate-community-dialects` | SQLite dialect configuration in `application.properties` | Hibernate needs SQLite dialect support | JPA/Hibernate SQL generation and mapping can fail |
| `spring-kafka` | [KafkaProducerService.java](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/employee-api/src/main/java/com/example/employee_api/service/KafkaProducerService.java), Kafka config classes | produce Kafka messages and manage Kafka config | Kafka producer flow stops working |
| `kafka-json-schema-serializer` | [KafkaProducerConfig.java](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/employee-api/src/main/java/com/example/employee_api/config/KafkaProducerConfig.java) | serializes `EmployeeEvent` using Schema Registry JSON Schema | schema-based Kafka publishing fails |
| `spring-cloud-starter-netflix-eureka-client` | service registration in Eureka | lets `employee-api` register/discover in the microservice setup | Eureka registration/discovery stops |
| `spring-cloud-starter-loadbalancer` | [HttpClientConfig.java](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/employee-api/src/main/java/com/example/employee_api/config/HttpClientConfig.java) and `http://notification-service` calls | allows service-to-service calls by logical service name | `notification-service` name resolution fails unless hardcoded URL is used |
| `resilience4j-spring-boot3` | [NotificationResilienceService.java](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/employee-api/src/main/java/com/example/employee_api/service/NotificationResilienceService.java) | gives retry, circuit breaker, timeout, fallback learning flow | resilience annotations/features stop working |
| `spring-boot-starter-aop` | supports Resilience4j method interception | needed because retry/circuit-breaker/timelimiter behavior uses AOP proxies | resilience annotations do not get applied correctly |
| `spring-boot-starter-security` | [SecurityConfig.java](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/employee-api/src/main/java/com/example/employee_api/config/SecurityConfig.java), JWT filter chain | secures endpoints and authentication flow | auth protection disappears |
| `jjwt-api` | [JwtService.java](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/employee-api/src/main/java/com/example/employee_api/security/JwtService.java) | JWT token creation/parsing API | JWT support cannot compile/work |
| `jjwt-impl` | runtime JWT implementation | required runtime engine for JJWT | token generation/parsing fails at runtime |
| `jjwt-jackson` | runtime JSON mapping for JWT claims | serializes/deserializes JWT payload content | JWT claim handling can fail at runtime |
| `springdoc-openapi-starter-webmvc-ui` | Swagger/OpenAPI docs | gives `/swagger-ui.html` and `/v3/api-docs` | API documentation UI disappears |
| `spring-boot-starter-validation` | DTOs and entity validation like [RegisterRequest.java](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/employee-api/src/main/java/com/example/employee_api/model/RegisterRequest.java), [Employee.java](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/employee-api/src/main/java/com/example/employee_api/entity/Employee.java) | validates request bodies before processing | bad input may reach DB/Kafka/business logic |
| `spring-boot-starter-actuator` | `/actuator/health`, `/actuator/prometheus`, cache endpoint | health and metrics endpoints | monitoring/health endpoints disappear |
| `micrometer-registry-prometheus` | Prometheus scrape endpoint | exports app metrics to Prometheus/Grafana | Prometheus cannot scrape useful metrics |
| `spring-boot-starter-cache` | caching in [EmployeeService.java](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/employee-api/src/main/java/com/example/employee_api/service/EmployeeService.java) | enables Spring cache abstraction | cache annotations/behavior stop working |
| `spring-boot-starter-data-redis` | Redis config, cache backend, Redis learning endpoints | connects app to Redis for cache, TTL, counters, streams, pub/sub, locks | Redis-backed features stop working |
| `spring-boot-starter-test` | tests | test libraries for Spring Boot | project tests become harder or fail to compile |

---

## Notification Service

File:

- `C:\Users\Salma\OneDrive\Documents\Java_Laranings\Unit_test_learning\src\notification-service\pom.xml`

| Dependency | Where used in code | Why needed | What happens if removed |
|---|---|---|---|
| `spring-kafka` | [NotificationConsumer.java](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/notification-service/src/main/java/com/example/notification_service/NotificationConsumer.java), Kafka listener config | consumes Kafka records and handles listener containers | Kafka consumer flow stops |
| `spring-boot-starter-web` | controller endpoints such as notification learning endpoints | exposes simple HTTP APIs from this service | no REST endpoints |
| `spring-cloud-starter-netflix-eureka-client` | service registration | registers `notification-service` in Eureka | gateway/service discovery flow weakens or breaks |
| `spring-retry` | Kafka listener error retry support | helps retry failed consumer processing before dead-letter routing | retry behavior becomes limited or unavailable |
| `kafka-json-schema-serializer` | consumer deserializer config for Schema Registry JSON | reads schema-based Kafka payloads into `EmployeeEvent` | schema-based payload consumption fails |
| `springdoc-openapi-starter-webmvc-ui` | Swagger docs for notification service | gives API docs UI and OpenAPI json | docs disappear |
| `spring-boot-starter-actuator` | health and metrics endpoints | exposes operational endpoints | health/metrics endpoints disappear |
| `micrometer-registry-prometheus` | Prometheus integration | lets Grafana/Prometheus see app metrics | service metrics are missing |
| `lombok` | compile-time helper if used in future or existing code generation | reduces boilerplate when annotations are used | if Lombok annotations are used, code may fail to compile |
| `spring-kafka-test` | Kafka-related tests | test support for Kafka components | Kafka tests become harder or fail |
| `spring-boot-starter-test` | unit/integration tests | Spring test support | test setup breaks |

---

## API Gateway

File:

- `C:\Users\Salma\OneDrive\Documents\Java_Laranings\Unit_test_learning\src\api-gateway\pom.xml`

| Dependency | Where used in code | Why needed | What happens if removed |
|---|---|---|---|
| `spring-cloud-starter-gateway-mvc` | gateway route config and request forwarding | acts as the API gateway in front of services | gateway routing stops working |
| `spring-cloud-starter-netflix-eureka-client` | service discovery through Eureka | lets gateway route to discovered services | routes to service names break |
| `spring-cloud-starter-loadbalancer` | gateway-side service instance resolution | balances and resolves service instances behind logical names | service-name routing becomes unreliable or fails |
| `spring-boot-starter-actuator` | health/monitoring endpoints | exposes gateway operational endpoints | health checks disappear |
| `spring-boot-starter-data-redis` | [RedisRateLimitFilter.java](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/api-gateway/src/main/java/com/example/apigateway/filter/RedisRateLimitFilter.java) | supports Redis-backed rate limiting | gateway rate limiting stops working |
| `spring-boot-starter-test` | tests | standard Spring test support | tests become harder or fail |

---

## Service Registry

File:

- `C:\Users\Salma\OneDrive\Documents\Java_Laranings\Unit_test_learning\src\service-registry\pom.xml`

| Dependency | Where used in code | Why needed | What happens if removed |
|---|---|---|---|
| `spring-boot-starter-web` | web server for Eureka dashboard and endpoints | runs the registry as an HTTP service | registry cannot expose its UI/API properly |
| `spring-cloud-starter-netflix-eureka-server` | Eureka server application | turns this app into the service registry | service discovery server does not exist |
| `spring-boot-starter-actuator` | health endpoints | operational visibility for registry | health/monitoring endpoints disappear |
| `spring-boot-starter-test` | tests | Spring test support | tests become harder or fail |

---

## Dependency Management Entries

These are not regular runtime features by themselves, but they are still important.

| Entry | Where used | Why needed | What happens if removed |
|---|---|---|---|
| `spring-boot-starter-parent` | all modules | gives consistent Spring Boot plugin and dependency versions | version management becomes manual and more error-prone |
| `spring-cloud-dependencies` | modules using Spring Cloud | aligns Eureka, Gateway, LoadBalancer, and related Spring Cloud versions | version mismatches can cause startup/runtime issues |

---

## Simple Learning Map

- `web` = expose REST endpoints
- `data-jpa` and `jdbc` = talk to the database
- `sqlite-jdbc` = actual SQLite driver
- `spring-kafka` = Kafka producer/consumer support
- `kafka-json-schema-serializer` = schema-based Kafka messages
- `eureka-client` = register/discover services
- `loadbalancer` = call services by name
- `resilience4j` + `aop` = retry, timeout, circuit breaker
- `security` + `jjwt` = login and JWT protection
- `springdoc` = Swagger/OpenAPI docs
- `validation` = reject bad request bodies early
- `actuator` + `prometheus` = monitoring
- `cache` + `redis` = cache, rate limiting, TTL, pub/sub, streams, locks

---

## Best Way To Study This File

Read it in this order:

1. `employee-api`
2. `notification-service`
3. `api-gateway`
4. `service-registry`

That order matches the request flow most closely.
