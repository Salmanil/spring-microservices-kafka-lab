# Spring Microservices Kafka Lab

Hands-on Spring Boot microservices learning project with Kafka, Redis, JWT security, API Gateway, Eureka, Elasticsearch, Kibana, Grafana, Prometheus, Zipkin, SonarQube, and Docker Compose.

## Modules

- `employee-api` - employee CRUD, JWT auth, Redis learning APIs, Kafka producer, HTTPS, tracing
- `notification-service` - Kafka consumer, error topic handling, event inspection APIs
- `api-gateway` - Spring Cloud Gateway, Redis rate limiting, idempotency header enforcement
- `service-registry` - Eureka server
- `docker/kafka-setup` - Kafka platform stack, observability stack, Elasticsearch/Kibana/Grafana/Zipkin/SonarQube support

## What This Project Covers

- Spring Boot microservices
- Eureka service discovery
- Spring Cloud Gateway
- Spring Security with JWT access and refresh tokens
- refresh token blacklist persistence
- HTTPS / SSL local setup
- Kafka producer / consumer flow
- Schema Registry JSON Schema serializer and deserializer
- Kafka Connect sink to Elasticsearch
- Kibana Discover and data views
- Redis caching, TTL, counters, rate limiting, pub/sub, streams, distributed locks
- Resilience4j retry, timeout, circuit breaker, fallback
- Prometheus, Grafana, Micrometer, Actuator
- Zipkin distributed tracing
- SonarQube local code quality setup
- unit and integration testing examples

## Repo Structure

```text
spring-microservices-kafka-lab/
|-- employee-api/
|-- notification-service/
|-- api-gateway/
|-- service-registry/
|-- docker/
|   `-- kafka-setup/
`-- *.md guides and checklists
```

## Startup Order

1. Start Docker Desktop
2. Start the platform stack from `docker/kafka-setup`
3. Start `service-registry`
4. Start `employee-api`
5. Start `notification-service`
6. Start `api-gateway`

## Platform Start Command

From `docker/kafka-setup`:

```powershell
docker compose up -d
```

## Spring App Start Commands

Open one terminal per service.

```powershell
cd service-registry
mvn spring-boot:run
```

```powershell
cd employee-api
mvn spring-boot:run
```

```powershell
cd notification-service
mvn spring-boot:run
```

```powershell
cd api-gateway
mvn spring-boot:run
```

## Useful URLs

- Eureka: `http://localhost:8761`
- API Gateway: `http://localhost:8080`
- Employee API Swagger: `http://localhost:8081/swagger-ui.html`
- Notification Swagger: `http://localhost:8082/swagger-ui.html`
- Kafka UI: `http://localhost:8090`
- Kafka Connect: `http://localhost:8083/connectors`
- Schema Registry: `http://localhost:8085`
- ksqlDB: `http://localhost:8088`
- Elasticsearch: `http://localhost:9200`
- Kibana: `http://localhost:5601`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`
- Zipkin: `http://localhost:9411`
- SonarQube: `http://localhost:9000`

## Guides

- `LOCAL_URL_GUIDE.md`
- `MICROSERVICE_FLOW_GUIDE.md`
- `KAFKA_TEST_CHECKLIST.md`
- `REDIS_TEST_CHECKLIST.md`
- `SPRING_SECURITY_JWT_TEST_CHECKLIST.md`
- `DISTRIBUTED_TRACING_GUIDE.md`
- `INTEGRATION_TEST_STEP_BY_STEP.md`
- `TESTING_GUIDE.md`

## Notes

- This repo is designed as a learning lab, so it includes both production-style patterns and explicit learning endpoints.
- Some local features assume Docker Desktop and Windows PowerShell.
- For HTTPS local testing, you may need to disable SSL verification in Postman because the certificate is self-signed.
