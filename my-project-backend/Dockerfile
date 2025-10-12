# syntax=docker/dockerfile:1

# 构建阶段：使用 Maven 打包可执行的 Spring Boot 应用
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests clean package spring-boot:repackage

# 运行阶段：使用轻量级 JRE 镜像
FROM eclipse-temurin:17-jre
WORKDIR /app
ENV SPRING_PROFILES_ACTIVE=prod
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080                       # <-- 与 Spring Boot 默认端口保持一致
ENTRYPOINT ["java","-jar","/app/app.jar"]
