# Kafka Monitoring Guide

This project now has a local monitoring lab around Kafka.

## What is included

- Kafka broker
- Kafka UI
- Kafka exporter for Prometheus
- Prometheus
- Grafana
- Kafka Connect
- ksqlDB
- Spring Boot Actuator + Prometheus metrics in:
  - `employee-api`
  - `notification-service`

## Important URLs

- Kafka UI: `http://localhost:8080`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`
- Kafka Connect REST: `http://localhost:8083`
- ksqlDB server: `http://localhost:8088`
- Schema Registry: `http://localhost:8085`
- Employee API metrics: `http://localhost:8081/actuator/prometheus`
- Notification service metrics: `http://localhost:8082/actuator/prometheus`

## Grafana login

- Username: `admin`
- Password: `admin`

## What each tool helps you understand

- Kafka UI:
  inspect topics, partitions, messages, and consumer groups
- Prometheus:
  collect time-series metrics from Kafka exporter and your Spring apps
- Grafana:
  visualize request rate, JVM memory, topic offsets, and consumer lag
- Kafka Connect:
  manage connectors that move data between Kafka and external systems
- ksqlDB:
  run SQL-like stream queries on Kafka topics

## Files added in Docker setup

Folder: `C:\Docker_compose\kafka-setup`

- `docker-compose.yml`
- `prometheus/prometheus.yml`
- `grafana/provisioning/datasources/datasource.yml`
- `grafana/provisioning/dashboards/dashboards.yml`
- `grafana/dashboards/kafka-lab-overview.json`
- `kafka-connect/Dockerfile`
- `connectors/employee-sqlite-source.json`
- `ksql/employee-events.sql`

## What to monitor first

In Kafka:

- topic partitions
- topic offsets
- consumer lag
- partition growth imbalance

In Spring Boot apps:

- request count
- request rate
- JVM heap usage
- health endpoint state

## How to use it

1. Start Docker Desktop.
2. In `C:\Docker_compose\kafka-setup`, run `docker compose up -d`.
3. Start `employee-api`.
4. Start `notification-service`.
5. Open Grafana and Kafka UI.
6. Produce messages and watch:
   - partition offsets increase
   - consumer lag move up/down
   - Spring app metrics change

## Kafka Connect basics

- Kafka Connect is not your business app.
- It is a worker that runs connectors.
- A connector is a plugin job that reads from or writes to Kafka.

Useful checks:

- List connectors:
  `GET http://localhost:8083/connectors`
- Connector status:
  `GET http://localhost:8083/connectors/<name>/status`
- Example connector config in this lab:
  `C:\Docker_compose\kafka-setup\connectors\employee-sqlite-source.json`

## ksqlDB basics

- ksqlDB lets you treat Kafka topics like streams in SQL.
- You can inspect topics, create streams, and run continuous queries.

Useful checks:

- Server info:
  `GET http://localhost:8088/info`

Run CLI:

- `docker exec -it ksqldb-cli ksql http://ksqldb-server:8088`

Simple commands inside ksqlDB:

- `SHOW TOPICS;`
- `SHOW STREAMS;`
- `PRINT 'employee-events' FROM BEGINNING;`
- Example ksql file in this lab:
  `C:\Docker_compose\kafka-setup\ksql\employee-events.sql`

## Good learning exercises

- Stop `notification-service` and watch lag increase.
- Start it again and watch lag recover.
- Send many messages and compare partition offsets.
- Keep one consumer group and then create another group to see the difference.
- Compare Kafka UI message view with Grafana metrics view.

## Next upgrades if you want deeper Kafka operations

- add Kafka Connect
- add ksqlDB
- add a 3-broker cluster for real HA practice
- add alert rules in Prometheus/Grafana
- add JMX-based broker metrics
