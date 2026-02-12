# User Subscription Manager & Rate Limiter

A high-performance Dropwizard microservice for managing user subscriptions and enforcing API rate limits. It uses Google Guice for DI, Hibernate for persistence, Caffeine for in-memory caching, and a uniform ApiResponse envelope with graceful exception handling.

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
- Error Handling & ApiResponse
- Caching
- Rate Limiting & Usage Sync
- Development Notes
- Troubleshooting

---

## Overview
This service provides:
- Subscription lifecycle management: create, list, cancel, upgrade.
- Smart rate limiting using Caffeine cache with per-user usage counters.
- Write-behind persistence to reduce DB load (periodic flush to MySQL).
- Consistent JSON responses via `ApiResponse<T>` and custom exceptions mapped globally.

---

## Architecture
- Dropwizard: Application framework, Jersey resources, lifecycle management.
- Google Guice: Dependency injection (see `SubscriptionModule`).
- Hibernate: ORM with `Subscription`, `User`, and `Plan` entities.
- Caffeine Cache:
  - In-memory counters for per-user API usage.
  - In-memory cache for user existence (to avoid DB hits on common validation paths).
- Write-Behind Task: `UsageSyncTask` periodically flushes cached usage to DB.
- Uniform Response Envelope: `com.traf.core.ApiResponse<T>` for all API responses.
- Global Exception Mapping: `GenericExceptionMapper` converts custom exceptions into `ApiResponse.fail` with appropriate HTTP status codes.

Key packages:
- `com.traf.resources`: HTTP endpoints (Jersey).
- `com.traf.service`: Business logic and background tasks; includes `UserService` using cache for user existence.
- `com.traf.db`: DAOs for database access.
- `com.traf.repository`: Cache-backed usage repository.
- `com.traf.core`: Entities and `ApiResponse`.
- `com.traf.exceptions`: Custom exceptions (`BadRequestException`, `NotFoundException`, `ConflictException`, `RateLimitExceededException`).

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
java -jar target/user-subscription-manager-1.1-SNAPSHOT.jar db migrate src/main/resources/config.yml
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
java -jar target/user-subscription-manager-1.1-SNAPSHOT.jar server src/main/resources/config.yml
```

- App port (default): `8080`
- Admin port (default): `8081`

---

## API Reference
All endpoints return/accept JSON using the `ApiResponse` envelope.

Base path: `/` (resources under `/subscriptions`, `/plans`, `/users`, `/api`)

### ApiResponse format
- Success:
  ```json
  { "success": true, "data": { /* payload */ } }
  ```
- Error:
  ```json
  { "success": false, "error": { "code": 404, "message": "User not found" } }
  ```

### Subscriptions
- Create subscription
  - `POST /subscriptions`
  - Body:
    ```json
    { "userId": 1, "planId": 2 }
    ```
  - Response: `ApiResponse<Subscription>`

- List subscriptions for user
  - `GET /subscriptions/user/{userId}`
  - Response: `ApiResponse<Subscription[]>`

- Cancel subscription
  - `PUT /subscriptions/{id}/cancel`
  - Response: `ApiResponse<Subscription>`

- Upgrade plan
  - `PUT /subscriptions/{id}/upgrade?planId={newPlanId}`
  - Response: `ApiResponse<Subscription>`

### Plans
- List plans
  - `GET /plans`
  - Response: `ApiResponse<Plan[]>`
- Create plan
  - `POST /plans`
  - Body: `Plan`
  - Response: `ApiResponse<Plan>`
- Delete plan
  - `DELETE /plans?id={id}`
  - Response: `ApiResponse<Long>`

### Users
- List users
  - `GET /users`
  - Response: `ApiResponse<User[]>`
- Get user by id
  - `GET /users/{id}`
  - Response: `ApiResponse<User>`
- Create user
  - `POST /users`
  - Body: `User`
  - Response: `ApiResponse<User>`
- Delete user
  - `DELETE /users/{id}`
  - Response: `ApiResponse<Long>`

### Rate Limiter
- Ping (rate-limited)
  - `GET /api/ping?userId={id}`
  - Response: `ApiResponse<String>`
  - Errors:
    - 400 if `userId` invalid
    - 429 if rate limit exceeded

---

## Error Handling & ApiResponse
- All exceptions are handled globally by `GenericExceptionMapper`.
- Resources throw custom exceptions for invalid input or missing entities:
  - `BadRequestException` → 400
  - `NotFoundException` → 404
  - `ConflictException` → 409
  - `RateLimitExceededException` → 429
- The mapper converts exceptions into `ApiResponse.fail(code, message)` ensuring consistent JSON error responses.

---

## Caching
- Usage counters: `Cache<Long, Integer>` in `SubscriptionModule`, used by `UserUsageRepository`.
- User existence cache: `Cache<Long, Boolean>` to avoid DB hits on common validation paths in `UserService.exists(userId)`.
  - Checks cache first; on miss, queries `UserDAO` and populates the cache.
  - TTL and size are tuned in `SubscriptionModule`.

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
- DI wiring in `SubscriptionModule` provides DAOs, `SessionFactory`, and caches.
- Use `@UnitOfWork` on resource methods to manage Hibernate sessions.
- Entities: ensure `Subscription` maps fields such as `status`, `plan`, `user`, and `currentUsage`.
- `UserService` provides cached user existence checks and user retrieval.

---

## Troubleshooting
- DB connection errors: verify `config.yml` credentials and that MySQL is running.
- Migration failures: confirm the DB exists and you’re using the correct config path.
- 404s on endpoints: confirm resources are registered and the server is running.
- Usage not persisting: make sure `UsageSyncTask` is managed by environment lifecycle.
- Ambiguous resource paths: ensure `@GET` methods have distinct `@Path` annotations (e.g., `/users` vs `/users/{id}`).

---

