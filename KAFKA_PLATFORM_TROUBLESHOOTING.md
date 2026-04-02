# Kafka Platform Troubleshooting

Use this guide when Kafka, Kafka Connect, Elasticsearch, Kibana, Grafana, or the Spring services stop behaving the way you expect.

## 1. Quick Mental Model

There are 4 separate paths in this lab:

- `employee-api` -> database
- `employee-api` -> Kafka
- `notification-service` <- Kafka
- Kafka Connect -> Elasticsearch -> Kibana

That means one path can fail while another still works.

Example:

- Kafka can still produce and `notification-service` can still consume
- but Elasticsearch can fail if Kafka Connect is down or the sink task is broken

## 2. First Checks

### Check Spring apps

Open:

- `http://localhost:8081/actuator/health`
- `http://localhost:8082/actuator/health`
- `http://localhost:8080/actuator/health`
- `http://localhost:8761`

Expected:

- all services show `UP`
- Eureka shows `EMPLOYEE-API`, `NOTIFICATION-SERVICE`, `API-GATEWAY`

### Check Docker stack

```powershell
cd C:\Docker_compose\kafka-setup
docker compose ps
```

Expected:

- `kafka`
- `schema-registry`
- `kafka-connect`
- `kafka-ui`
- `elasticsearch`
- `kibana`
- `prometheus`
- `grafana`
- `ksqldb-server`

## 3. If Kafka UI Shows Connector But Elasticsearch Does Not Update

### Symptom

- Kafka messages are produced
- `notification-service` consumes them
- Elasticsearch does not show new documents

### Check connector status

```powershell
Invoke-WebRequest -UseBasicParsing http://localhost:8083/connectors/employee-events-elasticsearch-sink/status | Select-Object -ExpandProperty Content
```

What to look for:

- `connector.state = RUNNING`
- `task.state = RUNNING`

### Common failure

If you see `Unknown magic byte`, it means Kafka Connect found a record that was not produced with Schema Registry JSON schema.

That usually happens when:

- an old plain string message is still in the topic
- someone produced a dummy raw message outside the app contract

### Fix

Restart the connector:

```powershell
Invoke-WebRequest -UseBasicParsing -Method Post http://localhost:8083/connectors/employee-events-elasticsearch-sink/restart
```

If it keeps failing, check logs:

```powershell
docker logs --tail=150 kafka-connect
```

Current lab behavior:

- the sink is configured to tolerate bad records
- bad records should go to `employee-events-dlq`
- good schema-based records should continue flowing

### Verify Elasticsearch

```powershell
Invoke-WebRequest -UseBasicParsing http://localhost:9200/employee-events/_search?pretty | Select-Object -ExpandProperty Content
```

## 4. If Kafka Connect Is Missing In Kafka UI

### Symptom

- Kafka UI opens
- `Kafka Connect` page does not show the sink

### Check connector list directly

```powershell
Invoke-WebRequest -UseBasicParsing http://localhost:8083/connectors | Select-Object -ExpandProperty Content
```

If empty:

- the Connect worker is up
- but the connector was not created

### Fix

```powershell
$body = Get-Content C:\Docker_compose\kafka-setup\connectors\employee-events-elasticsearch-sink.json -Raw
Invoke-WebRequest -UseBasicParsing -Method Post -Uri http://localhost:8083/connectors -ContentType "application/json" -Body $body
```

Then refresh Kafka UI.

### Root cause that happened in this lab

The `connect-init` container was checking the connector with plain `curl`.

Problem:

- `curl` does not fail on HTTP `404` by default
- so the script thought the connector already existed
- and it exited without creating it

The fix was:

- use `curl -f` in the `connect-init` check inside [docker-compose.yml](C:/Docker_compose/kafka-setup/docker-compose.yml)

This is the pattern to remember:

- Connect worker `UP`
- connector list empty
- Kafka UI shows Kafka Connect but no sink
- then the problem is usually connector creation, not the Connect worker itself

## 5. If Grafana Shows No Data In Kafka Panels

### Symptom

- Spring app graphs show data
- Kafka-related panels show `No data`

### What it usually means

- `kafka-exporter` is down
- or the Grafana query is asking for a metric that does not exist

### Check exporter target

Open:

- `http://localhost:9090/targets`

Look for:

- `kafka-exporter` should be `UP`

### Fix exporter

```powershell
cd C:\Docker_compose\kafka-setup
docker compose up -d kafka-exporter
```

### For app Kafka metrics

In this lab, the useful Spring Kafka metrics are:

- `spring_kafka_template_seconds_count`
- `spring_kafka_listener_seconds_count`

The dashboard was updated to use these real metric names.

### Verify metrics directly

Open:

- `http://localhost:8081/actuator/prometheus`
- `http://localhost:8082/actuator/prometheus`

Search for:

- `spring_kafka_template_seconds_count`
- `spring_kafka_listener_seconds_count`

## 6. If Demo Producer Fails

### Symptom

- `POST /employees/send/demo?count=6` fails

### Common reason

- topic `employee-events` has only `1` partition
- but the demo producer tries to send to partitions `0`, `1`, `2`

### Check topic

```powershell
docker exec kafka /opt/kafka/bin/kafka-topics.sh --describe --topic employee-events --bootstrap-server localhost:9092
```

Expected:

- `PartitionCount: 3`

### Fix

```powershell
docker exec kafka /opt/kafka/bin/kafka-topics.sh --alter --topic employee-events --partitions 3 --bootstrap-server localhost:9092
```

## 7. If Notification Service Consumes Only Some Records

### Symptom

- Kafka topic offsets increase on all partitions
- but `notification-service` only shows part of the records

### Check group assignment

```powershell
docker exec kafka /opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group notification-group
```

Expected:

- partitions assigned across both consumers

### Fix

Restart the service so the two listeners rebalance:

```powershell
cd C:\Users\Salma\OneDrive\Documents\Java_Laranings\Unit_test_learning\src\notification-service
mvn spring-boot:run
```

If already running, restart it once.

## 8. If Kibana Does Not Show Newest Records First

### Symptom

- records appear
- but order is confusing
- newest event is not at top

### What to check

- Elasticsearch template exists
- Kibana index pattern exists
- time field is `eventTimestamp`

### Check Elasticsearch template

```powershell
Invoke-WebRequest -UseBasicParsing http://localhost:9200/_index_template/employee-events-template?pretty | Select-Object -ExpandProperty Content
```

### Check Kibana index pattern

```powershell
Invoke-WebRequest -UseBasicParsing http://localhost:5601/api/saved_objects/_find?type=index-pattern -Headers @{'kbn-xsrf'='true'} | Select-Object -ExpandProperty Content
```

Expected:

- `employee-events*`
- `timeFieldName = eventTimestamp`

## 9. If ksqlDB Shows Only Header And No Rows

### Symptom

- query runs
- only column names show

### Reason

- ksqlDB push queries wait for new data unless you ask to read earlier offsets

### Use this

```powershell
docker exec -i ksqldb-cli ksql http://ksqldb-server:8088 -e "SET 'auto.offset.reset'='earliest'; SELECT * FROM EMPLOYEE_EVENTS_JSON EMIT CHANGES LIMIT 5;"
```

## 10. If Database Insert Fails But Kafka Still Moves

### Important note

These are different endpoints:

- `POST /employees`
  - writes to DB
  - then publishes Kafka event
- `POST /employees/send`
  - only produces to Kafka
  - does not write to DB
- `POST /employees/send/demo`
  - only produces demo Kafka records
  - does not write to DB

So if DB did not change but Kafka did, check which endpoint you called first.

## 11. Most Useful Recovery Commands

Restart connector:

```powershell
Invoke-WebRequest -UseBasicParsing -Method Post http://localhost:8083/connectors/employee-events-elasticsearch-sink/restart
```

Restart Kafka Connect:

```powershell
cd C:\Docker_compose\kafka-setup
docker compose restart kafka-connect
```

Restart Kafka exporter:

```powershell
docker compose up -d kafka-exporter
```

Restart Kibana:

```powershell
docker compose restart kibana
```

Restart Spring app:

```powershell
cd C:\Users\Salma\OneDrive\Documents\Java_Laranings\Unit_test_learning\src\notification-service
mvn spring-boot:run
```

## 12. Best Debug Order

When something breaks, use this order:

1. Check the exact endpoint or UI symptom
2. Check app health
3. Check Docker services
4. Check Kafka topic/consumer group
5. Check connector status
6. Check Elasticsearch documents
7. Check Kibana index pattern
8. Check Prometheus targets
9. Check Grafana panels

This order usually finds the issue faster than jumping straight into logs.
