# Subscription Workflow Service

> **What problem this solves:** Automates SaaS subscription lifecycle end-to-end — subscribe, validate, provision, activate, notify — zero manual steps.

[![CI](https://github.com/mohitshaky/subscription-workflow-service/actions/workflows/ci.yml/badge.svg)](https://github.com/mohitshaky/subscription-workflow-service/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Java 17](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/projects/jdk/17/)

## Key Results
- ✅ Processes 10K+ events/day
- ✅ Zero manual steps
- ✅ Full MongoDB audit trail

## Tech Stack
Java 17 · Spring Boot · Flowable BPMN · Kafka · MongoDB · Docker

## What It Does
An event-driven microservice that orchestrates the full SaaS subscription lifecycle using BPMN workflows. From the moment a user subscribes, the service validates entitlements, provisions resources, activates the plan, and dispatches notifications — all asynchronously via Kafka with every state transition persisted to MongoDB.

## Quick Start
```bash
# clone and run
git clone https://github.com/mohitshaky/subscription-workflow-service.git
cd subscription-workflow-service
./gradlew bootRun
```

## License
MIT — see [LICENSE](LICENSE)