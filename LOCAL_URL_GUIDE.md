# Local URL Guide

This file is a quick local URL reference for your project.

Use it when you want to open the right service quickly without searching through code or Docker files.

## Core Services

| Service | URL | Why you open it |
|---|---|---|
| API Gateway | `http://localhost:8080` | main entry point for routed service calls |
| Employee API | `http://localhost:8081` | direct access to employee service |
| Employee API HTTPS | `https://localhost:8443` | secure local testing for auth/JWT flow |
| Notification Service | `http://localhost:8082` | direct access to notification service |
| Service Registry | `http://localhost:8761` | Eureka dashboard to see registered services |

## API Docs

| Docs | URL | Why you open it |
|---|---|---|
| Employee Swagger UI | `http://localhost:8081/swagger-ui.html` | direct docs for employee-api |
| Employee OpenAPI JSON | `http://localhost:8081/v3/api-docs` | raw employee-api OpenAPI spec |
| Notification Swagger UI | `http://localhost:8082/swagger-ui.html` | direct docs for notification-service |
| Notification OpenAPI JSON | `http://localhost:8082/v3/api-docs` | raw notification-service OpenAPI spec |
| Employee docs via gateway | `http://localhost:8080/api/docs/employee/swagger-ui.html` | employee docs through gateway |
| Employee OpenAPI via gateway | `http://localhost:8080/api/docs/employee/v3/api-docs` | employee OpenAPI through gateway |
| Notification docs via gateway | `http://localhost:8080/api/docs/notification/swagger-ui.html` | notification docs through gateway |
| Notification OpenAPI via gateway | `http://localhost:8080/api/docs/notification/v3/api-docs` | notification OpenAPI through gateway |

## Health and Monitoring

| Tool / Endpoint | URL | Why you open it |
|---|---|---|
| Employee health | `http://localhost:8081/actuator/health` | check employee-api is up |
| Notification health | `http://localhost:8082/actuator/health` | check notification-service is up |
| Gateway health | `http://localhost:8080/actuator/health` | check api-gateway is up |
| Employee Prometheus | `http://localhost:8081/actuator/prometheus` | app metrics for employee-api |
| Notification Prometheus | `http://localhost:8082/actuator/prometheus` | app metrics for notification-service |
| Prometheus UI | `http://localhost:9090` | inspect scrape targets and queries |
| Prometheus targets | `http://localhost:9090/targets` | see which metric targets are up/down |
| Grafana | `http://localhost:3000` | dashboards for Kafka, apps, JVM |
| Zipkin | `http://localhost:9411` | distributed trace search and span visualization |

## Kafka Platform

| Tool | URL | Why you open it |
|---|---|---|
| Kafka UI | `http://localhost:8090` | topics, partitions, messages, consumer groups, Kafka Connect |
| Kafka Connect REST | `http://localhost:8083/connectors` | list connectors |
| Elasticsearch sink status | `http://localhost:8083/connectors/employee-events-elasticsearch-sink/status` | check if sink is running |
| Schema Registry | `http://localhost:8085/subjects` | inspect registered schemas |
| Schema Registry config | `http://localhost:8085/config` | check compatibility mode |
| ksqlDB info | `http://localhost:8088/info` | verify ksqlDB is up |

## Elasticsearch and Kibana

| Tool | URL | Why you open it |
|---|---|---|
| Elasticsearch | `http://localhost:9200` | raw Elasticsearch root endpoint |
| Employee events search | `http://localhost:9200/employee-events/_search?pretty` | inspect indexed Kafka documents |
| Kibana | `http://localhost:5601` | explore documents and filters visually |

## SonarQube

| Tool | URL | Why you open it |
|---|---|---|
| SonarQube | `http://localhost:9000` | static analysis dashboard |

## Gateway Business Routes

These go through `api-gateway`.

| Route | URL | Meaning |
|---|---|---|
| Employees via gateway | `http://localhost:8080/api/employees` | gateway route to employee-api |
| Employee by id via gateway | `http://localhost:8080/api/employees/{id}` | fetch employee through gateway |
| Notifications via gateway | `http://localhost:8080/api/notifications` | gateway route to notification-service |
| Notification health via gateway | `http://localhost:8080/api/notifications/health` | quick routed health check |

## Auth URLs

You can test auth on HTTP or HTTPS.

### HTTP auth

| Endpoint | URL |
|---|---|
| Register | `http://localhost:8081/auth/register` |
| Login | `http://localhost:8081/auth/login` |
| Refresh | `http://localhost:8081/auth/refresh` |
| Logout | `http://localhost:8081/auth/logout` |

### HTTPS auth

| Endpoint | URL |
|---|---|
| Register | `https://localhost:8443/auth/register` |
| Login | `https://localhost:8443/auth/login` |
| Refresh | `https://localhost:8443/auth/refresh` |
| Logout | `https://localhost:8443/auth/logout` |

Important note:

- HTTPS is using a local self-signed certificate
- Postman may require SSL verification to be disabled for local testing

## Employee API Learning Endpoints

| Purpose | URL |
|---|---|
| List employees | `http://localhost:8081/employees` |
| Employee by id | `http://localhost:8081/employees/{id}` |
| Produce schema-safe Kafka message | `http://localhost:8081/employees/send` |
| Producer schema-check learning endpoint | `http://localhost:8081/employees/send/schema-check` |
| Produce raw Kafka message | `http://localhost:8081/employees/send/raw` |
| Produce demo Kafka messages | `http://localhost:8081/employees/send/demo?count=6` |
| Kafka bootstrap check | `http://localhost:8081/employees/check` |
| Resilience test endpoint | `http://localhost:8081/employees/resilience/notifications?mode=ok` |

## Notification Service Learning Endpoints

| Purpose | URL |
|---|---|
| Notification health | `http://localhost:8082/health` |
| In-memory consumed events | `http://localhost:8082/notifications/events` |
| Failed Kafka messages | `http://localhost:8082/notifications/errors` |
| Notification demo slow/fail endpoint | `http://localhost:8082/notifications/demo?mode=ok` |

## Redis Learning Endpoints

| Purpose | URL |
|---|---|
| Put key with TTL | `http://localhost:8081/employees/redis/values?key=demo:user&value=salma&ttlSeconds=20` |
| Get key | `http://localhost:8081/employees/redis/values/demo:user` |
| Increment counter | `http://localhost:8081/employees/redis/counters/demo-counter/increment?ttlSeconds=30` |
| Publish pub/sub message | `http://localhost:8081/employees/redis/pubsub/publish?channel=channel:employee:alerts&payload=hello` |
| View recent pub/sub messages | `http://localhost:8081/employees/redis/pubsub/messages` |
| Append stream event | `http://localhost:8081/employees/redis/streams/events?eventType=CREATED&employeeId=9702&payload=employee-created` |
| Read latest stream events | `http://localhost:8081/employees/redis/streams/events?count=5` |
| Create stream consumer group | `http://localhost:8081/employees/redis/streams/groups/employee-group` |
| Read from stream consumer group | `http://localhost:8081/employees/redis/streams/groups/employee-group/consumers/consumer-a?count=5` |
| Acquire lock | `http://localhost:8081/employees/redis/locks/payroll-job/acquire?ttlSeconds=30` |
| Inspect lock | `http://localhost:8081/employees/redis/locks/payroll-job` |
| Release lock | `http://localhost:8081/employees/redis/locks/payroll-job/release?ownerToken=<token>` |

## Which Page To Open For Which Problem

| If you want to check... | Open this |
|---|---|
| Are services registered? | `http://localhost:8761` |
| Are Kafka messages produced? | `http://localhost:8090` |
| Is the connector running? | `http://localhost:8083/connectors/employee-events-elasticsearch-sink/status` |
| Are schemas registered? | `http://localhost:8085/subjects` |
| Did Elasticsearch receive records? | `http://localhost:9200/employee-events/_search?pretty` |
| Can I search visually? | `http://localhost:5601` |
| Are Grafana dashboards up? | `http://localhost:3000` |
| Is tracing working end to end? | `http://localhost:9411` |
| Are Prometheus scrape targets healthy? | `http://localhost:9090/targets` |
| Are auth endpoints working? | `http://localhost:8081/auth/login` or `https://localhost:8443/auth/login` |

## Best Daily Open Order

When you start your lab, this is a good browser order:

1. `http://localhost:8761`
2. `http://localhost:8090`
3. `http://localhost:3000`
4. `http://localhost:9411`
5. `http://localhost:5601`
6. `http://localhost:8081/swagger-ui.html`
7. `http://localhost:8082/swagger-ui.html`
