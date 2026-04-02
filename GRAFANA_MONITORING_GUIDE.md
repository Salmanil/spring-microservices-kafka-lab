# Grafana Monitoring Guide

This project can show more in Grafana than just Kafka offsets and heap usage.

## What Grafana Can Show Right Now

Because Prometheus is already scraping your apps and Kafka exporter, Grafana can show:

- which monitored services are up or down
- Spring request rate
- Kafka producer send rate
- Kafka consumer receive rate
- topic offsets per partition
- JVM heap usage
- JVM live threads
- application CPU usage

## What "Monitored Targets Status" Means

This panel shows whether Prometheus can currently scrape these targets:

- `employee-api`
- `notification-service`
- `prometheus`
- `kafka-exporter`

If a target is:

- `Up` -> Prometheus can reach it
- `Down` -> Prometheus cannot scrape it

So this is your simplest "which service is currently running" view.

## What We Added

In the Grafana dashboard:

- `Monitored Targets Status`
- `JVM Live Threads`
- `Application CPU Usage`

These are useful for learning runtime behavior, not just Kafka behavior.

## Can Grafana Show Long-Running Queries?

Not reliably in the current setup.

Why:

- your app DB is SQLite
- Grafana only knows what Prometheus exposes
- right now there is no slow-query exporter or query-timing metric for SQLite

So Grafana cannot automatically know:

- which exact SQL query is running
- whether a SQL query is "long running"

## What Would Be Needed For Long-Running Query Visibility

To monitor slow or long-running DB queries, we would need extra instrumentation such as:

1. datasource-proxy or p6spy in the Spring app
2. custom Micrometer timer metrics around repository/service DB calls
3. move to a DB with better exporter support, like PostgreSQL, and scrape DB metrics

Then Grafana could show:

- slow query count
- average DB call time
- max DB call time
- queries crossing threshold like 2s or 5s

## Simple Rule

Grafana can only show:

- what Prometheus scrapes
- what your application/exporters publish as metrics

If something is not instrumented, Grafana cannot guess it.

## Good Extra Panels You Can Add Later

These are realistic next additions:

- HTTP 4xx/5xx error rate
- p95 request latency
- Redis availability
- Redis command rate
- Kafka Connect health
- Schema Registry availability
- Zipkin availability

## Best Learning Takeaway

Use Grafana for:

- live health visibility
- request rate
- throughput
- memory / CPU / threads
- Kafka behavior

Use logs / DB tools / Kibana for:

- exact failures
- individual records
- raw event details

Use tracing for:

- request journey across services
