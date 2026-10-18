# EECS4413_GreenDriveEV

## Overview
GreenDriveEV is a full‑stack web application built with **Spring Boot (Java)** for the backend and a future **frontend** (HTML/React/Vue) planned for development.  
This repository currently includes a working backend server and a placeholder homepage.

---

## Setup Instructions

### 1. Install Requirements
- **Java 21** or newer  
- **PostgreSQL** (with a database named `GreenDriveEV`)  
- **Maven** (optional — the project includes `mvnw.cmd`)

---

### 2. Configure Database
Edit `backend/src/main/resources/application.properties`:

```properties
spring.application.name=greendriveev
spring.datasource.url=jdbc:postgresql://localhost:5432/GreenDriveEV
spring.datasource.username=postgres
spring.datasource.password=PASSWORD
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### Run
Run the GreendriveevApplication.java file
