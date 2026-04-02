# Microservice Flow Guide

This project now has these moving parts:

- `service-registry`: Eureka server
- `api-gateway`: single entry point to the services
- `employee-api`: writes employee data and produces Kafka events
- `notification-service`: consumes Kafka events and exposes a small read API
- Kafka + Schema Registry + Kafka Connect + Elasticsearch + Kibana

## 1. Request Flow

For business APIs, the usual path is:

1. Client calls `api-gateway`
2. `api-gateway` looks up the target service in Eureka
3. The request is forwarded to `employee-api` or `notification-service`
4. `employee-api` saves to the database first
5. After save/update/delete, `employee-api` publishes an `EmployeeEvent` to Kafka
6. `notification-service` consumes the same Kafka event
7. Kafka Connect also consumes the same event and writes it to Elasticsearch
8. Kibana reads from Elasticsearch
9. Prometheus/Grafana read metrics from the apps and Kafka side

## 2. What Each Service Is Doing

### `service-registry`

- Runs Eureka on `http://localhost:8761`
- Keeps a live list of registered services
- Lets the gateway route by service name instead of hardcoded host/port

### `api-gateway`

- Runs on `http://localhost:8080`
- Routes:
  - `/api/employees/**` -> `employee-api`
  - `/api/notifications/**` -> `notification-service`
  - `/api/docs/employee/**` -> employee OpenAPI docs
  - `/api/docs/notification/**` -> notification OpenAPI docs

### `employee-api`

- Runs on `http://localhost:8081`
- Handles create/update/delete for employees
- Produces Kafka messages using JSON Schema serializer
- Registers itself in Eureka
- Exposes Swagger/OpenAPI and Prometheus metrics

### `notification-service`

- Runs on `http://localhost:8082`
- Consumes `employee-events`
- Has 2 consumers in the same group, so Kafka splits partitions between them
- Stores recent consumed events in memory for learning
- Exposes a small API and Prometheus metrics

## 3. Kafka Flow

The event payload is now a Java object called `EmployeeEvent`.

Producer side:

- `employee-api` serializes `EmployeeEvent`
- Schema Registry stores the schema
- Kafka stores the record in a topic partition

Consumer side:

- `notification-service` deserializes the same schema back into `EmployeeEvent`
- Kafka Connect reads the same topic
- Elasticsearch sink stores the event as a document

## 4. Why Schema Registry Matters Here

Before this change, Kafka messages were plain strings.

Now:

- the producer sends schema-backed JSON
- Schema Registry tracks the message structure
- consumers and connectors can safely deserialize the value

This helps you learn:

- schema compatibility
- payload validation expectations
- why random plain text like `hello` is not a valid business event anymore

## 5. Why Kibana Was Overwriting Documents

The Elasticsearch sink uses the Kafka key as the Elasticsearch document id.

So:

- same key -> same document id
- later event with same key -> document gets updated

That means Kibana behaves more like "latest state for this key" than an append-only event log.

## 6. Why Timestamp Matters

The event now includes `eventTimestamp`.

We also added:

- Elasticsearch index template mapping `eventTimestamp` as `date`
- Kibana data view setup that uses `eventTimestamp` as the time field

That is what lets Kibana sort with newest events first in Discover.

## 7. OpenAPI URLs

Direct service docs:

- `http://localhost:8081/swagger-ui.html`
- `http://localhost:8082/swagger-ui.html`

Direct OpenAPI JSON:

- `http://localhost:8081/v3/api-docs`
- `http://localhost:8082/v3/api-docs`

Through gateway:

- `http://localhost:8080/api/docs/employee/v3/api-docs`
- `http://localhost:8080/api/docs/notification/v3/api-docs`

## 8. Simple Mental Model

- Eureka answers: "where is the service?"
- Gateway answers: "how does the client enter the system?"
- Kafka answers: "how do services react to events?"
- Schema Registry answers: "what is the event structure?"
- Kafka Connect answers: "how does the topic data move to Elasticsearch?"
- Kibana answers: "what did Elasticsearch store?"
- Grafana answers: "how healthy and busy is everything?"
