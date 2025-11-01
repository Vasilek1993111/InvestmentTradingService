# ===========================================
# Multi-stage Dockerfile для Investment Trading Service
# ===========================================

# Stage 1: Build stage
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Копируем только pom.xml для кэширования зависимостей
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем исходный код и собираем приложение
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Устанавливаем wget для health check
RUN apk add --no-cache wget

# Устанавливаем рабочую директорию
WORKDIR /app

# Создаем пользователя для безопасности (не root)
RUN addgroup -S spring && adduser -S spring -G spring

# Копируем JAR из build stage
COPY --from=build /app/target/*.jar app.jar

# Меняем владельца файлов
RUN chown spring:spring app.jar

# Переключаемся на непривилегированного пользователя
USER spring:spring

# JVM параметры для ограничения памяти и оптимизации
# -Xmx1536m: максимальная heap память 1.5GB (оставляем ~500MB для off-heap)
# -Xms512m: начальная heap память 512MB
# -XX:+UseG1GC: использование G1 сборщика мусора
# -XX:MaxGCPauseMillis=200: максимальная пауза GC 200ms
ENV JAVA_OPTS="-Xmx1536m -Xms512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Expose порт (будет переопределен через переменные окружения)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:${SERVER_PORT:-8080}/actuator/health || exit 1

# Запуск приложения
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

