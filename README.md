# TX Purchase

This service manages purchase records and provides currency conversion using official data from
the [U.S. Treasury Reporting Rates of Exchange](https://fiscaldata.treasury.gov/datasets/treasury-reporting-rates-exchange/treasury-reporting-rates-of-exchange).
It ensures financial precision by rounding values and strictly following the exchange rates active on the transaction
date.

---

## 🛠️ Dependencies

To run or develop this project, ensure you have the following installed:

* OpenJDK 21+
* Docker: For containerized execution.
* Docker Compose: To orchestrate the application and database services.
* PostgreSQL 18+: The relational database used for persistence.

---

## 🚀 Getting Started

### 🐳 Run with Docker (Recommended)

Docker Compose manages both the application and the PostgreSQL database.
No local installation or configuration is required; you only need Docker and Docker Compose installed.

```bash
docker compose up --build
```

### Run with Gradle

Ensure you have JDK 21 and a PostgreSQL instance running.

Command example:

```bash
DB_URL=jdbc:postgresql://localhost:5432/tx_purchase DB_USERNAME=postgres DB_PASSWORD=password ./gradlew bootRun
```

---

## APIs

Access the interactive Swagger UI to test the endpoints:
http://localhost:8080/api-doc

## Testing & Quality

Strict quality gates are enforced via Git Hooks (pre-commit), SonarLint, and JaCoCo (90% Class / 75% Line coverage).

### Commands

* Run all tests:
  ```bash
  ./gradlew test
  ```
* Run a specific test class:
  ```bash
  ./gradlew test --tests SomeTestClass
  ```
* Run a specific test method:
  ```bash
  ./gradlew test --tests SomeTestClass.someSpecificMethod
  ```
* Run tests with debug logs:
  ```bash
  DEBUG=true ./gradlew test
  ```
* Full Quality Check (Lint + Coverage + Tests):
  ```bash
  ./gradlew check
  ```

---
