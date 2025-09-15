# StudySphere（中文）

## 概述

StudySphere 是一个用于课程交流的学习社区/论坛。**后端：** Spring Boot + MyBatis-Plus + Redis + JWT + RabbitMQ + MinIO；**前端：** Vue 3 + Element Plus。内置注册/登录/重置、验证码、请求追踪、限流与 OpenAPI 接口文档。

## ✨ 功能

### 后端

* **账号基础**：注册、登录、重置密码（REST 接口）
* **MyBatis-Plus** 简化持久层与 CRUD
* **Redis** 存储注册/重置验证码并设置过期时间
* **RabbitMQ** 积压邮件/消息任务，由监听器统一处理
* **Spring Security + JWT**：手动整合认证与鉴权
* **基于 IP 的限流**（Redis）防刷接口
* **DTO/DO 分离**，提供基于反射的映射工具
* **统一 JSON 错误/异常返回模型**
* **手动 CORS** 过滤器处理跨域
* **雪花 Trace ID**：过滤器为每次请求自动生成
* **多环境配置**：开发/生产分离
* **结构化日志**：记录完整请求信息 + Trace ID，支持文件日志
* **Swagger/OpenAPI** 自动文档（含登录等鉴权接口）
* **MinIO** 用于对象存储（如图片上传）

### 前端

* **认证界面**：注册、登录、重置 + 简易主页
* **Vue Router** 负责路由
* **Axios** 负责 HTTP 请求
* **Element Plus** 作为 UI 组件库
* **VueUse** 支持深色模式切换
* **unplugin-auto-import** 按需引入以减小体积

## 🧱 技术栈

**后端：** Spring Boot 3、Spring Security、JWT、MyBatis-Plus、Redis、RabbitMQ、MinIO、Springdoc OpenAPI
**前端：** Vue 3、Vite、Vue Router、Axios、Element Plus、VueUse、unplugin-auto-import

## 🗂 项目结构

```
study-sphere/
├─ backend/                # Spring Boot 应用
│  ├─ src/main/java/...
│  ├─ src/main/resources/
│  │  ├─ application.yml
│  │  ├─ application-dev.yml
│  │  └─ application-prod.yml
│  └─ pom.xml
└─ frontend/               # Vue 3 应用
   ├─ src/
   ├─ index.html
   ├─ vite.config.ts
   └─ package.json
```

## ⚙️ 环境要求

* **Java 17+**、Maven 3.8+
* **Node.js 18+**、pnpm/npm/yarn
* **依赖服务**：Redis、RabbitMQ、MinIO

## 🐳 快速启动（Docker 依赖）

创建 `docker-compose.yml` 并启动：

```yaml
version: "3.9"
services:
  redis:
    image: redis:7
    ports: [ "6379:6379" ]

  rabbitmq:
    image: rabbitmq:3-management
    ports: [ "5672:5672", "15672:15672" ]
    environment:
      RABBITMQ_DEFAULT_USER: admin
      RABBITMQ_DEFAULT_PASS: admin

  minio:
    image: minio/minio:latest
    command: server /data --console-address ":9001"
    ports: [ "9000:9000", "9001:9001" ]
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin
    volumes:
      - ./data/minio:/data
```

运行：

```bash
docker compose up -d
# MinIO 控制台 → http://localhost:9001  (账号/密码: minioadmin / minioadmin)
```

## 🔐 配置

### 后端（`application.yml`）

```yaml
spring:
  profiles:
    active: dev

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

spring:
  data:
    redis:
      host: localhost
      port: 6379
  rabbitmq:
    host: localhost
    port: 5672
    username: admin
    password: admin
```

### 前端（`.env.local`）

```dotenv
VITE_API_BASE_URL=http://localhost:8080
VITE_UPLOAD_BUCKET=studysphere
```

## 🚀 启动

### 后端

```bash
cd backend
mvn clean package -DskipTests
java -jar target/*.jar
```

* Swagger UI → `http://localhost:8080/swagger-ui/index.html`
* OpenAPI 文档 → `http://localhost:8080/v3/api-docs`

### 前端

```bash
cd frontend
pnpm i    # 或 npm i / yarn
pnpm dev  # 或 npm run dev / yarn dev
# 访问 → http://localhost:5173
```

## 📚 常用接口

* `POST /api/auth/register` —— 发送验证码并注册
* `POST /api/auth/login` —— 登录获取 JWT
* `POST /api/auth/reset/request` —— 申请重置验证码
* `POST /api/auth/reset/confirm` —— 确认重置
* `GET /api/user/profile` —— 需 `Authorization: Bearer <token>`
* `POST /api/topic` —— 新建话题（需 JWT）

**常用请求头：**

* `Authorization: Bearer <JWT>`
* `X-Trace-Id`（若缺失由过滤器自动生成）

## 🧩 关键实现说明

* 验证码以 `reg:{email}` / `reset:{email}` 存储在 Redis，并自动过期
* RabbitMQ 解耦邮件/消息发送，监听器统一消费
* Spring Security 保护路由，JWT 过滤器校验并注入用户
* Redis 滑动窗口实现基于 IP 的限流
* 基于反射的 DTO/DO 映射工具
* 统一 JSON 错误结构：`{ code, message, traceId, path, timestamp }`
* 雪花 ID 注入日志 MDC 与响应头，便于定位问题

## 🛡 安全与上线建议

* 使用强 JWT 密钥并定期轮换
* 不同环境分别配置 CORS 白名单
* MinIO 数据目录持久化
* 配置 HTTPS 与反向代理（如 Nginx）
* 根据流量调参限流阈值
* 日志集中化（ELK/OpenSearch）并携带 traceId

## 📜 许可证

MIT（或按需更换）
