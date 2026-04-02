# SonarQube Guide

## What This Adds

SonarQube is a static code analysis tool. It checks source code for:

- bugs
- code smells
- vulnerabilities
- duplicated code
- maintainability issues

It is different from Grafana and Kafka UI:

- Grafana shows runtime metrics
- Kafka UI shows runtime Kafka data
- SonarQube shows code quality issues

## Docker Services

The Docker stack now includes:

- `sonarqube-db`
- `sonarqube`

URLs:

- SonarQube: `http://localhost:9000`
- PostgreSQL for SonarQube: local host port `5433`

Default startup notes:

- first startup can take a few minutes
- SonarQube depends on `sonarqube-db`

## Start SonarQube

Run this from `C:\Docker_compose\kafka-setup`:

```powershell
docker compose up -d sonarqube-db sonarqube
```

Check status:

```powershell
docker compose ps
```

Open:

- `http://localhost:9000`

Default login:

- username: `admin`
- password: `admin`

SonarQube will ask you to change the password on first login.

## Generate A Token

After login:

1. Go to `My Account`
2. Open `Security`
3. Generate a user token
4. Copy that token

You will use it in Maven scan commands.

## Recommended Projects To Scan

- `employee-api`
- `notification-service`
- `api-gateway`
- `service-registry`

## Maven Scan Commands

Run these from each project folder.

### employee-api

```powershell
mvn clean verify sonar:sonar "-Dsonar.projectKey=employee-api" "-Dsonar.projectName=employee-api" "-Dsonar.host.url=http://localhost:9000" "-Dsonar.token=YOUR_TOKEN"
```

### notification-service

```powershell
mvn clean verify sonar:sonar "-Dsonar.projectKey=notification-service" "-Dsonar.projectName=notification-service" "-Dsonar.host.url=http://localhost:9000" "-Dsonar.token=YOUR_TOKEN"
```

### api-gateway

```powershell
mvn clean verify sonar:sonar "-Dsonar.projectKey=api-gateway" "-Dsonar.projectName=api-gateway" "-Dsonar.host.url=http://localhost:9000" "-Dsonar.token=YOUR_TOKEN"
```

### service-registry

```powershell
mvn clean verify sonar:sonar "-Dsonar.projectKey=service-registry" "-Dsonar.projectName=service-registry" "-Dsonar.host.url=http://localhost:9000" "-Dsonar.token=YOUR_TOKEN"
```

## What To Look For In SonarQube

Open each project and review:

- `Issues`
- `Measures`
- `Code`
- `Activity`

Most useful tabs for learning:

- `Issues`: exact code problems
- `Measures`: duplication, coverage, maintainability
- `Code`: issue locations in files

## Simple Learning Flow

1. Start SonarQube
2. Scan `employee-api`
3. Open project dashboard
4. Review issue categories
5. Fix one issue
6. Scan again
7. Compare old and new results

## What SonarQube Does Not Do

SonarQube does not:

- consume Kafka messages
- show runtime application metrics
- replace Prometheus or Grafana
- replace Kafka UI

It only analyzes code and project structure.

## Common Problems

### SonarQube page does not open

Check:

```powershell
cd C:\Docker_compose\kafka-setup
docker compose ps
```

If `sonarqube` is not healthy yet, wait a little longer and recheck:

```powershell
docker compose logs --tail=100 sonarqube
```

### Scan fails with unauthorized

Cause:

- wrong or expired token

Fix:

- generate a new SonarQube token
- rerun the scan with the new token

### Scan fails because SonarQube is unreachable

Check:

- `http://localhost:9000`
- `docker compose ps`

### Project appears but shows few results

Cause:

- scan did not include compilation/tests properly

Fix:

- use `mvn clean verify sonar:sonar`

## Recommended Stop Command

```powershell
cd C:\Docker_compose\kafka-setup
docker compose stop sonarqube sonarqube-db
```
