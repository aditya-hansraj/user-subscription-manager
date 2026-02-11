# User Subscription Manager & Rate Limiter

A high-performance Dropwizard microservice for managing user subscriptions and enforcing API rate limits. It leverages Google Guice for DI, Hibernate for persistence, and Caffeine for ultra-fast in-memory counters with a write-behind strategy to MySQL.

---

## Contents
- Overview
- Architecture
- Prerequisites
- Setup
  - Install dependencies
  - Configuration (`config.yml`)
  - Database migrations
- Build & Run
- API Reference
- Rate Limiting & Usage Sync
- Development Notes
- Troubleshooting

---

## Overview
This service provides:
- Subscription lifecycle management: create, list, cancel, upgrade.
- Smart rate limiting using Caffeine cache with per-user usage counters.
- Write-behind persistence to reduce DB load (periodic flush to MySQL).

---

## Architecture
- Dropwizard: Application framework, Jersey resources, lifecycle management.
- Google Guice: Dependency injection (see `SubscriptionModule`).
- Hibernate: ORM with `Subscription`, `User`, and `Plan` entities.
- Caffeine Cache: In-memory counters for per-user usage.
- Write-Behind Task: `UsageSyncTask` periodically flushes cached usage to DB.

Key packages:
- `com.traf.resources`: HTTP endpoints (Jersey).
- `com.traf.service`: Business logic and background tasks.
- `com.traf.db`: DAOs for database access.
- `com.traf.repository`: Cache-backed usage repository.
- `com.traf.core`: Entities.

---

## Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8.0+
- A database named `user_subscription_manager`

---

## Setup

### 1) Install dependencies
Run Maven to download and compile all dependencies (Dropwizard, Guicey, Hibernate, Caffeine):

```bash
mvn clean install -DskipTests
```

### 2) Configuration
Create `src/main/resources/config.yml` and populate DB settings. Do not commit real credentials.

```yaml
server:
  applicationConnectors:
    - type: http
      port: 8080
  adminConnectors:
    - type: http
      port: 8081

database:
  driverClass: com.mysql.cj.jdbc.Driver
  user: your_db_user
  password: your_db_password
  url: jdbc:mysql://localhost:3306/user_subscription_manager?useSSL=false
  properties:
    charSet: UTF-8
    maxWaitForConnection: 1s
    validationQuery: "/* MySQL Health Check */ SELECT 1"
    minSize: 1
    maxSize: 10
```

### 3) Database migrations
Liquibase migrations create `users`, `plans`, and `subscriptions` tables including the `current_usage` column.

```bash
java -jar target/user-subscription-manager-1.0-SNAPSHOT.jar db migrate src/main/resources/config.yml
```

---

## Build & Run

### Build
Package the service into a fat JAR:

```bash
mvn package
```

### Run
Start the server:

```bash
java -jar target/user-subscription-manager-1.0-SNAPSHOT.jar server src/main/resources/config.yml
```

- App port (default): `8080`
- Admin port (default): `8081`

---

## API Reference
All endpoints return/accept JSON.

Base path: `/` (resources under `/subscriptions`, `/plans`, `/users`, etc.)

### Subscriptions
- Create subscription
  - `POST /subscriptions`
  - Body:
    ```json
    { "userId": 1, "planId": 2 }
    ```
  - Response: `Subscription`

- List subscriptions for user
  - `GET /subscriptions/user/{userId}`
  - Response: `Subscription[]`

- Cancel subscription
  - `PUT /subscriptions/{id}/cancel`
  - Response: updated `Subscription`

- Upgrade plan
  - `PUT /subscriptions/{id}/upgrade?planId={newPlanId}`
  - Response: updated `Subscription`

### Plans
- Typical endpoints (if exposed):
  - `GET /plans`
  - `GET /plans/{id}`

### Users
- Typical endpoints (if exposed):
  - `GET /users`
  - `GET /users/{id}`

### Health
- Dropwizard admin endpoints on `:8081` (health, metrics, threads).

---

## Rate Limiting & Usage Sync

### How it works
- `UserUsageRepository` keeps an in-memory counter per user via Caffeine.
- Reads are served from cache; on cache miss it loads current usage from DB.
- A background `UsageSyncTask` flushes cache values to DB every 60 seconds.

### Benefits
- Minimizes DB calls for high-traffic APIs.
- Atomic `Cache.get(key, loader)` ensures consistent cache loads.

### Notes
- Ensure `UsageSyncTask` is registered in the application lifecycle so the write-behind persists usage periodically.
- Plan limits are checked via `SubscriptionDAO#getPlanLimit(userId)` against active subscriptions.

---

## Development Notes
- DI wiring in `SubscriptionModule` provides DAOs, `SessionFactory`, and the `Cache<Long, Integer>` bean.
- Use `@UnitOfWork` on resource methods to manage Hibernate sessions.
- Entities: ensure `Subscription` maps fields such as `status`, `plan`, `user`, and `currentUsage`.

---

## Troubleshooting
- DB connection errors: verify `config.yml` credentials and that MySQL is running.
- Migration failures: confirm the DB exists and you’re using the correct config path.
- 404s on endpoints: confirm resources are registered and the server is running.
- Usage not persisting: make sure `UsageSyncTask` is managed by environment lifecycle.

---

