# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

MyLiftIsRPG is a Spring Boot application built with Kotlin that gamifies weightlifting and fitness tracking. The application uses Spring Boot 3.5.5 with Gradle 8.14.3 as the build tool.

## Essential Commands

### Development
```bash
# Start MySQL database (required before running the app)
docker compose up -d

# Run the application with hot-reload
./gradlew bootRun

# Stop the application
# Press Ctrl+C in the terminal or kill the background process

# Stop MySQL database
docker compose down
```

### Build and Testing
```bash
# Build the application
./gradlew build

# Run all tests
./gradlew test

# Run tests with detailed output
./gradlew test --info

# Clean build artifacts
./gradlew clean

# Build without running tests
./gradlew build -x test

# Create executable JAR
./gradlew bootJar

# Build Docker image
./gradlew bootBuildImage
```

### Database Management
The application uses MySQL running in Docker. Database connection details are configured in `src/main/resources/application.properties`:
- Database: mydatabase
- Username: myuser
- Password: secret
- Port: 3306

## Architecture

### Technology Stack
- **Language**: Kotlin 1.9.25
- **Framework**: Spring Boot 3.5.5
- **Database**: MySQL (via Docker Compose)
- **ORM/Query**: JOOQ with Spring JDBC
- **Security**: Spring Security
- **Build Tool**: Gradle with Kotlin DSL

### Package Structure
- `com.mylifeisrpg.myliftisrpg` - Base package containing:
  - `MyLiftIsRpgApplication.kt` - Spring Boot application entry point
  - `SecurityConfig.kt` - Security configuration allowing public access to `/` and `/api/**`
  - `HomeController.kt` - REST controller with health check endpoints

### Key Dependencies
- `spring-boot-starter-web` - Web server and REST capabilities
- `spring-boot-starter-jdbc` - Database connectivity
- `spring-boot-starter-jooq` - Type-safe SQL query construction
- `spring-boot-starter-security` - Authentication and authorization
- `spring-boot-devtools` - Hot reload during development
- `spring-boot-docker-compose` - Auto-manages Docker containers
- `mysql-connector-j` - MySQL JDBC driver

### Security Configuration
The application has Spring Security configured with:
- Public endpoints: `/`, `/api/**`
- CSRF protection disabled for API endpoints
- All other endpoints require authentication

### Docker Integration
Spring Boot Docker Compose support automatically starts/stops the MySQL container when running the application. The `compose.yaml` file defines the MySQL service configuration.

## Development Workflow

1. Ensure Docker is running on your system
2. MySQL container will auto-start when you run `./gradlew bootRun`
3. Application runs on `http://localhost:8080`
4. DevTools provides automatic restart on code changes
5. Test endpoints:
   - `GET /` - Returns welcome message
   - `GET /api/health` - Returns health status

## Important Notes

- The application requires Java 21 (configured via toolchain)
- Gradle Daemon is used by default for faster builds
- Spring DevTools is included for development only
- The application uses HikariCP for connection pooling
- JPA/Hibernate DDL is set to `update` mode (auto-creates/updates schema)