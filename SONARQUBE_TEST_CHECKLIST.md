# SonarQube Test Checklist

## 1. SonarQube Starts

Test case: SonarQube container startup

Step:

```powershell
cd C:\Docker_compose\kafka-setup
docker compose up -d sonarqube-db sonarqube
docker compose ps
```

Expected result:

- `sonarqube-db` is `Up`
- `sonarqube` is `Up`
- `http://localhost:9000` opens

Why it matters:

- confirms the analysis platform is available

## 2. Login Works

Test case: First SonarQube login

Step:

1. Open `http://localhost:9000`
2. Login with `admin / admin`
3. change the password if prompted

Expected result:

- SonarQube home page loads

Why it matters:

- confirms the UI and authentication are working

## 3. Token Creation Works

Test case: Create scan token

Step:

1. open `My Account`
2. open `Security`
3. create a token

Expected result:

- token is generated

Why it matters:

- Maven scan needs the token

## 4. employee-api Scan Pass

Test case: Analyze `employee-api`

Step:

```powershell
cd C:\Users\Salma\OneDrive\Documents\Java_Laranings\Unit_test_learning\src\employee-api
mvn clean verify sonar:sonar "-Dsonar.projectKey=employee-api" "-Dsonar.projectName=employee-api" "-Dsonar.host.url=http://localhost:9000" "-Dsonar.token=YOUR_TOKEN"
```

Expected result:

- build succeeds
- SonarQube shows project `employee-api`

Why it matters:

- proves end-to-end code analysis works

## 5. notification-service Scan Pass

Test case: Analyze `notification-service`

Step:

```powershell
cd C:\Users\Salma\OneDrive\Documents\Java_Laranings\Unit_test_learning\src\notification-service
mvn clean verify sonar:sonar "-Dsonar.projectKey=notification-service" "-Dsonar.projectName=notification-service" "-Dsonar.host.url=http://localhost:9000" "-Dsonar.token=YOUR_TOKEN"
```

Expected result:

- project appears in SonarQube

Why it matters:

- confirms more than one service can be analyzed

## 6. api-gateway Scan Pass

Test case: Analyze `api-gateway`

Step:

```powershell
cd C:\Users\Salma\OneDrive\Documents\Java_Laranings\Unit_test_learning\src\api-gateway
mvn clean verify sonar:sonar "-Dsonar.projectKey=api-gateway" "-Dsonar.projectName=api-gateway" "-Dsonar.host.url=http://localhost:9000" "-Dsonar.token=YOUR_TOKEN"
```

Expected result:

- project appears in SonarQube

Why it matters:

- checks gateway code quality too

## 7. service-registry Scan Pass

Test case: Analyze `service-registry`

Step:

```powershell
cd C:\Users\Salma\OneDrive\Documents\Java_Laranings\Unit_test_learning\src\service-registry
mvn clean verify sonar:sonar "-Dsonar.projectKey=service-registry" "-Dsonar.projectName=service-registry" "-Dsonar.host.url=http://localhost:9000" "-Dsonar.token=YOUR_TOKEN"
```

Expected result:

- project appears in SonarQube

Why it matters:

- covers your service registry too

## 8. SonarQube Issue Review

Test case: View issues in UI

Step:

1. open a scanned project
2. open `Issues`

Expected result:

- issue list is visible
- file paths and code problem summaries are shown

Why it matters:

- this is the main value of SonarQube

## 9. Wrong Token Failure

Test case: Invalid token

Step:

```powershell
cd C:\Users\Salma\OneDrive\Documents\Java_Laranings\Unit_test_learning\src\employee-api
mvn clean verify sonar:sonar "-Dsonar.projectKey=employee-api" "-Dsonar.projectName=employee-api" "-Dsonar.host.url=http://localhost:9000" "-Dsonar.token=WRONG_TOKEN"
```

Expected result:

- scan fails with authorization error

Why it matters:

- helps you recognize credential problems quickly

## 10. SonarQube Down Failure

Test case: SonarQube unavailable

Step:

```powershell
cd C:\Docker_compose\kafka-setup
docker compose stop sonarqube
```

Then run a scan again.

Expected result:

- scan fails because server is unreachable

Why it matters:

- helps you identify platform outage versus code problem

Recovery:

```powershell
cd C:\Docker_compose\kafka-setup
docker compose start sonarqube
```

## 11. PostgreSQL Down Failure

Test case: SonarQube database unavailable

Step:

```powershell
cd C:\Docker_compose\kafka-setup
docker compose stop sonarqube-db
```

Expected result:

- SonarQube becomes unhealthy or unavailable

Why it matters:

- shows the dependency between SonarQube and its database

Recovery:

```powershell
cd C:\Docker_compose\kafka-setup
docker compose start sonarqube-db sonarqube
```

## 12. Repeat Scan After Code Change

Test case: Re-analysis after edits

Step:

1. make a small code change
2. rerun the Maven scan

Expected result:

- analysis timestamp updates
- issue list may change

Why it matters:

- teaches how SonarQube is used during development
