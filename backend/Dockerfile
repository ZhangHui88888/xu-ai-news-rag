# XU-News-AI-RAG Backend Dockerfile
# 多阶段构建：Maven编译 + OpenJDK运行

# ==== 构建阶段 ====
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# 复制pom.xml并下载依赖（利用Docker缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码并编译
COPY src ./src
RUN mvn clean package -DskipTests -B

# ==== 运行阶段 ====
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 创建非root用户
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# 从构建阶段复制jar包
COPY --from=build /app/target/news-ai-rag.jar app.jar

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/auth/health || exit 1

# 启动应用
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
CMD ["--spring.profiles.active=prod"]

