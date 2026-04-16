# Batch Management System - Implementation and Setup Guide

This document explains:
- what is implemented in backend and frontend
- how to set up and run backend in Spring Tool Suite (STS)
- how to set up and run frontend
- how to verify end-to-end flow

---

## 1) Project Structure

The project is split into two independent folders:

- `backend` -> Spring Boot API (Java 17, Spring Security JWT, JPA, MySQL, Maven)
- `frontend` -> React app (Vite, Tailwind CSS, Axios, React Router v6)

---

## 2) Backend Implementation Summary

Path: `backend/src/main/java/com/batchmanagement/backend`

### Main Modules

- `controller`
  - `AuthController` -> `POST /api/auth/login`
  - `AdminController` -> trainers/admins/batches/dashboard APIs
  - `TrainerController` -> trainer batch APIs

- `service`
  - `AuthService` -> login and token response
  - `AdminService` -> admin operations (create/manage users, assign batch, dashboard)
  - `TrainerService` -> trainer operations (create batch, update progress, mark complete)

- `repository`
  - `UserRepository`
  - `BatchRepository`

- `entity`
  - `User` (id, name, email, password, role)
  - `Batch` (id, domainName, dates, trainer, progress, status, time, labNo, noOfStudents)
  - enums `Role`, `BatchStatus`

- `security`
  - `SecurityConfig` -> route access rules + CORS + stateless auth
  - `JwtAuthFilter` -> extracts/validates token from header
  - `JwtService` -> create/validate JWT with role claim
  - `UserDetailsServiceImpl` -> loads user by email

- `dto`
  - request/response objects for auth, admin, trainer, and common flows

- `exception`
  - global exception handling and custom exceptions

- `config`
  - `DataInitializer` -> creates default admin if not present

### Security Rules Implemented

- `POST /api/auth/login` -> public
- `/api/admin/**` -> ADMIN only
- `/api/trainer/**` -> TRAINER only
- all other APIs require authentication

### Authentication Behavior

- login with email/password
- JWT generated on success
- role included in token claims
- frontend sends token in `Authorization: Bearer <token>`

---

## 3) Database Setup (MySQL)

### Option A (Recommended): Run provided SQL file

1. Open MySQL Workbench (or CLI).
2. Open file: `backend/batch_management.sql`
3. Execute script.

This creates:
- database `batch_management`
- tables `users`, `batches`
- constraints and default admin row

Default admin:
- email: `admin@batch.com`
- password: `admin123`

### Option B: Let Hibernate create tables

If you keep `spring.jpa.hibernate.ddl-auto=update`, backend can auto-create tables.
Still, using the SQL file is better for consistent setup.

---

## 4) Backend Setup in Spring Tool Suite (STS)

### Prerequisites

- Java 17 installed
- Maven available (STS can use embedded Maven)
- MySQL running

### Import Project

1. Open STS.
2. Go to `File -> Import -> Maven -> Existing Maven Projects`.
3. Browse and select `Batch_Management/backend`.
4. Finish import.

### Configure Database Properties

Open: `backend/src/main/resources/application.properties`

Update if needed:
- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

Default currently:
- URL: `jdbc:mysql://localhost:3306/batch_management?...`
- username: `root`
- password: `root`

### Run Backend

Method 1 (from STS):
1. Right click `BackendApplication`.
2. `Run As -> Spring Boot App`.

Method 2 (terminal):
```bash
cd backend
mvn spring-boot:run
```

Backend runs on:
- `http://localhost:8080`

---

## 5) Frontend Setup and Run

### Prerequisites

- Node.js (LTS recommended)
- npm

### Install and Run

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on:
- `http://localhost:5173`

### Build Frontend

```bash
cd frontend
npm run build
```

---

## 6) API Base URL and Token Flow

Frontend Axios config:
- file: `frontend/src/api/axios.ts`
- base URL: `http://localhost:8080/api`

Token storage:
- token stored in `localStorage`
- interceptor attaches token to every request

---

## 7) First Time End-to-End Test

1. Start MySQL.
2. Execute `backend/batch_management.sql`.
3. Start backend in STS.
4. Start frontend with `npm run dev`.
5. Open `http://localhost:5173`.
6. Login as:
   - email: `admin@batch.com`
   - password: `admin123`
7. Create trainer users from Admin panel.
8. Login as trainer and create/update batches.
9. Use admin panel to assign and monitor batches.

---

## 8) Implemented API List

### Auth
- `POST /api/auth/login`

### Admin
- `GET /api/admin/trainers`
- `POST /api/admin/trainers`
- `PUT /api/admin/trainers/{id}`
- `DELETE /api/admin/trainers/{id}`
- `GET /api/admin/admins`
- `POST /api/admin/admins`
- `GET /api/admin/batches`
- `POST /api/admin/assign-batch`
- `GET /api/admin/dashboard`

### Trainer
- `GET /api/trainer/batches`
- `POST /api/trainer/batches`
- `PUT /api/trainer/progress/{id}`
- `PUT /api/trainer/complete/{id}`

---

## 9) Notes

- No signup/public registration is implemented.
- Only ADMIN can create users (admins/trainers).
- Backend and frontend are fully separated and independently runnable.
- If login fails after running SQL, ensure DB user/password in `application.properties` match your local MySQL setup.
