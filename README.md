# Subscription Workflow Service

![Java 17](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java)
![Spring Boot 3.2](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen?style=flat-square&logo=spring)
![Flowable](https://img.shields.io/badge/Flowable-6.8.0-blue?style=flat-square)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-Event--Driven-black?style=flat-square&logo=apachekafka)
![MongoDB](https://img.shields.io/badge/MongoDB-7.0-green?style=flat-square&logo=mongodb)
![Gradle](https://img.shields.io/badge/Gradle-8-blue?style=flat-square&logo=gradle)
![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)

BPMN-driven subscription fulfillment service — orchestrates end-to-end subscription lifecycle using Flowable workflows, Kafka event streaming, and MongoDB persistence.

---

## Architecture

```
REST Client
    │  POST /subscription/start/{processKey}
    ▼
SubscriptionController
    │
    ▼
SubscriptionService ──► SubscriptionWorkflowWrapper
                               │
                               ▼
                      Flowable RuntimeService
                      (starts process instance)
                               │
                   ┌───────────▼────────────┐
                   │     BPMN Process Flow   │
                   │  [Start]               │
                   │     ↓                  │
                   │  [Validate]            │ ◄── ValidateSubscriptionDelegate
                   │     ↓                  │       └─► publishes SW_VALIDATION_STATUS
                   │  [UserTask: Wait]       │ ◄── awaits SW_PROVISIONING_RESULT (Kafka)
                   │     ↓                  │
                   │  [UpdateStatus]        │ ◄── UpdateSubscriptionStatusDelegate
                   │     ↓                  │       └─► publishes SW_SUBSCRIPTION_STATUS
                   │  [Notify]              │ ◄── SendActivationNotificationDelegate
                   │     ↓                  │
                   │  [End]                 │
                   └────────────────────────┘
                               │
              ┌────────────────┴────────────────┐
              │ Kafka Listeners                 │
              │  SW_SUBSCRIPTION_STATUS →       │
              │    SubscriptionStatusEventListener│
              │  SW_PROVISIONING_RESULT  →      │
              │    ProvisioningResultEventListener│
              └────────────────────────────────┘
                               │
                           MongoDB
              (process_instance_details collection)
```

---

## Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Language | Java | 17 |
| Framework | Spring Boot | 3.2.5 |
| Workflow Engine | Flowable | 6.8.0 |
| Messaging | Apache Kafka | — |
| Database | MongoDB | — |
| Build | Gradle | 8 |
| Auth | Spring Security (Basic Auth) | — |
| API Docs | SpringDoc OpenAPI | — |

---

## Prerequisites

| Software | Version | Notes |
|----------|---------|-------|
| Java | 17+ | `java -version` |
| Gradle | 8+ | or use `./gradlew` wrapper |
| Docker | 20.10+ | for Docker Compose option |
| Docker Compose | 2.x+ | `docker compose version` |
| MongoDB | 6+ | only needed for local option |
| Apache Kafka | 3.x+ | only needed for local option |

---

## Quick Start

### Option A: Docker Compose (Recommended)

Starts MongoDB, Zookeeper, Kafka, and the app in one command.

```bash
git clone https://github.com/mohitshaky/subscription-workflow-service.git
cd subscription-workflow-service
docker-compose up --build
```

App starts on **http://localhost:8080** after all containers are healthy.

> Kafka is available at `localhost:29092` from your host machine.  
> MongoDB is available at `localhost:27017`.

---

### Option B: Local (Manual)

**1. Start MongoDB and Kafka manually** (or point to existing instances).

**2. Export environment variables:**

```bash
export MONGO_URI=mongodb://localhost:27017/subscriptiondb
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export SERVER_PORT=8080
export APP_USER=admin
export APP_PASSWORD=admin123
export SUBSCRIPTION_CONCURRENCY=3
export PROVISIONING_CONCURRENCY=3
```

**3. Build and run:**

```bash
./gradlew bootRun
```

Or build a JAR and run it:

```bash
./gradlew build
java -jar build/libs/subscription-workflow-service-*.jar
```

---

## Environment Variables

All variables have defaults in `application.yml`. Override as needed.

| Variable | Default | Description |
|----------|---------|-------------|
| `MONGO_URI` | `mongodb://localhost:27017/subscriptiondb` | MongoDB connection URI |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka bootstrap servers |
| `SERVER_PORT` | `8080` | Application port |
| `APP_USER` | `admin` | Basic auth username |
| `APP_PASSWORD` | `admin123` | Basic auth password |
| `SUBSCRIPTION_CONCURRENCY` | `3` | Kafka listener thread count (subscription topic) |
| `PROVISIONING_CONCURRENCY` | `3` | Kafka listener thread count (provisioning topic) |

---

## API Endpoints

All endpoints require **Basic Auth** (`admin` / `admin123` by default) and the following headers:

| Header | Description | Example |
|--------|-------------|---------|
| `transactionId` | Unique transaction identifier | `txn-001` |
| `correlationId` | Correlation ID for distributed tracing | `corr-001` |
| `sourceChannel` | Originating channel | `WEB`, `MOBILE`, `API` |
| `tenantId` | Tenant identifier | `tenant-1` |

### Subscription Workflow

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/subscription/start/{processKey}` | Start a BPMN subscription workflow |
| `PATCH` | `/subscription/task/{taskId}/complete` | Complete a Flowable user task |
| `PATCH` | `/subscription/{processInstanceId}/{signalName}/signal` | Send a signal to a running process |

#### Start a Subscription Workflow

```bash
curl -X POST http://localhost:8080/subscription/start/subscriptionFulfillmentProcess \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -H "transactionId: txn-001" \
  -H "correlationId: corr-001" \
  -H "sourceChannel: WEB" \
  -H "tenantId: tenant-1" \
  -d '{
    "subscriptionId": "SUB-12345",
    "customerId": "CUST-001",
    "planId": "PLAN-PREMIUM",
    "channel": "WEB"
  }'
```

#### Complete a User Task

```bash
curl -X PATCH http://localhost:8080/subscription/task/{taskId}/complete \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -H "transactionId: txn-002" \
  -H "correlationId: corr-001" \
  -H "sourceChannel: WEB" \
  -H "tenantId: tenant-1" \
  -d '{
    "provisioningResult": "SUCCESS"
  }'
```

---

## Kafka Topics

| Topic | Direction | Description |
|-------|-----------|-------------|
| `SW_SUBSCRIPTION_STATUS` | Outbound / Inbound | Subscription status change events |
| `SW_PROVISIONING_RESULT` | Inbound | Provisioning result received from downstream |
| `SW_VALIDATION_STATUS` | Outbound | Validation outcome events |
| `SW_NOTIFICATION` | Outbound | Activation notification events |

---

## MongoDB

**Collection**: `process_instance_details`

Stores the mapping between business identifiers and Flowable process instances.

| Field | Description |
|-------|-------------|
| `subscriptionId` | Business subscription ID |
| `processInstanceId` | Flowable process instance ID |
| `status` | Current process status |
| `createdAt` | Process start timestamp |
| `updatedAt` | Last updated timestamp |

---

## Swagger / API Docs

| Resource | URL |
|----------|-----|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/api-docs |

---

## Health & Monitoring

| Endpoint | Description |
|----------|-------------|
| `GET /actuator/health` | Application health status |
| `GET /actuator/info` | Application info |
| `GET /actuator/metrics` | Application metrics |

```bash
curl -u admin:admin123 http://localhost:8080/actuator/health
```

---

## Running Tests

```bash
./gradlew test
```

Test reports are generated at `build/reports/tests/test/index.html`.

---

## Project Structure

```
src/main/java/com/mohit/subscription/workflow/
├── controller/      SubscriptionController.java
├── delegate/        ValidateSubscriptionDelegate.java
│                    UpdateSubscriptionStatusDelegate.java
│                    SendActivationNotificationDelegate.java
├── handler/         SubscriptionEventHandler.java
├── listener/        SubscriptionStatusEventListener.java
│                    ProvisioningResultEventListener.java
├── model/           ProcessInstanceDetail.java
├── repository/      ProcessInstanceDetailRepository.java
├── service/         SubscriptionService.java
└── wrapper/         SubscriptionWorkflowWrapper.java

src/main/resources/
└── processes/
    └── subscription-fulfillment-process.bpmn20.xml
```

**BPMN Process Key**: `subscriptionFulfillmentProcess`

---

## Author

Built by **Mohit** — Senior Java Backend Developer | [Portfolio](https://mohitshaky.github.io) | [GitHub](https://github.com/mohitshaky)
