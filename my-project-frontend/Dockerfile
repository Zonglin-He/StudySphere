# syntax=docker/dockerfile:1

# 构建阶段：在 Node 环境中编译前端资源
FROM node:18-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
# 通过 build ARG 注入 Vite 变量（你的代码里需使用 import.meta.env.VITE_API_BASE）
ARG VITE_API_BASE=/api
ENV VITE_API_BASE=${VITE_API_BASE}
RUN npm run build

# 运行阶段：使用 Nginx 托管静态资源
FROM nginx:1.25-alpine
COPY --from=builder /app/dist /usr/share/nginx/html
# 覆盖默认站点配置，启用 /api 反代与 SPA 回退
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
