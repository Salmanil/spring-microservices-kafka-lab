# Distributed Tracing Test Checklist

## 1. Zipkin Up

Test case:

- verify Zipkin UI is available

Step:

- open [http://localhost:9411](http://localhost:9411)

Expected result:

- Zipkin home page opens

Why it matters:

- traces need a backend UI to be stored and viewed

## 2. Gateway To Employee Trace

Test case:

- trace a simple gateway request

Step:

1. get a valid access token
2. call:

```text
GET http://localhost:8080/api/employees/9702
Authorization: Bearer <access_token_here>
X-Client-Id: salma-trace
```

Expected result:

- request succeeds
- a trace appears in Zipkin
- spans should include gateway and employee-api

Why it matters:

- this is the simplest end-to-end HTTP tracing example

## 3. Employee API Direct Trace

Test case:

- trace a direct secured service request

Step:

```text
GET http://localhost:8081/employees/9702
Authorization: Bearer <access_token_here>
```

Expected result:

- request succeeds
- trace appears in Zipkin
- service name should be `employee-api`

Why it matters:

- helps compare direct tracing with gateway tracing

## 4. Service-To-Service Trace

Test case:

- trace employee-api calling notification-service

Step:

```text
GET http://localhost:8081/employees/resilience/notifications?mode=ok
Authorization: Bearer <access_token_here>
```

Expected result:

- request succeeds
- trace appears in Zipkin
- spans should include:
  - `employee-api`
  - outgoing client call
  - `notification-service`

Why it matters:

- proves trace propagation across HTTP service calls

## 5. Kafka Produce And Consume Trace

Test case:

- trace producer and consumer activity

Step:

```text
POST http://localhost:8081/employees/send/demo?count=3
Authorization: Bearer <access_token_here>
```

Expected result:

- trace appears in Zipkin
- spans may include Kafka producer operations
- consumer-side spans should appear when `notification-service` receives records

Why it matters:

- this is the tracing view for asynchronous event flow

## 6. Compare Trace Id In Logs

Test case:

- verify trace ids are visible in logs

Step:

1. call any traced endpoint
2. check logs in:
   - `api-gateway`
   - `employee-api`
   - `notification-service`

Expected result:

- log lines include correlation section like:

```text
[traceId,spanId]
```

Why it matters:

- lets you connect logs with the Zipkin trace view

## 7. Anonymous Protected Request

Test case:

- unauthorized request behavior

Step:

```text
GET http://localhost:8081/employees/9702
```

Expected result:

- request fails with auth error
- a short trace may still be recorded

Why it matters:

- shows tracing works even for failed requests

## 8. Zipkin Down

Test case:

- tracing backend unavailable

Step:

1. stop Zipkin:

```powershell
cd C:\Docker_compose\kafka-setup
docker compose stop zipkin
```

2. call a traced endpoint

Expected result:

- app request should usually still work
- traces will not be visible in Zipkin

Why it matters:

- tracing should not break the business flow

## 9. Sampling Check

Test case:

- verify all local traces are sampled

Step:

- call several endpoints
- search in Zipkin

Expected result:

- traces appear consistently because:

```text
management.tracing.sampling.probability=1.0
```

Why it matters:

- good for local learning because nothing is randomly dropped

## 10. What To Check If You See No Traces

Step:

1. make sure Zipkin is running
2. make sure the app was restarted after tracing changes
3. confirm:
   - `employee-api`
   - `notification-service`
   - `api-gateway`
   are using the new code
4. call one endpoint again
5. refresh Zipkin search

Expected result:

- traces should appear

Why it matters:

- most tracing issues come from runtime restart gaps, not code mistakes
