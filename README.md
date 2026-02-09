# TaskFlow

A **Jira-like** task and project management system built with a microservices architecture using Spring Boot.

## 📋 Overview

TaskFlow is a modern, scalable task management platform designed to help teams organize, track, and manage their projects and tasks efficiently. The application follows a microservices architecture pattern for better scalability, maintainability, and independent deployment of services.

## 🏗️ Architecture

This project follows a **microservices architecture** with the following services:

| Service | Description | Port |
|---------|-------------|------|
| **Gateway Service** | API Gateway for routing and load balancing | - |
| **Auth Service** | Authentication and authorization (JWT-based) | - |
| **Project Service** | Project management and operations | - |
| **Task Service** | Task management and operations | - |

## 🛠️ Tech Stack

### Core Framework
- **Java 17**
- **Spring Boot 3.5.x**
- **Gradle** (Build Tool)

### Services & Libraries

#### Gateway Service
- Spring Cloud Gateway (WebMVC)

#### Auth Service
- Spring Security
- Spring OAuth2 Client
- JWT (jjwt) for token-based authentication
- Spring Data JPA
- PostgreSQL
- Flyway (Database migrations)
- Spring Mail & Thymeleaf (Email templates)

#### Project Service
- Spring Web MVC
- Spring Actuator
- Lombok

#### Task Service
- Spring Web MVC
- Lombok

### Database
- **PostgreSQL** - Primary database
- **H2** - In-memory database for testing
- **Flyway** - Database migration tool

### Other Technologies
- **Spring Cloud** (2025.1.0)
- **Lombok** - Boilerplate code reduction
- **JUnit 5** - Testing framework

## 📁 Project Structure

```
taskflow/
├── auth-service/          # Authentication & Authorization
├── gateway-service/       # API Gateway
├── project-service/       # Project Management
├── task-service/          # Task Management
├── build.gradle           # Root build configuration
└── settings.gradle        # Multi-project settings
```

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- Gradle 8.x
- PostgreSQL (for auth-service)

### Build the Project

```bash
./gradlew build
```

### Run Individual Services

```bash
# Auth Service
./gradlew :auth-service:bootRun

# Gateway Service
./gradlew :gateway-service:bootRun

# Project Service
./gradlew :project-service:bootRun

# Task Service
./gradlew :task-service:bootRun
```

## 📝 License

This project is licensed under the MIT License.
