# Distributed Tracing Guide

This guide explains what distributed tracing is, what was added to this project, and how to use it for learning.

## What Distributed Tracing Means

Distributed tracing helps you follow one request across multiple services.

Instead of only seeing:

- one log in `api-gateway`
- one log in `employee-api`
- one log in `notification-service`

you can follow one trace id across all of them.

That gives you:

- one request
- many spans
- one end-to-end story

## What Was Added

Tracing was added to:

- [employee-api/pom.xml](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/employee-api/pom.xml)
- [notification-service/pom.xml](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/notification-service/pom.xml)
- [api-gateway/pom.xml](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/api-gateway/pom.xml)

Key dependencies:

- `micrometer-tracing-bridge-brave`
- `zipkin-reporter-brave`

Tracing properties were added to:

- [employee-api application.properties](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/employee-api/src/main/resources/application.properties)
- [notification-service application.properties](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/notification-service/src/main/resources/application.properties)
- [api-gateway application.properties](C:/Users/Salma/OneDrive/Documents/Java_Laranings/Unit_test_learning/src/api-gateway/src/main/resources/application.properties)

Important settings:

- `management.tracing.sampling.probability=1.0`
- `management.zipkin.tracing.endpoint=http://localhost:9411/api/v2/spans`
- `logging.pattern.correlation=[%X{traceId:-},%X{spanId:-}]`

Kafka observation was also enabled in the services that produce or consume Kafka:

- `spring.kafka.template.observation-enabled=true`
- `spring.kafka.listener.observation-enabled=true`

## Zipkin

Zipkin was added to the Docker stack in:

- [docker-compose.yml](C:/Docker_compose/kafka-setup/docker-compose.yml)

Open Zipkin here:

- [http://localhost:9411](http://localhost:9411)

## What You Should See

### HTTP tracing

If you call:

- `api-gateway -> employee-api`

you should see spans for:

- gateway request
- downstream employee-api request

### Service-to-service tracing

If `employee-api` calls `notification-service` through `RestClient`, the trace should continue into:

- `notification-service`

### Kafka tracing

If a request causes Kafka publish and consume, you should see spans related to:

- Kafka producer send
- Kafka consumer receive

This is why Kafka observation was enabled.

## Best Learning Flow

1. Start Docker stack including Zipkin.
2. Start:
   - `service-registry`
   - `employee-api`
   - `notification-service`
   - `api-gateway`
3. Call one request through the gateway.
4. Open Zipkin and search traces.
5. Open app logs and compare the trace id in logs with the trace shown in Zipkin.

## What To Open

- Zipkin: [http://localhost:9411](http://localhost:9411)
- Gateway: [http://localhost:8080](http://localhost:8080)
- Employee API: [http://localhost:8081](http://localhost:8081)
- Notification Service: [http://localhost:8082](http://localhost:8082)

## Why This Is Different From Metrics

Metrics answer:

- how many requests?
- how much memory?
- what is request rate?

Tracing answers:

- which exact request failed?
- where did time get spent?
- which service was called next?
- which spans belong to the same user action?

## Simple Mental Model

- Prometheus/Grafana = "system health and numbers"
- Kibana = "search logs/documents"
- Zipkin = "follow one request journey"

## Current Limitation

This setup gives you practical local tracing for learning.

It is not yet a full OpenTelemetry platform with:

- Jaeger
- Tempo
- advanced trace analytics
- custom span attributes everywhere

But it is a solid real tracing setup for this project.
