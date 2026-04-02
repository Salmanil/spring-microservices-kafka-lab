# Microservice Pass/Fail Checklist

Use this in order.

## Start Order

Spring apps:

1. `service-registry`
2. `employee-api`
3. `notification-service`
4. `api-gateway`

Docker stack:

```powershell
cd C:\Docker_compose\kafka-setup
docker compose up -d
```

## Auth Prerequisite

`employee-api` is protected now.

Get a token first:

```text
POST http://localhost:8081/auth/login
```

```json
{
  "username": "salmauser",
  "password": "Password123"
}
```

Use this header on protected employee calls:

```text
Authorization: Bearer <access_token_here>
```

## Pass Cases

### 1. Eureka Registration

Step:

- Open `http://localhost:8761`

Expected:

- `EMPLOYEE-API`
- `NOTIFICATION-SERVICE`
- `API-GATEWAY`

Why it matters:

- proves service discovery is working

### 2. Gateway Route to Notification Service

Step:

- Open `http://localhost:8080/api/notifications/health`

Expected:

```json
{"status":"UP","service":"notification-service"}
```

Why it matters:

- proves gateway + Eureka routing are working

### 3. OpenAPI JSON Through Gateway

Step:

- Open `http://localhost:8080/api/docs/employee/v3/api-docs`
- Open `http://localhost:8080/api/docs/notification/v3/api-docs`

Expected:

- OpenAPI JSON loads from both paths

Why it matters:

- proves docs are exposed and reachable through the gateway

### 4. Produce One Event Through Employee API

Postman:

- Method: `POST`
- URL: `http://localhost:8081/employees/send?key=emp-7001`
- Header:

```text
Authorization: Bearer <access_token_here>
```

- Body:

```json
{
  "action": "MANUAL",
  "empId": 7001,
  "name": "Salma Kafka",
  "deptId": 10,
  "salary": 9000,
  "eventTimestamp": "2026-03-19T12:30:00Z"
}
```

Expected:

- API returns success
- Kafka UI shows the message
- `notification-service` consumes it
- Elasticsearch stores it

Why it matters:

- validates the full event path

### 5. Produce Demo Partition Traffic

Postman:

- Method: `POST`
- URL: `http://localhost:8081/employees/send/demo?count=9`
- Header:

```text
Authorization: Bearer <access_token_here>
```

Expected:

- events are spread across partitions `0`, `1`, `2`
- consumer group shares the work
- Grafana offsets and rates move

Why it matters:

- teaches partitions and consumer-group balancing

### 6. Check Recent Consumed Events

Step:

- Open `http://localhost:8082/notifications/events`

Expected:

- recent `EmployeeEvent` objects are listed

Why it matters:

- confirms consumer deserialization works with Schema Registry

### 7. Check Connector Status

Command:

```powershell
Invoke-WebRequest -UseBasicParsing http://localhost:8083/connectors/employee-events-elasticsearch-sink/status | Select-Object -ExpandProperty Content
```

Expected:

- connector state `RUNNING`
- task state `RUNNING`

Why it matters:

- proves Kafka Connect is healthy

### 8. Check Schema Registry Subjects

Command:

```powershell
Invoke-WebRequest -UseBasicParsing http://localhost:8085/subjects | Select-Object -ExpandProperty Content
```

Expected:

- a subject related to `employee-events-value`

Why it matters:

- proves schema-backed serialization is happening

### 9. Check Elasticsearch Document

Command:

```powershell
Invoke-WebRequest -UseBasicParsing http://localhost:9200/employee-events/_search?pretty | Select-Object -ExpandProperty Content
```

Expected:

- document contains:
  - `action`
  - `empId`
  - `name`
  - `deptId`
  - `salary`
  - `eventTimestamp`
  - `kafka_partition`
  - `kafka_offset`

Why it matters:

- proves sink indexing is working

### 10. Check Kibana Newest First

Step:

1. Open `http://localhost:5601`
2. Go to Discover
3. Select data view `employee-events`

Expected:

- time field is `eventTimestamp`
- latest events appear at the top

Why it matters:

- confirms timestamp mapping and Kibana data-view setup are correct

### 11. Check ksqlDB

Command:

```powershell
docker exec -i ksqldb-cli ksql http://ksqldb-server:8088 -e "SET 'auto.offset.reset'='earliest'; SELECT empId, name, deptId, salary, eventTimestamp FROM EMPLOYEE_EVENTS_JSON EMIT CHANGES LIMIT 5;"
```

Expected:

- rows appear when records exist

Why it matters:

- proves ksqlDB can read the schema-backed stream

### 12. Gateway Mutating Request Requires Idempotency Header

Test case:

- gateway should reject mutating employee calls without `Idempotency-Key`

Step:

call through gateway:

```text
POST http://localhost:8080/api/employees
```

with a valid bearer token but without `Idempotency-Key`

Expected:

- HTTP `400`
- message says `Idempotency-Key header is required`

Why it matters:

- proves gateway write protection is active

### 13. Gateway Mutating Request Passes With Idempotency Header

Test case:

- gateway allows write call when required header is present

Step:

```text
POST http://localhost:8080/api/employees
```

Headers:

```text
Authorization: Bearer <access_token_here>
Idempotency-Key: emp-create-1001
```

Expected:

- request reaches `employee-api`
- normal employee validation/business flow continues

Why it matters:

- shows gateway preprocessing before service logic

## Fail Cases

### 1. Kafka Down

Command:

```powershell
cd C:\Docker_compose\kafka-setup
docker compose stop kafka
```

Then test:

- send a POST to `employee-api`

Expected:

- producer call fails
- Connect and ksqlDB are impacted

Why it matters:

- shows Kafka is the event backbone

Recovery:

```powershell
docker compose start kafka
docker compose restart schema-registry kafka-connect ksqldb-server
```

### 2. Schema Registry Down

Command:

```powershell
cd C:\Docker_compose\kafka-setup
docker compose stop schema-registry
```

Then test:

- send a POST to `employee-api`

Expected:

- schema-based producer should fail to serialize

Why it matters:

- teaches the dependency introduced by schema-backed messaging

Recovery:

```powershell
docker compose start schema-registry
```

### 3. Kafka Connect Down

Command:

```powershell
cd C:\Docker_compose\kafka-setup
docker compose stop kafka-connect
```

Then test:

- send a POST to `employee-api`

Expected:

- Kafka still receives the message
- `notification-service` still consumes it
- Elasticsearch does not get the new document

Why it matters:

- shows Kafka Connect is independent from your app consumer

Recovery:

```powershell
docker compose start kafka-connect
```

### 4. Elasticsearch Down

Command:

```powershell
cd C:\Docker_compose\kafka-setup
docker compose stop elasticsearch
```

Then test:

- send a POST to `employee-api`

Expected:

- Kafka still stores the event
- `notification-service` still consumes it
- sink task may fail or retry
- Kibana will not show the new event

Why it matters:

- shows downstream storage failure without Kafka data loss

Recovery:

```powershell
docker compose start elasticsearch
docker compose restart kibana kafka-connect
```

### 5. Invalid Payload

Postman:

- Method: `POST`
- URL: `http://localhost:8081/employees/send?key=bad-1`
- Header:

```text
Authorization: Bearer <access_token_here>
```

- Body:

```json
{
  "empId": "not-a-number"
}
```

Expected:

- request fails in the API layer

Why it matters:

- shows the app contract is stronger than sending random Kafka strings

### 6. Missing Bearer Token

Test case:

- protected `employee-api` endpoint should reject anonymous access

Step:

```text
POST http://localhost:8081/employees/send?key=no-auth
```

without `Authorization` header

Expected:

- request is rejected

Why it matters:

- confirms security was applied after the original Kafka setup

## Quick Troubleshooting

### Kafka panels in Grafana show no data

Check:

```powershell
cd C:\Docker_compose\kafka-setup
docker compose ps
```

If `kafka-exporter` is not healthy:

```powershell
docker compose up -d kafka-exporter
```

### ksqlDB query shows only headers

Use:

```powershell
docker exec -i ksqldb-cli ksql http://ksqldb-server:8088 -e "SET 'auto.offset.reset'='earliest'; SELECT * FROM EMPLOYEE_EVENTS_JSON EMIT CHANGES LIMIT 5;"
```

Reason:

- push queries usually wait for new data unless you set offset behavior

### Kibana does not sort newest first

Check:

- index template applied before indexing
- data view uses `eventTimestamp`
- recreate the `employee-events` index if old mappings still exist
