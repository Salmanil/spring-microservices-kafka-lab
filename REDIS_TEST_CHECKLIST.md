# Redis Test Checklist

## 1. Redis Container Up

Test case: Redis platform is available

Step:

```powershell
cd C:\Docker_compose\kafka-setup
docker compose up -d redis
docker compose ps
```

Expected result:

- `redis` is `Up`
- port `6379` is exposed

Why it matters:

- app caching and gateway rate limiting both depend on Redis

## 2. Auth Prerequisite

Most `employee-api` endpoints used below are protected.

Get an access token first:

```text
POST http://localhost:8081/auth/login
```

```json
{
  "username": "salmauser",
  "password": "Password123"
}
```

Use:

```text
Authorization: Bearer <access_token_here>
```

## 3. employee-api Uses Redis Cache

Test case: employee read cache by id

Step:

1. restart `employee-api`
2. call:

```text
GET http://localhost:8081/employees/9702
```

with header:

```text
Authorization: Bearer <access_token_here>
```

3. call the same URL again
4. open:

```text
http://localhost:8081/actuator/caches
```

Expected result:

- first call loads normally
- second call can be served from cache
- cache names include `employeeById`

Why it matters:

- confirms Redis-backed caching is active

## 4. Employee List Cache

Test case: cache the employee list

Step:

```text
GET http://localhost:8081/employees
```

with header:

```text
Authorization: Bearer <access_token_here>
```

Then call it again.

Expected result:

- repeated reads use `employeeList` cache

Why it matters:

- shows cache behavior for collections too

## 5. Cache Eviction On Create

Test case: create invalidates list cache

Step:

1. call `GET /employees`
2. create a new employee with bearer token:

```json
{
  "empId": 9961,
  "firstName": "Redis",
  "lastName": "Create",
  "deptId": 11,
  "managerId": 100,
  "hireDate": "2026-03-24",
  "salary": 7000
}
```

Expected result:

- create succeeds
- `employeeList` cache is refreshed or evicted
- employee id cache is updated for `9961`

Why it matters:

- proves write operations keep cache consistent

## 6. Cache Eviction On Update

Test case: update refreshes cache

Step:

1. read an employee by id
2. update the same employee with the current `version`

Expected result:

- update succeeds
- cached employee reflects new values

Why it matters:

- confirms cache is not stale after updates

## 7. Redis TTL Value

Test case: direct key/value with TTL

Step:

```text
POST http://localhost:8081/employees/redis/values?key=demo:user&value=salma&ttlSeconds=20
```

with header:

```text
Authorization: Bearer <access_token_here>
```

Then:

```text
GET http://localhost:8081/employees/redis/values/demo:user
```

Expected result:

- value is returned
- TTL is visible and positive

Why it matters:

- demonstrates expiring data in Redis

## 8. Redis TTL Expiry

Test case: value expires automatically

Step:

1. store a value with `ttlSeconds=5`
2. wait 6 to 8 seconds
3. read it again

Expected result:

- `exists` becomes `false`
- value becomes `null`

Why it matters:

- teaches TTL expiry behavior

## 9. Redis Counter Increment

Test case: atomic counter usage

Step:

```text
POST http://localhost:8081/employees/redis/counters/demo-counter/increment?ttlSeconds=30
```

with header:

```text
Authorization: Bearer <access_token_here>
```

Call it multiple times.

Expected result:

- value increases `1`, `2`, `3`...

Why it matters:

- counters are the core primitive behind many Redis patterns

## 10. Gateway Rate Limit Pass

Test case: requests under the limit

Step:

Call through the gateway with these headers:

```text
GET http://localhost:8080/api/employees/9702
X-Client-Id: salma-demo
Authorization: Bearer <access_token_here>
```

Repeat up to 5 times within one minute.

Expected result:

- responses succeed
- headers show remaining limit decreasing

Why it matters:

- confirms Redis-backed gateway limiting is active

## 11. Gateway Rate Limit Fail

Test case: too many requests

Step:

Send the same request more than 5 times inside one minute:

```text
GET http://localhost:8080/api/employees/9702
X-Client-Id: salma-demo
Authorization: Bearer <access_token_here>
```

Expected result:

- one request starts returning `429 Too Many Requests`

Why it matters:

- shows Redis rate limiting in action

## 12. Rate Limit Reset

Test case: limit resets after TTL window

Step:

1. exceed the limit
2. wait about 60 seconds
3. send the request again

Expected result:

- request succeeds again

Why it matters:

- proves the Redis window expires correctly

## 13. Separate Clients Get Separate Limits

Test case: rate limiting by client id

Step:

Call:

```text
GET http://localhost:8080/api/employees/9702
X-Client-Id: client-a
Authorization: Bearer <access_token_here>
```

and:

```text
GET http://localhost:8080/api/employees/9702
X-Client-Id: client-b
Authorization: Bearer <access_token_here>
```

Expected result:

- limits are tracked separately

Why it matters:

- shows how Redis stores per-client counters

## 14. Redis Down Failure

Test case: Redis outage

Step:

```powershell
cd C:\Docker_compose\kafka-setup
docker compose stop redis
```

Expected result:

- `employee-api` cache behavior may fail or log Redis errors
- gateway rate limiting may fail requests or startup depending on timing

Why it matters:

- helps you understand Redis as a dependency

Recovery:

```powershell
cd C:\Docker_compose\kafka-setup
docker compose start redis
```

Then restart:

- `employee-api`
- `api-gateway`

## 15. Wrong Port Failure

Test case: bad Redis configuration

Step:

Change Redis port in one app temporarily and restart it.

Expected result:

- app fails to connect to Redis

Why it matters:

- useful for diagnosing config mistakes

## 16. Bypass Gateway

Test case: rate limit not applied directly to service

Step:

Call directly:

```text
GET http://localhost:8081/employees/9702
Authorization: Bearer <access_token_here>
```

Expected result:

- gateway rate limiting does not apply

Why it matters:

- clarifies that rate limiting lives in `api-gateway`, not `employee-api`

## 17. Pub/Sub Publish Pass

Test case: publish and receive a pub/sub message

Step:

```text
POST http://localhost:8081/employees/redis/pubsub/publish?channel=channel:employee:alerts&payload=hello-redis
Authorization: Bearer <access_token_here>
```

Then:

```text
GET http://localhost:8081/employees/redis/pubsub/messages
Authorization: Bearer <access_token_here>
```

Expected result:

- recent messages include the published payload

Why it matters:

- shows pub/sub delivery with a live subscriber

## 18. Pub/Sub Wrong Channel Pattern

Test case: publish to a channel with no matching subscriber

Step:

```text
POST http://localhost:8081/employees/redis/pubsub/publish?channel=channel:other:test&payload=no-listener
Authorization: Bearer <access_token_here>
```

Then:

```text
GET http://localhost:8081/employees/redis/pubsub/messages
Authorization: Bearer <access_token_here>
```

Expected result:

- new payload does not appear in the stored subscriber list

Why it matters:

- shows that subscribers only receive matching channels

## 19. Stream Append Pass

Test case: append a Redis stream event

Step:

```text
POST http://localhost:8081/employees/redis/streams/events?eventType=CREATED&employeeId=9702&payload=employee-created
Authorization: Bearer <access_token_here>
```

Expected result:

- response contains a `recordId`

Why it matters:

- proves a stream event was persisted

## 20. Stream Read Pass

Test case: read latest stream events

Step:

```text
GET http://localhost:8081/employees/redis/streams/events?count=5
Authorization: Bearer <access_token_here>
```

Expected result:

- list includes recent stream records with fields and ids

Why it matters:

- confirms stream history is readable later

## 21. Stream Consumer Group Pass

Test case: create and use a stream consumer group

Step:

1. append at least one stream record
2. create group:

```text
POST http://localhost:8081/employees/redis/streams/groups/employee-group
Authorization: Bearer <access_token_here>
```

3. read with consumer:

```text
GET http://localhost:8081/employees/redis/streams/groups/employee-group/consumers/consumer-a?count=5
Authorization: Bearer <access_token_here>
```

Expected result:

- group creation reports created or already exists
- consumer read returns records

Why it matters:

- teaches stream group mechanics

## 22. Stream Consumer Group Fail

Test case: create group before the stream exists

Step:

Use a fresh Redis instance or clear the stream, then call:

```text
POST http://localhost:8081/employees/redis/streams/groups/new-group
Authorization: Bearer <access_token_here>
```

Expected result:

- response shows `created=false` with an error message

Why it matters:

- teaches the dependency on stream existence

## 23. Lock Acquire Pass

Test case: acquire a distributed lock

Step:

```text
POST http://localhost:8081/employees/redis/locks/payroll-job/acquire?ttlSeconds=30
Authorization: Bearer <access_token_here>
```

Expected result:

- `acquired=true`
- response includes `ownerToken`

Why it matters:

- proves the lock was created in Redis

## 24. Lock Contention Fail

Test case: second client cannot acquire the same lock immediately

Step:

1. acquire the lock once
2. immediately call acquire again for the same lock name

Expected result:

- second response shows `acquired=false`

Why it matters:

- demonstrates lock contention

## 25. Lock Release Pass

Test case: release lock with correct owner token

Step:

```text
POST http://localhost:8081/employees/redis/locks/payroll-job/release?ownerToken=PASTE_TOKEN_HERE
Authorization: Bearer <access_token_here>
```

Expected result:

- `released=true`

Why it matters:

- verifies safe release by owner

## 26. Lock Release Fail

Test case: release lock with wrong owner token

Step:

```text
POST http://localhost:8081/employees/redis/locks/payroll-job/release?ownerToken=wrong-token
Authorization: Bearer <access_token_here>
```

Expected result:

- `released=false`

Why it matters:

- prevents one client from releasing another client's lock

## 27. Lock TTL Expiry

Test case: lock expires automatically

Step:

1. acquire lock with `ttlSeconds=5`
2. wait 6 to 8 seconds
3. inspect the lock

Expected result:

- `locked=false`

Why it matters:

- TTL protects against stuck locks

## 28. Critical Section Simulation

Test case: hold and release a lock around simulated work

Step:

```text
POST http://localhost:8081/employees/redis/locks/payroll-job/critical-section?ttlSeconds=30&holdMs=3000
Authorization: Bearer <access_token_here>
```

Expected result:

- response shows `acquired=true`
- response shows `released=true`

Why it matters:

- shows a typical lock usage pattern around critical work
