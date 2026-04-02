# Redis Guide

## What Redis Is Doing In This Project

Redis is now used in these practical ways:

- caching employee reads in `employee-api`
- key/value and counter learning endpoints in `employee-api`
- gateway rate limiting in `api-gateway`
- pub/sub in `employee-api`
- streams in `employee-api`
- distributed locking in `employee-api`

This is a good learning mix because it shows both:

- application-side Redis usage
- gateway/platform-side Redis usage

## Docker

Redis runs in the Kafka platform stack:

- host: `localhost`
- port: `6379`

Start it:

```powershell
cd C:\Docker_compose\kafka-setup
docker compose up -d redis
```

## What Changed

### employee-api

Redis is used for:

- `employeeById` cache
- `employeeList` cache
- direct Redis learning endpoints

Important files:

- `C:\Users\Salma\OneDrive\Documents\Java_Laranings\Unit_test_learning\src\employee-api\src\main\java\com\example\employee_api\config\RedisConfig.java`
- `C:\Users\Salma\OneDrive\Documents\Java_Laranings\Unit_test_learning\src\employee-api\src\main\java\com\example\employee_api\service\EmployeeService.java`
- `C:\Users\Salma\OneDrive\Documents\Java_Laranings\Unit_test_learning\src\employee-api\src\main\java\com\example\employee_api\controller\RedisLearningController.java`
- `C:\Users\Salma\OneDrive\Documents\Java_Laranings\Unit_test_learning\src\employee-api\src\main\java\com\example\employee_api\service\RedisLearningService.java`

### api-gateway

Redis is used for:

- rate limiting requests to `/api/employees/**`

Important files:

- `C:\Users\Salma\OneDrive\Documents\Java_Laranings\Unit_test_learning\src\api-gateway\src\main\java\com\example\apigateway\filter\RedisRateLimitFilter.java`
- `C:\Users\Salma\OneDrive\Documents\Java_Laranings\Unit_test_learning\src\api-gateway\src\main\java\com\example\apigateway\config\RedisGatewayConfig.java`

## Restart Order After These Changes

1. start Redis
2. restart `employee-api`
3. restart `api-gateway`

Commands:

```powershell
cd C:\Docker_compose\kafka-setup
docker compose up -d redis
```

```powershell
cd C:\Users\Salma\OneDrive\Documents\Java_Laranings\Unit_test_learning\src\employee-api
mvn spring-boot:run
```

```powershell
cd C:\Users\Salma\OneDrive\Documents\Java_Laranings\Unit_test_learning\src\api-gateway
mvn spring-boot:run
```

## Employee Cache Behavior

### Cached read by id

When you call:

- `GET /employees/{id}`

the first request hits the database, then Redis stores the result.

The next same request can be served from Redis cache.

### Cached employee list

When you call:

- `GET /employees`

the employee list can be cached in Redis.

### Cache invalidation

When you call:

- `POST /employees`
- `PUT /employees/{id}`
- `DELETE /employees/{id}`

the relevant Redis cache entries are updated or cleared.

That keeps cache and DB consistent.

## Redis Learning Endpoints

These endpoints help you understand TTL and counters directly.

### Set a value with TTL

```text
POST http://localhost:8081/employees/redis/values?key=demo:user&value=salma&ttlSeconds=60
```

### Read a value

```text
GET http://localhost:8081/employees/redis/values/demo:user
```

### Increment a counter

```text
POST http://localhost:8081/employees/redis/counters/demo-counter/increment?ttlSeconds=60
```

### Delete a key

```text
DELETE http://localhost:8081/employees/redis/values/demo:user
```

## Gateway Rate Limiting

Requests through:

- `http://localhost:8080/api/employees/**`

are now limited using Redis.

Default rule:

- 5 requests
- per 60 seconds
- per client

Client identity is resolved from:

- `X-Client-Id` header if present
- otherwise remote IP

Response headers:

- `X-RateLimit-Limit`
- `X-RateLimit-Remaining`
- `X-RateLimit-Reset-Seconds`

When the limit is exceeded:

- HTTP `429 Too Many Requests`

## Why Redis Is Useful

### Caching

- reduces repeated DB calls
- improves read speed

### Rate limiting

- protects APIs from abuse
- shows how Redis can coordinate limits across requests

### TTL

- useful for expiring data automatically

### Counters

- useful for rate limiting, analytics, and temporary usage tracking

## Advanced Redis Patterns

### Pub/Sub

Pub/Sub is for fire-and-forget notifications.

In this project:

- publish endpoint sends a message to a Redis channel
- an in-app subscriber listens and stores the recent received messages in memory

Endpoints:

```text
POST http://localhost:8081/employees/redis/pubsub/publish?channel=channel:employee:alerts&payload=hello-salma
```

```text
GET http://localhost:8081/employees/redis/pubsub/messages
```

What it teaches:

- publisher and subscriber are loosely coupled
- Redis Pub/Sub does not persist history for you
- if no subscriber is listening at publish time, the message is effectively gone

### Streams

Redis Streams are better when you want append-only event records and later reads.

Endpoints:

```text
POST http://localhost:8081/employees/redis/streams/events?eventType=CREATED&employeeId=9702&payload=employee-created
```

```text
GET http://localhost:8081/employees/redis/streams/events?count=10
```

Create a consumer group:

```text
POST http://localhost:8081/employees/redis/streams/groups/employee-group
```

Read with a consumer:

```text
GET http://localhost:8081/employees/redis/streams/groups/employee-group/consumers/consumer-a?count=5
```

What it teaches:

- stream entries are persisted
- entries have record ids
- consumer groups let multiple consumers coordinate

### Distributed Lock

Distributed lock is useful when only one worker should do a critical job at a time.

Acquire a lock:

```text
POST http://localhost:8081/employees/redis/locks/payroll-job/acquire?ttlSeconds=30
```

Inspect a lock:

```text
GET http://localhost:8081/employees/redis/locks/payroll-job
```

Release a lock:

```text
POST http://localhost:8081/employees/redis/locks/payroll-job/release?ownerToken=PASTE_TOKEN_HERE
```

Simulate critical section:

```text
POST http://localhost:8081/employees/redis/locks/payroll-job/critical-section?ttlSeconds=30&holdMs=5000
```

What it teaches:

- only one holder should own the lock
- TTL prevents permanent deadlocks
- release must validate the owner token

## Useful Checks

Redis container:

```powershell
cd C:\Docker_compose\kafka-setup
docker compose ps
```

Spring cache actuator:

- `http://localhost:8081/actuator/caches`

Gateway health:

- `http://localhost:8080/actuator/health`

Employee health:

- `http://localhost:8081/actuator/health`

## Troubleshooting

### Redis is down

Symptoms:

- cache fails silently or app logs Redis connection errors
- rate limiting may fail the gateway startup or request flow

Fix:

```powershell
cd C:\Docker_compose\kafka-setup
docker compose up -d redis
```

### Rate limit does not trigger

Check:

- are you calling through gateway `8080`
- not directly through `8081`

### Cache does not seem active

Check:

- did you restart `employee-api`
- are you hitting the same employee id repeatedly
- is Redis container up

### Too many requests happens too fast

Check:

- repeated calls may share the same `X-Client-Id`
- wait for the reset window or change the header

### Stream consumer group creation fails

Cause:

- stream may not exist yet

Fix:

- append at least one stream event first
- then create the consumer group

### Lock release returns false

Cause:

- wrong `ownerToken`
- lock already expired

Fix:

- inspect the lock first
- use the exact owner token returned by acquire

## Guided Walkthrough

### 1. Cache hit

1. call `GET http://localhost:8081/employees/9702`
2. call it again
3. open `http://localhost:8081/actuator/caches`

Learning point:

- first request loads data normally
- second request is a cache candidate

### 2. TTL expiry

1. call:

```text
POST http://localhost:8081/employees/redis/values?key=demo:user&value=salma&ttlSeconds=10
```

2. immediately call:

```text
GET http://localhost:8081/employees/redis/values/demo:user
```

3. wait 11 seconds
4. call the same GET again

Learning point:

- Redis deletes expired keys automatically

### 3. Counter increment

1. call:

```text
POST http://localhost:8081/employees/redis/counters/demo-counter/increment?ttlSeconds=30
```

2. call it again and again

Learning point:

- Redis atomic increment is simple and very fast

### 4. Rate limit pass

1. send up to 5 requests through the gateway:

```text
GET http://localhost:8080/api/employees/9702
Header: X-Client-Id: salma-demo
```

Learning point:

- the headers show the window state

### 5. Rate limit fail

1. keep sending the same request more than 5 times within a minute

Expected:

- one request returns `429`

Learning point:

- Redis stores the window counter per client

### 6. Pub/Sub

1. publish:

```text
POST http://localhost:8081/employees/redis/pubsub/publish?channel=channel:employee:alerts&payload=welcome
```

2. view received messages:

```text
GET http://localhost:8081/employees/redis/pubsub/messages
```

Learning point:

- subscriber receives messages live

### 7. Streams

1. append a record
2. read latest records
3. create a consumer group
4. read as `consumer-a`

Learning point:

- stream records stay available for later reads

### 8. Distributed lock

1. acquire a lock
2. try acquiring the same lock again immediately
3. inspect it
4. release using the correct owner token

Learning point:

- only one owner should hold the lock at a time
