# Test Checklist Index

Use this as the first file to open when you want to practice or verify something in the project.

## Main Checklists

| Area | File | Use it for |
|---|---|---|
| Kafka end-to-end flow | [KAFKA_TEST_CHECKLIST.md](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/KAFKA_TEST_CHECKLIST.md) | producer, partitions, consumer group, Connect, Elasticsearch, Kibana, ksqlDB, Grafana |
| Full microservice platform | [MICROSERVICE_PASS_FAIL_CHECKLIST.md](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/MICROSERVICE_PASS_FAIL_CHECKLIST.md) | Eureka, gateway, routed docs, schema registry, connector status, failover checks |
| Redis learning | [REDIS_TEST_CHECKLIST.md](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/REDIS_TEST_CHECKLIST.md) | caching, TTL, counters, rate limiting, pub/sub, streams, locks |
| Spring Security and JWT | [SPRING_SECURITY_JWT_TEST_CHECKLIST.md](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/SPRING_SECURITY_JWT_TEST_CHECKLIST.md) | register, login, bearer token, refresh, logout, blacklist, HTTPS |
| SonarQube | [SONARQUBE_TEST_CHECKLIST.md](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/SONARQUBE_TEST_CHECKLIST.md) | SonarQube startup, token creation, scans, failure cases |

## Supporting Guides

| Guide | File | Use it for |
|---|---|---|
| URLs | [LOCAL_URL_GUIDE.md](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/LOCAL_URL_GUIDE.md) | all local endpoints and dashboards |
| Dependency reference | [DEPENDENCY_REFERENCE_GUIDE.md](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/DEPENDENCY_REFERENCE_GUIDE.md) | what each dependency does and what breaks without it |
| Security explanation | [SPRING_SECURITY_JWT_GUIDE.md](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/SPRING_SECURITY_JWT_GUIDE.md) | access token, refresh token, blacklist, HTTPS |
| Redis explanation | [REDIS_GUIDE.md](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/REDIS_GUIDE.md) | cache, rate limit, pub/sub, streams, locks |
| Resilience and error topic | [RESILIENCE4J_AND_ERROR_TOPIC_GUIDE.md](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/RESILIENCE4J_AND_ERROR_TOPIC_GUIDE.md) | retry, timeout, circuit breaker, fallback, error topic |
| Kafka monitoring | [KAFKA_MONITORING_GUIDE.md](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/KAFKA_MONITORING_GUIDE.md) | Grafana, Prometheus, Kafka metrics |
| Microservice flow | [MICROSERVICE_FLOW_GUIDE.md](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/MICROSERVICE_FLOW_GUIDE.md) | service interactions at a high level |
| SonarQube explanation | [SONARQUBE_GUIDE.md](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/SONARQUBE_GUIDE.md) | how scanning works and how to read the reports |

## Best Study Order

1. [LOCAL_URL_GUIDE.md](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/LOCAL_URL_GUIDE.md)
2. [SPRING_SECURITY_JWT_TEST_CHECKLIST.md](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/SPRING_SECURITY_JWT_TEST_CHECKLIST.md)
3. [KAFKA_TEST_CHECKLIST.md](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/KAFKA_TEST_CHECKLIST.md)
4. [MICROSERVICE_PASS_FAIL_CHECKLIST.md](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/MICROSERVICE_PASS_FAIL_CHECKLIST.md)
5. [REDIS_TEST_CHECKLIST.md](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/REDIS_TEST_CHECKLIST.md)
6. [SONARQUBE_TEST_CHECKLIST.md](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/SONARQUBE_TEST_CHECKLIST.md)

## What I Corrected

- Kafka UI port references were aligned to `8090`
- protected `employee-api` calls now mention bearer token requirement
- Kafka raw bad-message test now matches the real `/employees/send/raw` endpoint
- gateway write-path behavior now includes `Idempotency-Key`
- Redis checks now reflect the secured `employee-api`

## What Is Already In Good Shape

- Spring Security checklist
- SonarQube checklist

Those two already matched the current implementation closely.
