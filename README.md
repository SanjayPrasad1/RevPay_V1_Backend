# RevPay — Digital Payment Platform

A full-stack fintech application built with **Spring Boot 4** and **Angular 21**, enabling personal users, businesses, and admins to manage money transfers, invoicing, and loans on a single unified platform.

---

## 🚀 RevPay System

This project is split into two main repositories:

### 🧠 Backend 
Handles APIs, authentication, business logic, and service communication.

👉 [Go to Backend Repo](https://github.com/SanjayPrasad1/RevPay_V1_Backend)

---

### 🎨 Frontend (UI Application)
Handles user interface and client-side interactions.

👉 [Go to Frontend Repo](https://github.com/SanjayPrasad1/RevPay_V1_Frontend/tree/main/revpay_frontend)

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Database Schema](#database-schema)
- [Getting Started](#getting-started)
- [Environment Configuration](#environment-configuration)
- [API Reference](#api-reference)
- [Authentication Flow](#authentication-flow)
- [Transaction Logic](#transaction-logic)
- [Loan & EMI System](#loan--emi-system)
- [Scheduled Jobs](#scheduled-jobs)
- [Default Credentials](#default-credentials)

---

## Overview

RevPay solves the problem of fragmented financial tooling by bringing personal users, businesses, and administrators under one roof. A personal user can send money and pay invoices. A business user can issue invoices, apply for loans, and manage EMI repayments. An admin can oversee the entire platform, approve loans, and manage accounts — all through a clean, role-based dashboard.

---

## Features

### Personal Users
- Register and log in with JWT-based authentication
- Send money to any registered user by email
- Top up wallet using saved payment methods
- Request money from other users
- View paginated transaction history
- Export transaction history as a PDF (date-range filter)
- Pay invoices received from businesses

### Business Users
- All personal features, plus:
- Create and send itemized invoices with tax and line items
- Apply for business loans
- View EMI repayment schedule with principal/interest breakdown
- Pay EMIs manually or enable auto-debit
- Track loan lifecycle from PENDING to CLOSED

### Admin
- View and manage all registered users
- Enable or disable user accounts (admin accounts are protected)
- Review and approve or reject loan applications with notes
- View all platform transactions
- Platform-wide dashboard with statistics
- Auto-seeded on first startup — no manual setup needed

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend Framework | Spring Boot 4.0.3 |
| Language | Java 21 |
| Frontend Framework | Angular 21 |
| Frontend Language | TypeScript |
| Database | MySQL |
| ORM | Spring Data JPA / Hibernate |
| Authentication | JWT (JSON Web Tokens) |
| Password Security | BCrypt (cost factor 10) |
| Scheduled Jobs | Spring `@Scheduled` |
| PDF Export | jsPDF (browser-side) |
| Build Tool (BE) | Maven |
| Build Tool (FE) | Angular CLI / npm |

---

## Project Structure

### Backend — `revpay-backend/`

```
src/main/java/com/revpay/
├── entity/                  JPA entities (mapped to DB tables)
│   ├── User.java            users table — auth, roles, reset token
│   ├── Account.java         accounts table — balance, account number
│   ├── Transaction.java     transactions table — every money movement
│   ├── Loan.java            loans table — business loan applications
│   ├── EMI.java             emis table — monthly repayment schedule
│   ├── Invoice.java         invoices table — business billing
│   ├── InvoiceItem.java     invoice_items table — line items
│   ├── PaymentMethod.java   payment_methods table — saved cards
│   ├── Notification.java    notifications table — in-app alerts
│   └── AuditLog.java        audit_logs table — admin action tracking
│
├── repository/              Spring Data JPA interfaces (SQL auto-generated)
│   ├── UserRepository.java
│   ├── AccountRepository.java
│   ├── TransactionRepository.java
│   ├── LoanRepository.java
│   └── EMIRepository.java
│
├── service/                 All business logic
│   ├── AuthService.java     login, register, forgot/reset password
│   ├── TransactionService.java  transfer, top-up, toResponse(tx, viewer)
│   ├── LoanService.java     apply, approve, payEmi, closure
│   ├── DashboardService.java    aggregate data per role
│   ├── InvoiceService.java  create, send, pay invoices
│   └── AdminService.java    user management, loan decisions
│
├── controller/              REST endpoints — thin, delegates to service
│   ├── AuthController.java        /api/auth/**
│   ├── TransactionController.java /api/transactions/**
│   ├── LoanController.java        /api/loans/**
│   ├── InvoiceController.java     /api/invoices/**
│   ├── AdminController.java       /api/admin/**
│   └── DashboardController.java   /api/dashboard
│
├── security/
│   ├── JwtUtil.java          generate and validate JWT tokens
│   ├── JwtAuthFilter.java    intercepts every request, sets SecurityContext
│   └── SecurityConfig.java   defines public vs protected routes
│
├── scheduler/
│   └── EMIScheduler.java     @Scheduled fine imposition + auto-debit
│
├── seeder/
│   └── DataSeeder.java       auto-creates admin user on first startup
│
└── common/
    ├── RevPayException.java  custom exception factory (400/401/403/404/409)
    └── ApiResponse.java      consistent { status, message, data } wrapper
```

### Frontend — `revpay_frontend/src/app/`

```
src/app/
├── auth/
│   ├── login/               login form, JWT storage
│   ├── register/            personal + business registration
│   ├── forgot-password/     email submission form
│   └── reset-password/      reads ?token= from URL, new password form
│
├── personal/
│   ├── dashboard/           balance, recent transactions, quick actions
│   ├── send-money/          transfer form with receiver email lookup
│   ├── top-up/              wallet top-up with saved payment method
│   ├── transactions/        paginated history, PDF export
│   └── money-requests/      create and manage requests
│
├── business/
│   ├── dashboard/           invoices summary, loan status, balance
│   ├── invoices/            create, send, track invoices
│   └── loans/               apply for loan, EMI schedule view, pay EMI
│
├── admin/
│   ├── dashboard/           platform overview stats
│   ├── users/               all users, enable/disable
│   └── loans/               pending applications, approve/reject
│
├── shared/
│   ├── services/            AuthService, TransactionService, etc.
│   ├── guards/              AuthGuard, RoleGuard — protect routes
│   ├── interceptors/        AuthInterceptor — adds JWT to every request
│   └── models/              TypeScript interfaces for all API responses
│
└── app.routes.ts            lazy-loaded route definitions per role
```

---

## Database Schema

RevPay uses 10 MySQL tables. Below is the relationship overview:

```
users (1) ──────────────── (1) accounts
  │                              │
  │ (1:many)                     │ (1:many as sender)
  ▼                              ▼
payment_methods           transactions ◄─── (1:many as receiver)
loans ◄── borrower_id          │
  │                             └── payment_method_id
  │ (1:many)
  ▼
emis

users (1) ──── (1:many) ──► invoices ──── (1:many) ──► invoice_items
users (1) ──── (1:many) ──► notifications
users (1) ──── (1:many) ──► audit_logs
```

**Key design decisions:**

- `transactions.sender_account_id` is **nullable** — loan disbursements come from the system (NULL sender), not a user account
- `transactions.receiver_account_id` is **nullable** — EMI payments go to the system (NULL receiver)
- Users are never hard-deleted — they are disabled (`enabled = 0`) to preserve financial history
- `audit_logs` uses a polymorphic design (`entity_name` + `entity_id`) so one table tracks actions on any entity

---

## Getting Started

### Prerequisites

- Java 21
- Node.js 18+ and npm
- MySQL 8.x
- Maven 3.8+

### Backend Setup

```bash
# 1. Clone the repository
git clone https://github.com/your-username/revpay.git

# 2. Create the MySQL database
mysql -u root -p
CREATE DATABASE revpay_db;
EXIT;

# 3. Configure application.properties (see Environment Configuration)

# 4. Run the backend
cd revpay-backend
mvn spring-boot:run
```

The backend starts on `http://localhost:8080`. On first run, the admin account is auto-created by `DataSeeder.java`.

### Frontend Setup

```bash
# 1. Install dependencies
cd revpay_frontend
npm install

# 2. Start the development server
ng serve
```

The frontend starts on `http://localhost:4200`.

---

## Environment Configuration

Create or edit `src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/revpay_db
spring.datasource.username=root
spring.datasource.password=your_mysql_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# JWT
jwt.secret=your-256-bit-secret-key-here
jwt.expiration=86400000

# Scheduler
spring.task.scheduling.enabled=true
```

> **Note:** The forgot password feature currently prints the reset link to the console (`System.out.println`). In production, replace this with a real email service (SendGrid, AWS SES, etc.).

---

## API Reference

All endpoints are prefixed with `/api`. Protected endpoints require the header:

```
Authorization: Bearer <jwt_token>
```

### Auth (Public)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register/personal` | Register a personal user |
| POST | `/api/auth/register/business` | Register a business user |
| POST | `/api/auth/login` | Login — returns JWT + user info |
| POST | `/api/auth/forgot-password` | Request password reset token |
| POST | `/api/auth/reset-password` | Reset password using token |

### Dashboard (Protected)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/dashboard` | Role-specific dashboard data |

### Transactions (Protected)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/transactions/transfer` | Send money to another user |
| POST | `/api/transactions/top-up` | Add money from payment method |
| GET | `/api/transactions/history` | Paginated history (`?page=0&size=10`) |
| GET | `/api/transactions/export` | Export with optional date filter |

### Loans & EMI (Protected, Business only)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/loans/apply` | Apply for a business loan |
| GET | `/api/loans/my` | My loans with full EMI schedule |
| POST | `/api/loans/{loanId}/emis/{emiId}/pay` | Pay an EMI manually |
| PATCH | `/api/loans/{loanId}/auto-debit` | Toggle auto-debit on/off |

### Invoices (Protected)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/invoices` | Create an invoice |
| GET | `/api/invoices` | Get my invoices |
| POST | `/api/invoices/{id}/send` | Send invoice to client |
| POST | `/api/invoices/{id}/pay` | Pay an invoice (client) |
| DELETE | `/api/invoices/{id}` | Delete a draft invoice |

### Money Requests (Protected)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/money-requests` | Create a money request |
| GET | `/api/money-requests/incoming` | Requests sent to me |
| GET | `/api/money-requests/outgoing` | Requests I sent |
| POST | `/api/money-requests/{id}/fulfill` | Pay a request |
| POST | `/api/money-requests/{id}/reject` | Decline a request |

### Admin (Protected, Admin role only)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/users` | All registered users |
| PATCH | `/api/admin/users/{id}/toggle-status` | Enable or disable a user |
| GET | `/api/admin/loans/pending` | Pending loan applications |
| POST | `/api/admin/loans/{id}/approve` | Approve a loan |
| POST | `/api/admin/loans/{id}/reject` | Reject a loan |
| GET | `/api/admin/transactions` | All platform transactions |
| GET | `/api/admin/dashboard` | Platform statistics |

---

## Authentication Flow

RevPay uses **stateless JWT authentication**.

1. User submits credentials → `POST /api/auth/login`
2. Backend verifies the account is enabled (before password check — gives a clearer error message)
3. BCrypt compares the submitted password against the stored hash
4. On success, a signed JWT is generated containing `{ email, role, iat, exp }`
5. Frontend stores the token in `localStorage` under key `revpay_access_token`
6. Angular's `AuthInterceptor` automatically attaches `Authorization: Bearer <token>` to every subsequent HTTP request
7. Spring's `JwtAuthFilter` validates the token signature and expiry on every request
8. `@AuthenticationPrincipal` in controllers gives direct access to the logged-in user

**Password Reset** uses a UUID token (32-char hex, 1-hour expiry) stored in the `users` table. The token is cleared after a successful reset and cannot be reused.

---

## Transaction Logic

Every money movement — transfer, top-up, loan disbursement, EMI payment, invoice payment — creates one row in the `transactions` table.

The same transaction record looks different depending on who is viewing it:

- For the **sender** → it is a **DEBIT** (money went out, shown in red)
- For the **receiver** → it is a **CREDIT** (money came in, shown in green)

This is resolved by `TransactionService.toResponse(transaction, viewerAccount)`, which determines credit or debit based on four rules:

1. `DEPOSIT` type → always **credit** (top-up adds money to your own wallet)
2. `sender_account_id` is NULL → always **credit** (system/loan disbursement)
3. Viewer is the receiver and NOT the sender → **credit**
4. Everything else → **debit**

All balance-affecting operations are wrapped in `@Transactional` to guarantee atomicity. If a transaction record fails to save, the balance updates are rolled back automatically.

---

## Loan & EMI System

### Lifecycle
`PENDING` → `APPROVED` → `ACTIVE` → `CLOSED` (or `REJECTED`)

### EMI Formula

```
EMI = P × r × (1 + r)^n
      ──────────────────
           (1 + r)^n − 1

P = Principal amount
r = Monthly interest rate (annual rate ÷ 12 ÷ 100)
n = Tenure in months
```

**Example:** ₹10,000 at 12% per year for 12 months = **₹888.49/month**

On loan approval:
- `n` EMI rows are inserted into the `emis` table (one per month)
- Money is credited to the business account
- A `LOAN_DISBURSEMENT` transaction is created with `sender_account_id = NULL`

When the last EMI is paid, the loan status is automatically set to `CLOSED`.

---

## Scheduled Jobs

Two background jobs run daily via Spring `@Scheduled`:

| Job | Cron | What it does |
|-----|------|-------------|
| Fine Imposition | `0 0 9 * * *` (9:00 AM) | Marks unpaid past-due EMIs as `OVERDUE`, applies a 2% daily fine |
| Auto-Debit | `0 30 9 * * *` (9:30 AM) | Pays due EMIs for loans with auto-debit enabled, if balance is sufficient |

The fine job runs 30 minutes before auto-debit so overdue fines are included in the amount when auto-debit processes them.

---

## Default Credentials

The admin account is auto-created on first startup by `DataSeeder.java`:

| Field | Value |
|-------|-------|
| Email | `admin@revpay.com` |
| Password | `Admin@123` |
| Role | `ADMIN` |

> Change these credentials before any deployment beyond local development.

---

## Known Limitations / Future Improvements

- **Email service:** Password reset links are printed to the console. Production requires a real email provider (SendGrid, AWS SES).
- **Payment gateway:** Top-up is simulated. Real card processing would need Stripe or Razorpay integration.
- **JWT storage:** `localStorage` is used for convenience. Production should use `HttpOnly` cookies to prevent XSS attacks.
- **Refresh tokens:** Current JWTs expire after 24 hours with no silent renewal. A refresh token system would improve UX.
- **Email verification:** The `email_verified` column exists in the DB but the verification flow is not yet implemented.
- **Rate limiting:** Auth endpoints have no rate limiting. `Bucket4j` or similar should be added before deployment.

---

*RevPay — Built with Spring Boot 4, Angular 21, MySQL, JWT*
