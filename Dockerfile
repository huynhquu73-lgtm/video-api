FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
# Tự động tạo thư mục chuẩn cho Java Spring Boot
RUN mkdir -p src/main/java/com/video/backend
# Copy file Java vào đúng thư mục vừa tạo
COPY VideoApiApplication.java src/main/java/com/video/backend/
RUN mvn clean package -DskipTests

FROM openjdk:17.0.1-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
