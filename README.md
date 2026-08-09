# export-record

CSV Export Microservice
is A standalone Spring Boot REST microservice that tracks export history for the Skill Progress Tracker application.
It does not generate files itself — the main application generates CSV/PDF exports and registers each attempt here
as a durable record, enabling history browsing, retrying failed exports, and safe ownership checks.

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Domain Model](#domain-model)
- [Error Handling & Validation](#error-handling--validation)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Testing](#testing)
- [API Endpoints](#api-endpoints)
- [Security](#security)
- [Consumed By](#consumed-by)
---

## Overview

The `export-record` microservice is a companion service to the main Skill Progress Tracker application. Its 
responsibility is to persist a record of every export attempt (CSV or PDF) initiated by a user — who requested it,
what type, whether it succeeded or failed, and when— and to let the user manage that history.
It does **not** generate, store, or serve the actual export file content; that logic lives entirely in the main application,
which calls this service via a Feign client after each export attempt. ExportRecord is user-managed: 
every record is created, listed, edited, retried, and deleted directly in response to explicit user actions in the main application's UI.

## Architecture

```
┌─────────────────┐         Feign Client          ┌──────────────────┐
│   Main App      │   ──────────────────────────▶ │   export-record  │
│ (skill-progress │   POST/GET/PUT/DELETE         │  microservice    │
│  tracker)       │   /api/v1/exportRecord/**     │                  │
│                 │◀───────────────────────────── │                  │
└─────────────────┘         JSON responses        └──────────────────┘
        │                                                    │
        ▼                                                    ▼
   skill_progress_tracker DB                          export_record DB
   (MySQL)                                            (MySQL, separate)
```

The main application generates the actual file content and decides the outcome (`SUCCEEDED`/`FAILED`); this
microservice only records that outcome and exposes history/retry endpoints.

## Features

- **Export History Tracking** — Every export attempt (CSV/PDF) is persisted as an `ExportRecord`, independent of
  whether the underlying generation succeeded.
- **Report Definition Management** — Users can persist and update their preferred export settings (format and
    whether to include hours) via a dedicated `ReportDefinition` resource. The definition is upserted per user
    (one definition per `userId`), so saving new preferences automatically replaces any previous ones.
- **Metadata Editing** — File name and description can be updated after the fact.
- **Retry Support** — A failed export's status can be updated once the main application successfully regenerates the file.
- **Duplicate Submission Guard** — Rejects a new export request with 409 Conflict if an identical (same user, same type)
  request was already submitted within the last 5 seconds, protecting against accidental double-clicks.
- **Soft Delete** — Records are never physically removed; a `deleted` flag hides them from all read operations while
  preserving the audit trail.
- **Scheduled Cleanup** — A background job (`ExportRecordCleanupJob`) runs periodically (every 30 days) and permanently
   purges soft-deleted records whose `updatedOn` timestamp is older than 30 days, keeping the database clean.
- **Caching** — Single-record lookups (`getById`) are cached in an in-memory `ConcurrentMapCache` (`exportRecordById`).
  The cache is automatically evicted on update, retry, and delete operations.
- **Ownership-Safe Access** — Every read/write operation validates that the requesting user owns the record. A
  record that doesn't exist and a record that belongs to someone else return an identical `404 Not Found`, so no
  information about record existence leaks to non-owners.
- **API Key Authentication** — Service-to-service authentication via a custom `X-API-Key` header filter; no user
  session or login flow, since this service is only ever called by the main application, not directly by browsers.

---

## Tech Stack

| Layer       | Technology                                |
|-------------|-------------------------------------------|
| Language    | Java 21                                   |
| Framework   | Spring Boot 4.1.0                         |
| Web         | Spring Web (REST, JSON)                   |
| Persistence | Spring Data JPA / Hibernate               |
| Database    | MySQL 8+ (separate schema from main app)  |
| Security    | Spring Security (stateless, API key)      |
| Validation  | Jakarta Bean Validation                   |
| Caching     | Spring Cache (ConcurrentMapCacheManager)  |
| Scheduling  | Spring @Scheduled (fixedDelay-based)      |
| Testing     | JUnit 5, Mockito, Spring Boot Test, H2    |
| Boilerplate | Lombok                                    |
| Build       | Apache Maven (Maven Wrapper included)     |

---

## Domain Model

### ExportRecord

| Field        | Type            | Notes                         |
|--------------|-----------------|-------------------------------|
| id           | UUID            | Primary key                   |
| userId       | UUID            | Owner of the record           |
| fileName     | String          | Editable                      |
| description  | String          | Editable, up to 1000 chars    |
| exportType   | ExportType      | Enum: `CSV`, `PDF`            |
| exportStatus | ExportStatus    | Enum: `SUCCEEDED`, `FAILED`   |
| exportDate   | LocalDateTime   | Set once, at creation         |
| updatedOn    | LocalDateTime   | Refreshed on every write      |
| deleted      | boolean         | Soft-delete flag              |

### ReportDefinition

| Field        | Type       | Notes                                            |
|--------------|------------|--------------------------------------------------|
| id           | UUID       | Primary key                                      |
| userId       | UUID       | Unique per user (one definition per user)        |
| format       | ExportType | Enum: `CSV`, `PDF` — the user's preferred format |
| includeHours | boolean    | Whether to include hours in the exported report  |

---

## Error Handling & Validation

All request DTOs are validated with Jakarta Bean Validation (`@Valid`). 
Validation failures return a structured `400 Bad Request` with per-field error messages.

Business rules are enforced in the service layer through a custom exception hierarchy:

- `ApplicationException` (base)
  - `EntityNotFoundException` — record not found, soft-deleted, or not owned by the requesting user (→ `404 Not Found`)
  - `NullArgumentException` — a required argument (ID, userId, DTO, status) was `null` (→ `400 Bad Request`)
  - `DuplicateExportException` — duplicate export submission within the dedup window (→ `409 Conflict`)

A centralized `GlobalExceptionHandler` (`@RestControllerAdvice`) maps every exception type to a consistent JSON
`ErrorResponse` shape (`timestamp`, `status`, `error`, `message`, `path`, `fieldErrors`), covering:

| Exception                                 | HTTP Status                 |
|-------------------------------------------|-----------------------------|
| `EntityNotFoundException`                 | `404 Not Found`             |
| `DuplicateExportException`                | `409 Conflict`              |
| `ApplicationException` (catch-all base)   | `400 Bad Request`           |
| `MethodArgumentNotValidException`         | `400 Bad Request`           |
| `MissingServletRequestParameterException` | `400 Bad Request`           |
| `MethodArgumentTypeMismatchException`     | `400 Bad Request`           |
| `HttpMessageNotReadableException`         | `400 Bad Request`           |
| Any other `Exception`                     | `500 Internal Server Error` |

**Note:** authentication failures (missing/invalid API key) are handled earlier in the filter chain, before
`GlobalExceptionHandler` can intercept them, and therefore return a simpler JSON error shape.

---

## Project Structure

```text
src/main/java/app/
├── Application.java
├── config/
│   ├── SecurityConfig.java              # API key auth filter chain, stateless sessions
│   ├── ApiKeyAuthentication.java        # Custom Authentication implementation
│   └── ApiKeyAuthenticationFilter.java  # Validates X-API-Key header on every request
│   └── CacheConfig.java                # ConcurrentMapCacheManager for exportRecordById
├── exception/          
│   ├── ApplicationException.java        # Base exception
│   ├── EntityNotFoundException.java     # 404 — record not found / not owned
│   ├── NullArgumentException.java       # 400 — null required argument
│   ├── DuplicateExportException.java    # 409 — duplicate export within dedup window
│   ├── ErrorResponse.java              # Structured JSON error body
│   └── GlobalExceptionHandler.java     # @RestControllerAdvice mapping exceptions → HTTP responses
├── model/
│   ├── ExportRecord.java                # Domain entity
│   ├── ReportDefinition.java            # User's preferred export settings entity
│   ├── ExportType.java                  # Enum: CSV, PDF
│   └── ExportStatus.java                # Enum: SUCCEEDED, FAILED
├── repository/
│   └── ExportRecordRepository.java
│   └── ReportDefinitionRepository.java
├── scheduler/
│   └── ExportRecordCleanupJob.java      # Purges soft-deleted records older than 30 days
├── service/
│   └── ExportRecordService.java
│   └── ReportDefinitionService.java
└── web/
    ├── ExportRecordController.java
    ├── ReportDefinitionController.java
    ├── dto/
    │   ├── exportRecord/                # ExportCreateRequestDto, ExportUpdateRequestDto, ExportResponseDto
    │   └── reportDefinition/            # ReportDefinitionUpsertRequestDto, ReportDefinitionResponseDto
    └── mapper/
        ├── exportRecord/ExportRecordMapper.java
        └── reportDefinition/ReportDefinitionMapper.java
        
src/test/java/app/
├── ApplicationTests.java                # Smoke test — verifies context loads
├── util/
│   └── ExportTestFactory.java           # Builder methods for test data
├── web/
│   ├── SecurityTestConfig.java          # Test-only config with API key constant
│   ├── ExportRecordControllerTest.java  # API test — @WebMvcTest
│   └── mapper/
│       └── exportRecord/
│           └── ExportRecordMapperTest.java  # Unit test — pure JUnit
└── service/
    └── ExportRecordServiceItTest.java   # Integration test — @SpringBootTest + H2

src/test/resources/
└── application-test.properties          # H2 in-memory database config for tests

src/main/resources/
└── application.properties
```

---

## Getting Started

**Prerequisites**

- Java 21 or later
- MySQL 8+ running locally
- Maven (or use the included `mvnw` wrapper)
- The main Skill Progress Tracker application (this service is not usefully standalone)
- A `DB_USERNAME` environment variable set for the MySQL username
- A `DB_PASSWORD` environment variable set for the MySQL password
- An `API_KEY` environment variable set (must match the main application's Feign client configuration)
- The main Skill Progress Tracker application (this service is not usefully standalone)

**Steps**

1. Configure the database and API key — see [Configuration](#configuration) below.
2. Set environment variables
   ```bash
   export DB_USERNAME=root
   export DB_PASSWORD=123456
   export API_KEY=my-secret-api-key
   ```
   
3. Run the application
   ```bash
   ./mvnw spring-boot:run
   # or on Windows
   mvnw.cmd spring-boot:run
   ```

4. The service starts on
   ```
   http://localhost:8081
   ```

The database schema is created automatically by Hibernate on the first launch.

---

## Configuration

Edit `src/main/resources/application.properties`:

```properties
spring.application.name=export-record
server.port=8081

# Database connection (separate schema from the main application)
spring.datasource.url=jdbc:mysql://localhost:3306/export_record?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# Schema management (use 'validate' or 'none' in production)
spring.jpa.hibernate.ddl-auto=update

# Shared secret with the main application — must match its Feign client configuration
export-record.service.api-key=${API_KEY}
```

The `API_KEY` value must be identical to the one configured on the main application's Feign client, or every
request will be rejected with `401 Unauthorized`.

---

## Testing

Unit, Integration, and API tests using JUnit 5, Mockito, and Spring Boot Test with H2 in-memory database.
  ```bash
  ./mvnw test
  ```
Line coverage: 75% | Branch coverage: 75%

## API Endpoints

All endpoints require the `X-API-Key` header. `userId` is passed as a query parameter (or inside the request body
for create/upsert) on every request — this service has no session/login concept of its own; the caller is always
the main application, which already knows the authenticated user.

| Method | Endpoint                          | Status       | Description                                                                                                                              |
|--------|-----------------------------------|--------------|------------------------------------------------------------------------------------------------------------------------------------------|
| POST   | `/api/v1/exportRecord`            | 201 Created  | Register a new export attempt (SUCCEEDED or FAILED); rejects with 409 Conflict if a duplicate was submitted in the last 5 seconds        |
| GET    | `/api/v1/exportRecord/{id}`       | 200 OK       | Get a single export record by ID (owner only)                                                                                            |
| GET    | `/api/v1/exportRecord`            | 200 OK       | List all non-deleted export records for a user                                                                                           |
| GET    | `/api/v1/exportRecord/failed`     | 200 OK       | List only FAILED export records for a user                                                                                               |
| PUT    | `/api/v1/exportRecord/{id}`       | 200 OK       | Update editable fields (fileName, description, exportType)                                                                               |
| PUT    | `/api/v1/exportRecord/{id}/retry` | 202 Accepted | Update the status of a record after a retry attempt                                                                                      |
| DELETE | `/api/v1/exportRecord/{id}`       | 202 Accepted | Soft-delete an export record (owner only)                                                                                                |

A record that doesn't exist, is soft-deleted, or belongs to another user all returns an identical `404 Not Found`,
by design — this prevents leaking information about which record IDs exist to a non-owner.

## Report Definition Endpoints

| Method | Endpoint                   | Status | Description                                                                              |
|--------|----------------------------|--------|------------------------------------------------------------------------------------------|
| POST   | `/api/v1/reportDefinition` | 200 OK | Upsert (create or update) the user's report definition (userId, format, includeHours)    |
| GET    | `/api/v1/reportDefinition` | 200 OK | List all report definitions                                                              |

---

## Security

This service uses **stateless API key authentication**, not user login:

- `ApiKeyAuthenticationFilter` validates the `X-API-Key` header on every request against the configured secret.
- CSRF protection is disabled (no cookies/sessions are used, so CSRF does not apply).
- Session creation policy is `STATELESS`.
- There are no user roles or permissions within this service — trust is entirely delegated to the main application,
  which is the only expected caller. End-user authentication and authorization (USER/ADMIN roles) are handled
  exclusively by the main application before it ever calls this service.

## Consumed By

The main Skill Progress Tracker application integrates with this service through:

ExportClient — a @FeignClient interface describing the HTTP contract above
ExportService — a thin wrapper that supplies the API key, unwraps responses, and translates Feign exceptions (FeignException.NotFound → ExportNotFoundException, FeignException.Conflict → ExportInProgressException)
ExportFileService — generates the actual CSV/PDF content (Apache PDFBox for PDF) from the user's activity log, and orchestrates the create/retry flow around this service
Two MVC controllers (ExportController, ExportUpdateController) rendering the export details, history, and edit pages