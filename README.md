# EECS4413_GreenDriveEV

## Description

GreenDriveEV is a full-stack electric vehicle marketplace web application developed for EECS4413 at York University. It allows users to browse electric vehicles, manage accounts, use shopping cart functionality, and estimate monthly vehicle payments.

## Technologies

- **Backend:** Java 21, Spring Boot
- **Frontend:** React 19, Vite
- **Database:** PostgreSQL
- **Build Tool:** Maven
- **Data Persistence:** Spring Data JPA / Hibernate

## Running the Application

The backend and frontend are running on the cloud via Render, it is hosted here: https://eecs4413-greendriveev-react.onrender.com

## Running Locally

If you want to run the project locally, you must follow these instructions

## Setup Instructions

### 1. Install Requirements

- Java 21 or newer
- PostgreSQL
- Node.js and npm

### 2. Configure the Database

Create a PostgreSQL database named `GreenDriveEV`.

Open `backend/src/main/resources/application.properties` and configure the database connection:

\`\`\`properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/GreenDriveEV}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

server.port=${PORT:8080}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
\`\`\`

Have the environment variables set up on your device

### 3. Install Frontend Dependencies

From the project root, run:

\`\`\`powershell
cd frontend/my-react-app
npm install
\`\`\`

## 4. Change backend location

Find any instance of the backend at https://eecs4413-greendriveev.onrender.com and change it to localhost

## Running the Application

The backend and frontend must run in separate terminals.

### 1. Start the Backend

From the project root, run:

\`\`\`powershell
cd backend
.\mvnw.cmd spring-boot:run
\`\`\`

The backend runs at `http://localhost:8080` by default.

### 2. Start the Frontend

Open a second terminal at the project root and run:

\`\`\`powershell
cd frontend/my-react-app
npm run dev
\`\`\`

Open the local URL displayed by Vite, usually `http://localhost:5173`, in your browser.


