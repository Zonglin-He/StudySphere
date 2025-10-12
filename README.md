

# StudySphere

## Overview

StudySphere is a full-stack learning/forum application. **Backend:** Spring Boot + MyBatis-Plus + Maven + Redis + JWT + RabbitMQ + MinIO + MySQL. **Frontend:** Vue 3 + Element Plus. It ships with auth (register/login/reset), verification codes, request tracing, rate limiting, and OpenAPI docs.

## ✨ Features

### Backend

* **Auth basics**: user registration, login, password reset (REST APIs)
* **MyBatis-Plus** for simpler persistence and CRUD
* **Redis** stores verification codes (register/reset) with TTL
* **RabbitMQ** queues email/message tasks, processed by a listener
* **Spring Security + JWT**: manual integration for authentication/authorization
* **IP rate limiting** via Redis to prevent abuse
* **DTO ↔ DO separation** with reflection-based mapping utilities
* **Unified JSON error model** for all errors/exceptions
* **Manual CORS** via a filter
* **Snowflake trace ID** auto-generated per request (filter)
* **Environment profiles**: separate dev/prod configuration
* **Structured logging**: full request info + trace ID, supports file logs
* **Swagger/OpenAPI** auto-generated docs, including auth endpoints
* **MinIO** for object storage (e.g., image uploads)

### Frontend

* **Auth UIs**: register, login, password reset + a simple home page
* **Vue Router** for routing
* **Axios** for HTTP requests
* **Element Plus** as the UI component library
* **VueUse** for dark-mode toggle
* **unplugin-auto-import** to reduce bundle size with on-demand imports

## 🧱 Tech Stack

**Backend:** Spring Boot 3, Spring Security, JWT, MyBatis-Plus, Redis, RabbitMQ, MinIO, Springdoc OpenAPI
**Frontend:** Vue 3, Vite, Vue Router, Axios, Element Plus, VueUse, unplugin-auto-import

## 🗂 Project Structure

```
study-sphere/
├─ backend/                # Spring Boot app
│  ├─ src/main/java/...
│  ├─ src/main/resources/
│  │  ├─ application.yml
│  │  ├─ application-dev.yml
│  │  └─ application-prod.yml
│  └─ pom.xml
└─ frontend/               # Vue 3 app
   ├─ src/
   ├─ index.html
   ├─ vite.config.ts
   └─ package.json
```

## ⚙️ Prerequisites

* **Java 17+**, Maven 3.8+
* **Node.js 18+**, pnpm/npm/yarn
* **Services**: Redis, RabbitMQ, MinIO

## 🐳 Docker All-in-One Deployment

The repo already contains a production-ready `docker-compose.yml`, multi-stage Dockerfiles for both apps, and a custom Nginx config. Follow these steps to launch everything:

1. **Install Docker**: use [Docker Desktop](https://www.docker.com/products/docker-desktop/) (includes Compose v2) or Docker Engine + Compose.
2. **Clone the project**:
   ```bash
   git clone https://github.com/xxx/StudySphere.git
   cd StudySphere
   ```
3. **(Optional) tweak config**:
   * The backend reads connection details from `SPRING_` / `APP_` environment variables defined in `docker-compose.yml`. Adjust credentials, ports, or bucket names there if needed.
   * The frontend build consumes `my-project-frontend/.env.production` and proxies every API call to `/api`. Edit the file if you need a different base path before building.
   * Update the bind-mounted `./data/**` directories if you want persistent volumes in a custom location.
4. **Start everything** (the first run builds the frontend/backend images automatically):
   ```bash
   docker compose up -d --build
   ```
   Compose will spin up MySQL, Redis, RabbitMQ, MinIO, the Spring Boot backend, and the Nginx-hosted frontend.
5. **Verify containers**:
   ```bash
   docker compose ps
   docker compose logs -f backend   # backend logs
   docker compose logs -f frontend  # Nginx/frontend logs
   ```
6. **Entry points**:
   * Frontend → `http://localhost:5173`
   * Backend & Swagger → `http://localhost:18080/swagger-ui/index.html`
   * RabbitMQ UI → `http://localhost:15673` (admin/admin)
   * MinIO console → `http://localhost:19001` (minioadmin/minioadmin)
7. **Stop / rebuild**:
   ```bash
   docker compose down            # stop containers
   docker compose down -v         # stop and remove volumes (wipe data)
   docker compose up -d --build   # rebuild & restart
   ```

> Tip: import `test.sql` into MySQL after the first startup to create an admin account. You can append additional `SPRING_` or `APP_` variables under `backend.environment` if you need to override more Spring Boot settings.

## 🔐 Configuration

### Backend (`application.yml`)

```yaml
spring:
  profiles:
    active: dev
  data:
    redis:
      host: localhost
      port: 6379
    rabbitmq:
      host: localhost
      port: 5672
      username: admin
      password: admin
app:
  jwt:
    secret: "CHANGE_ME_32+_CHARS"
    expire-hours: 72

  cors:
    allowed-origins: "http://localhost:5173"

  trace:
    header: "X-Trace-Id"

  minio:
    endpoint: "http://localhost:9000"
    bucket: "studysphere"
    access-key: "minioadmin"
    secret-key: "minioadmin"

  rate-limit:
    window-seconds: 60
    max-requests: 100

springdoc:
  api-docs:
    enabled: true
  swagger-ui:
    enabled: true
```

### Frontend (`.env.local`)

```dotenv
VITE_API_BASE_URL=http://localhost:8080
VITE_UPLOAD_BUCKET=studysphere
```

## 🚀 Run

### Backend

```bash
cd backend
mvn clean package -DskipTests
java -jar target/*.jar
```

* Swagger UI → `http://localhost:8080/swagger-ui/index.html`
* OpenAPI JSON → `http://localhost:8080/v3/api-docs`

### Frontend

```bash
cd frontend
pnpm i    # or npm i / yarn
pnpm dev  # or npm run dev / yarn dev
# App → http://localhost:5173
```

## 📚 Common API Endpoints

* `POST /api/auth/register` — send code + register
* `POST /api/auth/login` — issue JWT
* `POST /api/auth/reset/request` — request reset code
* `POST /api/auth/reset/confirm` — confirm reset
* `GET /api/user/profile` — requires `Authorization: Bearer <token>`
* `POST /api/topic` — create topic (JWT)

**Headers:**

* `Authorization: Bearer <JWT>`
* `X-Trace-Id` (auto-generated by filter if missing)

## 🧩 Implementation Notes

* Verification codes in Redis as `reg:{email}` / `reset:{email}` with TTL
* RabbitMQ decouples email/message sending; a listener consumes and sends
* Spring Security guards routes; JWT filter validates and injects user
* Redis sliding-window IP rate limit
* Reflection-based DTO/DO mapper utilities
* Unified JSON error schema: `{ code, message, traceId, path, timestamp }`
* Snowflake ID added to MDC logs and response header

## 🛡 Security & Production Tips

* Use a strong JWT secret and rotate periodically
* Configure per-environment CORS allow-lists
* Persist MinIO data to durable storage
* Add HTTPS and a reverse proxy (e.g., Nginx)
* Tune rate limits for your traffic
* Centralize logs (ELK/OpenSearch) with trace IDs

## 📜 License

MIT


## Database: Ready-made schema & seed (`test.sql`)

We provide a MySQL dump you can import to get a working schema and some sample data right away: **[`test.sql`](sandbox:/mnt/data/test.sql)**.
It creates core tables (accounts, topics, images, interactions, etc.) and inserts minimal seed rows (e.g., topic types, a few demo users with **bcrypt-hashed** passwords).&#x20;

### What’s inside

* Tables: `db_account`, `db_account_details`, `db_account_privacy`, `db_image_store`, `db_notification`, `db_topic`, `db_topic_comment`, `db_topic_interact_collect`, `db_topic_interact_like`, `db_topic_type`.
* Seeds: a few users (passwords are **hashed**), predefined topic types and demo records for images/topics. (If you don’t know the plaintext passwords, just **register a new user** via the API or use the reset-password flow.)&#x20;

### How to import

**Option A — MySQL CLI (local MySQL running, DB name: `test`)**

```bash
# create database if needed
mysql -uroot -p -h 127.0.0.1 -P 3306 -e "CREATE DATABASE IF NOT EXISTS test DEFAULT CHARACTER SET utf8mb4"

# import
mysql -uroot -p -h 127.0.0.1 -P 3306 test < test.sql
```

**Option B — Docker Compose (if your MySQL runs in Docker)**

```bash
# copy test.sql into the container (example container name: mysql)
docker cp test.sql mysql:/test.sql

# import from inside container
docker exec -i mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" test < /test.sql'
```

> The dump assumes schema name **`test`**. If you use a different schema, either `USE your_db;` before importing or rename in your connection URL.

### Backend datasource config (example)

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/test?useSSL=false&serverTimezone=UTC&characterEncoding=utf8
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### Notes

* **Passwords are bcrypt hashes**; use the **register** API to create your own login or use the **reset** flow to set a new password.&#x20;
* Sample emails/usernames are **for development only**—do not reuse in production.
* Unique constraints exist on usernames/emails; clear or change seeds if they conflict in your environment.&#x20;

---
