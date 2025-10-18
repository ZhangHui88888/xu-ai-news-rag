# XU-News-AI-RAG Backend Dockerfile
# 轻量构建：使用本地构建的 jar 文件（适用于 VMware 环境）

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 创建非root用户
RUN addgroup -S spring && adduser -S spring -G spring

# 复制本地构建的jar包
# 注意：需要先在宿主机执行 mvn clean package -DskipTests
COPY target/*.jar app.jar

# 修改文件所有者
RUN chown -R spring:spring /app

# 切换到非root用户
USER spring:spring

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/auth/health || exit 1

# 启动应用
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
CMD ["--spring.profiles.active=prod"]

