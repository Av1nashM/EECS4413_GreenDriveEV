# EECS4413_GreenDriveEV

## Description

GreenDriveEV is a full-stack electric vehicle marketplace web application developed for EECS4413 at York University. It allows users to browse electric vehicles, manage accounts, use shopping cart functionality, and estimate monthly vehicle payments.

## Technologies

- **Backend:** Java 21, Spring Boot
- **Frontend:** React 19, Vite
- **Database:** PostgreSQL
- **Build Tool:** Maven
- **Data Persistence:** Spring Data JPA / Hibernate

## Setup Instructions

### 1. Install Requirements

- Java 21 or newer
- PostgreSQL
- Node.js and npm

### 2. Configure the Database

Create a PostgreSQL database named `GreenDriveEV`.

Open `backend/src/main/resources/application.properties` and configure the database connection:

\`\`\`properties
spring.application.name=greendriveev
spring.datasource.url=jdbc:postgresql://localhost:5432/GreenDriveEV
spring.datasource.username=postgres
spring.datasource.password=YOUR_POSTGRES_PASSWORD
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
\`\`\`

Replace `YOUR_POSTGRES_PASSWORD` with your PostgreSQL password.

### 3. Install Frontend Dependencies

From the project root, run:

\`\`\`powershell
cd frontend/my-react-app
npm install
\`\`\`

## Running the Application

The backend and frontend must run in separate terminals.

### 1. Start the Backend

From the project root, run:

\`\`\`powershell
cd backend
.\mvnw.cmd spring-boot:run
\`\`\`
or
\`\`\`Run Java file "GreendriveevApplication.java
\`\`\`

The backend runs at `http://localhost:8080` by default.

### 2. Start the Frontend

Open a second terminal at the project root and run:

\`\`\`powershell
cd frontend/my-react-app
npm run dev
\`\`\`

Open the local URL displayed by Vite, usually `http://localhost:5173`, in your browser.
