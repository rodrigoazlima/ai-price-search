# POC AI Price Notifier

A proof-of-concept Spring Boot service that demonstrates price tracking foundations and simple AI integration via LM Studio. It exposes REST endpoints for products, alerts, and price history, and includes scaffolding for database access, scheduling (Quartz), and an HTTP client (OpenFeign) to call a local LM Studio API for text completions/embeddings.

Note: This is a POC. Some features are stubs or partially implemented and may change.

## Stack
- Language: Java 17
- Framework: Spring Boot 3.5.6
  - Spring Web (REST)
  - Spring Data JPA
  - Spring Boot Actuator
  - Quartz Scheduler
  - Spring Validation
  - Spring Boot DevTools (optional, runtime)
- HTTP Client: Spring Cloud OpenFeign 4.3.0 (+ feign-httpclient 12.1)
- Database: MySQL (via mysql-connector-j)
- Build/Package Manager: Maven
- Codegen/Annotations: Lombok
- Testing: Spring Boot Test, JUnit (via Spring Boot), Mockito, H2 (available as test dependency)
- API documentation annotations: swagger-annotations (io.swagger.core.v3)

## Project Structure
```
java-poc-ai-PriceNotifier
├─ pom.xml
├─ HELP.md
├─ doc/
│  ├─ Hello.api.md
│  ├─ Product.api.md
│  ├─ ProductHistory.api.md
│  ├─ Alert.api.md
│  ├─ Actuator.api.md
│  └─ extern/LMStudio.MD
├─ src
│  ├─ main
│  │  ├─ java/dev/rodrigoazlima/poc/ai/pricenotifier
│  │  │  ├─ AiPriceNotifierApplication.java          # Entry point
│  │  │  ├─ controller/api/
│  │  │  │  ├─ HelloWorldController.java             # GET /hello
│  │  │  │  ├─ ProductController.java                # /api/v1/products (CRUD stub)
│  │  │  │  ├─ PriceHistoryController.java           # /api/v1/products/{id}/history
│  │  │  │  └─ AlertController.java                  # /api/v1/alerts
│  │  │  ├─ config/DatabaseConfig.java               # DataSource bean from properties
│  │  │  ├─ config/DatabaseProperties.java           # Holds DB props (bound from app.yml)
│  │  │  ├─ feign/LmStudioClient.java                # Feign client to LM Studio
│  │  │  ├─ service/* & service/impl/*               # Service layer (LM Studio integration)
│  │  │  ├─ repository/PriceHistoryRepository.java   # Spring Data JPA repository
│  │  │  ├─ model/PriceHistory.java                  # JPA entity
│  │  │  └─ dto/**                                   # DTOs for LM Studio API
│  │  └─ resources/application.yml                   # App, DB, Actuator & LM Studio settings
│  └─ test
│     ├─ java/**                                     # Unit/WebMvc tests
│     └─ resources/application[-test].yml            # Test configs
└─ target/                                           # Build outputs (generated)
```

## Requirements
- Java 17 (JDK)
- Maven 3.9+
- MySQL server reachable at the configured host (defaults assume localhost)
- Optional: LM Studio running locally if you want to exercise AI integration
  - Default URL: http://localhost:1234

## Configuration
Configuration is primarily managed via src/main/resources/application.yml.

Defaults (application.yml):
- spring.application.name: "POC AI Price Notifier"
- Database (local dev example):
  - spring.datasource.url: jdbc:mysql://localhost:3306/prices
  - spring.datasource.username: root
  - spring.datasource.password: 123007
  - spring.datasource.driver-class-name: com.mysql.cj.jdbc.Driver
  - spring.jpa.properties.hibernate.dialect: org.hibernate.dialect.MySQL8Dialect
- Actuator (exposed over web): health, info
- LM Studio:
  - lmstudio.api.url: http://localhost:1234
  - lmstudio.api.api-key: ${LMSTUDIO_API_KEY:} (env var, optional)

Environment variables you can set:
- LMSTUDIO_API_KEY: API key for LM Studio, if required by your local setup.
- Standard Spring overrides (examples):
  - SPRING_DATASOURCE_URL
  - SPRING_DATASOURCE_USERNAME
  - SPRING_DATASOURCE_PASSWORD
  - SPRING_DATASOURCE_DRIVER_CLASS_NAME
  - SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT
  - SERVER_PORT

Note: For production-like setups change the passwords and use environment variables or externalized config.

## How to Run
From the project root directory:

- Run the application (dev):
  - mvn spring-boot:run

- Build an executable jar:
  - mvn clean package
  - java -jar target/POC-AI-Price-Notifier-0.0.1-SNAPSHOT.jar

- Run tests:
  - mvn test

Windows PowerShell users can run the same commands from this directory.

## Entry Point
- Main class: dev.rodrigoazlima.poc.ai.pricenotifier.AiPriceNotifierApplication

## Exposed Endpoints (current)
- GET /hello → returns "Hello, World!"
- Products (stubbed storage):
  - POST /api/v1/products → { product_id }
  - GET /api/v1/products?limit=&offset=
  - PUT /api/v1/products/{product_id}
  - DELETE /api/v1/products/{product_id}
- Alerts:
  - POST /api/v1/alerts → { alert_id } (validates product_id, desired_price > 0, min_interval_hours >= 0)
  - GET  /api/v1/alerts?product_id=&limit=&offset=
- Price History:
  - GET /api/v1/products/{product_id}/history?from=&to=&limit=&sort=asc|desc
- Actuator:
  - GET /actuator/health
  - GET /actuator/info

See the doc/ folder for additional API notes: Hello.api.md, Product.api.md, ProductHistory.api.md, Alert.api.md, Actuator.api.md.

## LM Studio Integration
- A Spring Cloud OpenFeign client (LmStudioClient) targets LM Studio at lmstudio.api.url.
- DTOs under dto/lmstudio support chat/completion/embedding messages.
- To test locally, ensure LM Studio is running and listening on the configured port (default 1234). Set LMSTUDIO_API_KEY if your LM Studio instance requires it.
- Refer to doc/extern/LMStudio.MD for local LM Studio notes.

## Database
- JPA entity: model/PriceHistory
- Repository: repository/PriceHistoryRepository
- DataSource is configured by DatabaseConfig using properties from application.yml (and/or env overrides).
- Default config points to a local MySQL instance. Create the database/schema before running or adjust the URL.

Example to create a dev database:
- CREATE DATABASE prices CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

Schema migration tooling (e.g., Flyway/Liquibase) is not currently included. TODO: Add migrations.

## Maven Commands (Scripts)
- mvn clean            → Clean build artifacts
- mvn test             → Run tests
- mvn package          → Build jar
- mvn spring-boot:run  → Run the app directly

The Spring Boot Maven Plugin is configured in pom.xml.

## Testing
- Frameworks: Spring Boot Test, Mockito
- Many tests are WebMvc tests for controllers (no DB required), for example:
  - controller/api/HelloWorldControllerTest
  - controller/api/ProductControllerTest
  - controller/api/PriceHistoryControllerTest
  - controller/api/AlertControllerTest
  - config/ActuatorHealthEndpointTest
- H2 is available as a test-scoped dependency (pom.xml) but is not explicitly configured in application-test.yml at this time. TODO: Wire H2 for repository-level tests or use a separate test profile.

Run all tests:
- mvn test

Add debug prints in tests by using System.out.println("[DEBUG_LOG] your message").

## Development Notes
- HELP.md contains additional Spring Boot/Maven references and notes on the parent POM license/developers overrides.
- Packages use dev.rodrigoazlima.poc.ai.pricenotifier (see HELP.md for note on original invalid package name).
- Quartz is included as a dependency but not yet wired with jobs/triggers in the current code. TODO: Add a sample scheduled job.
- Swagger annotations are present but no auto-configured OpenAPI UI is set up. TODO: Add springdoc-openapi-ui if desired.
- PriceController exists but currently has no mapped endpoints. TODO: Implement pricing detection endpoints or remove the stub.

## License
TODO: Add a LICENSE file and specify the license here (e.g., MIT, Apache-2.0). The current pom.xml contains empty license overrides.

## Troubleshooting
- Classpath/Lombok issues in IDE: Ensure Lombok plugin is installed and annotation processing is enabled in your IDE.
- Port conflicts: Change server.port in application.yml or via environment variable SERVER_PORT.
- Database connection failures: Verify MySQL is running, credentials are correct, and the schema exists (default password currently 123007; change for your env).
- LM Studio connection refused: Ensure LM Studio is running and reachable at lmstudio.api.url.
