# Swasth – Healthcare Platform Backend

## Overview
**Swasth** is a healthcare platform backend built with **Spring Boot 3.2.5** and **Java 21**. It provides features for managing families, patients, medications, and prescriptions.

## Tech Stack
- **Language**: Java 21
- **Framework**: Spring Boot 3.2.5
- **Build Tool**: Gradle
- **Database**: H2 (In-memory)
- **Security**: Spring Security with JWT
- **Documentation**: SpringDoc OpenAPI (Swagger UI)
- **Utilities**: Lombok, Apache Commons Lang3

## Project Structure
- `com.swasth.swasth.config`: Configuration classes for OpenAPI and Security.
- `com.swasth.swasth.controller`: REST Controllers for Family, Medication, Patient, Prescription, and User.
- `com.swasth.swasth.dto`: Data Transfer Objects for requests and responses.
- `com.swasth.swasth.entities`: JPA Entities (Family, Medication, MedicationDoseLog, Patient, Prescription, User).
- `com.swasth.swasth.repositories`: Spring Data JPA Repositories.
- `com.swasth.swasth.security`: JWT Filter, Utility, and Custom User Details Service.
- `com.swasth.swasth.service`: Business logic for Auth, Family, Medication, Prescription, and User.
- `com.swasth.swasth.storage`: File storage services for handling uploads.

## Key Features
- **User Authentication**: JWT-based login and registration.
- **Family Management**: Create and join families to manage health records collectively.
- **Patient Management**: Track individual patient profiles within a family.
- **Medication Tracking**: Log medication doses and track schedules.
- **Prescription Management**: Upload and manage prescriptions with local file storage.
- **API Documentation**: Interactive Swagger UI at `/swagger-ui.html`.

## Getting Started
### Prerequisites
- JDK 21
- Gradle

### Configuration
Key configurations are in `src/main/resources/application.properties`:
- **Port**: 8080
- **Database**: H2 Console available at `/h2-console`
- **File Uploads**: Prescriptions are stored in `./uploads`

### Running the Application
```bash
./gradlew bootRun
```
