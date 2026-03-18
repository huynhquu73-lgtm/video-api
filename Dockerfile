# Bước 1: Build ứng dụng
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# Copy file cấu hình dự án
COPY pom.xml .

# Tạo cấu trúc thư mục chuẩn (Cực kỳ quan trọng để Java tìm thấy file)
RUN mkdir -p src/main/java/com/video/backend
RUN mkdir -p src/main/resources

# Copy code Java vào đúng chỗ
COPY VideoApiApplication.java src/main/java/com/video/backend/

# Copy file mật khẩu Database vào đúng chỗ để Java đọc được
COPY application.properties src/main/resources/

# Bắt đầu đóng gói (Package)
RUN mvn clean package -DskipTests

# Bước 2: Chạy ứng dụng
FROM openjdk:17.0.1-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
