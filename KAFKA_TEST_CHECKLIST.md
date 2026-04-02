# Kafka Test Checklist

Use this checklist to practice the full flow:

- `employee-api` produces schema-based JSON events
- Kafka stores them in partitions
- `notification-service` consumes them
- Kafka Connect sends them to Elasticsearch
- ksqlDB reads them as a stream
- Kibana shows indexed documents
- Prometheus and Grafana show metrics

## Endpoints

- Kafka UI: `http://localhost:8090`
- Grafana: `http://localhost:3000`
- Kibana: `http://localhost:5601`
- Elasticsearch: `http://localhost:9200`
- Kafka Connect: `http://localhost:8083`
- ksqlDB: `http://localhost:8088`
- employee-api: `http://localhost:8081`
- notification-service: `http://localhost:8082`

## Auth Prerequisite

Most `employee-api` endpoints are protected now.

First get an access token:

```text
POST http://localhost:8081/auth/login
```

```json
{
  "username": "salmauser",
  "password": "Password123"
}
```

Then use:

```text
Authorization: Bearer <access_token_here>
```

## Useful Commands

```powershell
cd C:\Docker_compose\kafka-setup
docker compose ps
```

```powershell
docker exec kafka /opt/kafka/bin/kafka-topics.sh --describe --topic employee-events --bootstrap-server localhost:9092
```

```powershell
docker exec kafka /opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group notification-group
```

## 1. Topic Check

Test case: Verify Kafka topic setup

Step:

```powershell
docker exec kafka /opt/kafka/bin/kafka-topics.sh --describe --topic employee-events --bootstrap-server localhost:9092
```

Expected result:

- `PartitionCount: 3`
- partitions `0`, `1`, `2`

Why it matters:

- confirms the topic is ready for parallel processing

## 2. App Health Check

Test case: Verify both apps are running

Step:

```powershell
Invoke-WebRequest http://localhost:8081/actuator/health -UseBasicParsing
Invoke-WebRequest http://localhost:8082/actuator/health -UseBasicParsing
```

Expected result:

- both return status `UP`

Why it matters:

- producer and consumer must be alive before testing flow

## 3. Manual Producer Test

Test case: Send one JSON message manually

Postman:

- Method: `POST`
- URL: `http://localhost:8081/employees/send?key=manual-emp-5001`
- Header:

```text
Authorization: Bearer <access_token_here>
```

- Body:

```json
{
  "action": "MANUAL",
  "empId": 5001,
  "name": "Manual User",
  "deptId": 50,
  "salary": 7500.0,
  "eventTimestamp": "2026-03-25T12:00:00Z"
}
```

Expected result:

- API returns `Message sent to Kafka topic`
- Kafka UI shows the message
- Elasticsearch indexes one document

Why it matters:

- proves the simplest producer path

## 4. Demo Producer Test

Test case: Send multiple messages across partitions

Postman:

- Method: `POST`
- URL: `http://localhost:8081/employees/send/demo?count=9`
- Header:

```text
Authorization: Bearer <access_token_here>
```

Expected result:

- response says `9 demo messages sent across 3 partitions`
- messages land across partitions `0`, `1`, `2`
- consumer group reads them

Why it matters:

- easiest way to understand partitions visually

## 5. Create Employee API Test

Test case: DB + Kafka integration on create

Postman:

- Method: `POST`
- URL: `http://localhost:8081/employees`
- Header:

```text
Authorization: Bearer <access_token_here>
```

- Body:

```json
{
  "empId": 6001,
  "firstName": "Salma",
  "lastName": "Test",
  "deptId": 12,
  "managerId": 101,
  "hireDate": "2026-03-18",
  "salary": 8200
}
```

Expected result:

- employee saved in DB
- Kafka event with `CREATED`
- Elasticsearch stores the event

Why it matters:

- shows business flow, not just demo flow

## 6. Update Employee Test

Test case: update triggers Kafka

Postman:

- Method: `PUT`
- URL: `http://localhost:8081/employees/6001`
- Header:

```text
Authorization: Bearer <access_token_here>
```

- Body:

```json
{
  "empId": 6001,
  "firstName": "Salma",
  "lastName": "Updated",
  "deptId": 22,
  "managerId": 101,
  "hireDate": "2026-03-18",
  "salary": 9000,
  "version": 0
}
```

Expected result:

- Kafka event with `UPDATED`
- Elasticsearch gets updated event record

Why it matters:

- shows change events in event-driven systems

## 7. Delete Employee Test

Test case: delete triggers Kafka

Postman:

- Method: `DELETE`
- URL: `http://localhost:8081/employees/6001`
- Header:

```text
Authorization: Bearer <access_token_here>
```

Expected result:

- Kafka event with `DELETED`
- consumer processes it
- Elasticsearch stores delete event as history

Why it matters:

- shows event trail, not just current DB state

## 8. Kafka UI Verification

Test case: inspect messages in Kafka UI

Step:

1. Open `http://localhost:8090`
2. Open `employee-events`
3. Open `Messages`
4. Open `Partitions`

Expected result:

- messages visible
- offsets increasing
- partitions visible separately

Why it matters:

- easiest visual place to understand Kafka basics

## 9. Consumer Group Verification

Test case: see 2 consumers sharing 3 partitions

Step:

```powershell
docker exec kafka /opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group notification-group
```

Expected result:

- partitions assigned across two consumers
- lag should be `0`

Why it matters:

- teaches consumer groups and partition ownership

## 10. ksqlDB Raw Stream Test

Test case: print Kafka messages in ksqlDB

Step:

```powershell
docker exec -i ksqldb-cli ksql http://ksqldb-server:8088 -e "PRINT 'employee-events' FROM BEGINNING LIMIT 6;"
```

Expected result:

- raw JSON messages printed
- partition shown

Why it matters:

- lets you inspect Kafka data without Java code

## 11. ksqlDB Structured Query Test

Test case: query JSON fields as columns

Step:

```powershell
docker exec -i ksqldb-cli ksql http://ksqldb-server:8088 -e "SET 'auto.offset.reset'='earliest'; SELECT EMPID, NAME, DEPTID, SALARY, EVENTTIMESTAMP FROM EMPLOYEE_EVENTS_JSON EMIT CHANGES LIMIT 5;"
```

Expected result:

- records shown as rows and columns

Why it matters:

- teaches how ksqlDB turns event streams into queryable data

## 12. Kafka Connect Status Test

Test case: verify Elasticsearch sink is healthy

Step:

```powershell
Invoke-WebRequest http://localhost:8083/connectors/employee-events-elasticsearch-sink/status -UseBasicParsing
```

Expected result:

- connector `RUNNING`
- task `RUNNING`

Why it matters:

- confirms Kafka Connect is moving data to Elasticsearch

## 13. Elasticsearch Document Test

Test case: confirm indexed records

Step:

```powershell
Invoke-WebRequest http://localhost:9200/employee-events/_search?pretty -UseBasicParsing
```

Expected result:

- documents contain:
  - `action`
  - `empId`
  - `name`
  - `deptId`
  - `salary`
  - `eventTimestamp`
  - `kafka_partition`
  - `kafka_offset`

Why it matters:

- proves sink connector is working

## 14. Elasticsearch Partition Check

Test case: confirm partition-aware indexing

Step:

```powershell
Invoke-WebRequest -Method Post -Uri http://localhost:9200/employee-events/_search?pretty -ContentType "application/json" -Body '{"size":0,"aggs":{"by_partition":{"terms":{"field":"kafka_partition","size":10}}}}' -UseBasicParsing
```

Expected result:

- counts per partition

Why it matters:

- shows that docs came from multiple Kafka partitions

## 15. Kibana Source Check

Test case: verify Kibana is linked to correct source

Step:

1. Open `http://localhost:5601`
2. Go to Discover
3. Select `employee-events*`
4. Search fields:
   - `kafka_partition`
   - `empId`
   - `action`

Expected result:

- documents visible from `employee-events`

Why it matters:

- proves Kibana is reading the right Elasticsearch index

## 16. Grafana Monitoring Test

Test case: verify monitoring

Step:

1. Open `http://localhost:3000`
2. Open `Kafka Lab Overview`
3. Produce `9` demo messages

Expected result:

- Spring request rate changes
- topic offsets per partition change

Why it matters:

- connects operations view with actual traffic

## 17. Bring Kafka Broker Down

Test case: Kafka outage

Command:

```powershell
cd C:\Docker_compose\kafka-setup
docker compose stop kafka
```

Expected result:

- producer requests fail
- consumer stops polling
- Connect and ksqlDB are affected

Why it matters:

- teaches what breaks when broker is unavailable

Bring it back:

```powershell
docker compose start kafka
```

## 18. Bring Elasticsearch Down

Test case: sink target outage

Command:

```powershell
cd C:\Docker_compose\kafka-setup
docker compose stop elasticsearch
```

Expected result:

- Kafka still accepts messages
- Elasticsearch sink may fail or retry
- Kibana will not show new docs

Why it matters:

- teaches that Kafka can buffer even if downstream storage fails

Bring it back:

```powershell
docker compose start elasticsearch
```

Check connector:

```powershell
Invoke-WebRequest http://localhost:8083/connectors/employee-events-elasticsearch-sink/status -UseBasicParsing
```

## 19. Bring Kafka Connect Down

Test case: connector platform outage

Command:

```powershell
cd C:\Docker_compose\kafka-setup
docker compose stop kafka-connect
```

Expected result:

- producer still works
- consumer still works
- Elasticsearch stops receiving new docs

Why it matters:

- separates Kafka core from Connect platform

Bring it back:

```powershell
docker compose start kafka-connect
```

## 20. Bring notification-service Down

Test case: consumer app failure

Command:

```powershell
Get-NetTCPConnection -LocalPort 8082 -State Listen | Select-Object OwningProcess
Stop-Process -Id <PID> -Force
```

Expected result:

- lag starts increasing if producer still sends messages

Why it matters:

- shows Kafka durability and lag behavior

Restart it:

```powershell
cd C:\Users\Salma\OneDrive\Documents\Java_Laranings\Unit_test_learning\src\notification-service
mvn spring-boot:run
```

## 21. Wrong Payload Format Test

Test case: send bad raw payload

Postman:

- Method: `POST`
- URL: `http://localhost:8081/employees/send/raw?key=bad-1`
- Header:

```text
Authorization: Bearer <access_token_here>
Content-Type: text/plain
```

- Body:

```text
hello-text
```

Expected result:

- Kafka accepts it
- `notification-service` retries and then moves it to `employee-events-error`
- Elasticsearch sink does not index it as a valid schema-based employee document

Why it matters:

- teaches the difference between raw Kafka bytes and valid schema-based messages

## Best Practice Order

1. Topic check
2. App health
3. Login and keep one access token ready
4. Manual producer
5. Demo producer
6. Kafka UI
7. Consumer group
8. ksqlDB print/query
9. Elasticsearch check
10. Kibana check
11. Grafana check
12. Failure tests

## Learning Map

- `employee-api` produces
- Kafka stores by partition
- `notification-service` consumes
- ksqlDB queries the stream
- Kafka Connect moves topic data to Elasticsearch
- Kibana searches documents
- Prometheus and Grafana monitor everything
