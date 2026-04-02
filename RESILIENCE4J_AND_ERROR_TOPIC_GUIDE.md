# Resilience4j And Kafka Error Topic Guide

This guide explains the new learning flow added to the project.

## What Was Added

### Resilience4j learning flow

In `employee-api`, a new endpoint now calls `notification-service` through Eureka with:

- retry
- timeout
- circuit breaker
- fallback response

Endpoint:

- `GET /employees/resilience/notifications`

### Kafka bad message flow

In `notification-service`, bad Kafka messages are now:

1. retried 3 times
2. sent to error topic `employee-events-error`
3. stored in an in-memory failure view

Endpoints:

- `POST /employees/send/raw`
- `GET /notifications/errors`

## 1. Resilience4j Endpoints

### Normal success

Open:

```text
http://localhost:8081/employees/resilience/notifications?mode=ok
```

Expected:

```json
{
  "service": "notification-service",
  "mode": "ok",
  "status": "UP"
}
```

Meaning:

- call succeeded
- no retry needed
- no fallback used

### Timeout + fallback

Open:

```text
http://localhost:8081/employees/resilience/notifications?mode=slow&delayMs=3500
```

Expected:

```json
{
  "status": "FALLBACK",
  "mode": "slow",
  "delayMs": 3500,
  "reason": "TimeoutException",
  "service": "employee-api"
}
```

Meaning:

- `notification-service` was intentionally slow
- `TimeLimiter` cut the call off
- fallback response came from `employee-api`

### Failure + fallback

Open:

```text
http://localhost:8081/employees/resilience/notifications?mode=fail
```

Expected:

- fallback response with failure reason

Meaning:

- downstream service returned an error
- retry/circuit breaker logic wrapped the call
- fallback protected the caller

## 2. Notification Demo Endpoints

These are in `notification-service`.

### Normal

```text
http://localhost:8082/notifications/demo/ok
```

### Slow

```text
http://localhost:8082/notifications/demo/slow?delayMs=3500
```

### Fail

```text
http://localhost:8082/notifications/demo/fail
```

## 3. Kafka Error Topic Flow

### Valid schema message

This goes to Kafka normally and should be consumed normally:

Postman:

- Method: `POST`
- URL:

```text
http://localhost:8081/employees/send?key=emp-9301
```

- Body:

```json
{
  "action": "MANUAL",
  "empId": 9301,
  "name": "Valid Event",
  "deptId": 33,
  "salary": 9300,
  "eventTimestamp": "2026-03-21T12:50:00Z"
}
```

Expected:

- producer succeeds
- `notification-service` consumes it
- Elasticsearch stores it

### Invalid raw message

This is for learning only.

Postman:

- Method: `POST`
- URL:

```text
http://localhost:8081/employees/send/raw?key=bad-hello
```

- Body as raw text:

```text
hello
```

Expected:

- Kafka accepts it
- `notification-service` cannot deserialize it as schema message
- Spring Kafka retries 3 times
- then it sends the record to `employee-events-error`
- it appears in:

```text
http://localhost:8082/notifications/errors
```

Example result:

```json
{
  "key": "bad-hello",
  "payload": "hello",
  "errorClass": "org.springframework.kafka.support.serializer.DeserializationException",
  "errorMessage": "failed to deserialize",
  "originalTopic": "employee-events"
}
```

## 4. Where To See What Happened

### Main Kafka topic

Kafka UI:

- `http://localhost:8090`
- Topic: `employee-events`

### Error topic

Kafka UI:

- `http://localhost:8090`
- Topic: `employee-events-error`

### Failed records inside app

Open:

```text
http://localhost:8082/notifications/errors
```

### Successfully consumed records

Open:

```text
http://localhost:8082/notifications/events
```

### Elasticsearch

Open:

```text
http://localhost:9200/employee-events/_search?pretty
```

### Grafana

Open:

```text
http://localhost:3000
```

Dashboard:

- `Kafka Lab Overview`

## 5. Important Learning Point

These two endpoints are different:

- `POST /employees`
  - writes to DB
  - then produces Kafka event

- `POST /employees/send`
  - only produces Kafka event
  - does not insert into DB

- `POST /employees/send/raw`
  - only produces raw text
  - used to simulate bad format
  - does not insert into DB

So if DB did not change, first check whether you called:

- `/employees`
or
- `/employees/send`
or
- `/employees/send/raw`

## 6. If Something Fails

### Resilience endpoint not responding

Check:

- `http://localhost:8081/actuator/health`
- `http://localhost:8082/actuator/health`
- `http://localhost:8761`

### Error topic not receiving bad messages

Check:

- `notification-service` is running
- `employee-events-error` topic exists
- `http://localhost:8082/notifications/errors`

### Elasticsearch not receiving valid schema records

Check:

```powershell
Invoke-WebRequest -UseBasicParsing http://localhost:8083/connectors/employee-events-elasticsearch-sink/status | Select-Object -ExpandProperty Content
```

Expected:

- task state `RUNNING`

### Grafana Kafka rate panels empty

The dashboard now uses:

- `spring_kafka_template_seconds_count`
- `spring_kafka_listener_seconds_count`

If panels still show nothing:

1. produce a few new messages
2. wait 10 to 20 seconds
3. refresh Grafana

These are rate panels, so no recent traffic can look like zero.

## 7. Best Practice Test Order

1. call `mode=ok`
2. call `mode=slow`
3. call `mode=fail`
4. send one valid schema Kafka message
5. send one raw bad message like `hello`
6. check `/notifications/errors`
7. check Kafka UI topics
8. check Elasticsearch
9. check Grafana

## 8. What You Learned From This

- retry helps with temporary failures
- timeout stops slow downstream calls
- circuit breaker prevents repeated stress on a failing dependency
- fallback gives a safe response to the caller
- Kafka topics can carry both valid and bad records
- consumers need error handling
- bad records should be isolated in an error topic instead of crashing the whole flow
