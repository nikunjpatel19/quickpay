# QuickPay • Android + Ktor Backend Monorepo

QuickPay is a full-stack payment flow demo inspired by real-world Finix and Stripe-style payment systems.
It demonstrates how a mobile client and a backend coordinate to create hosted payment links, track order state, and process webhook-driven payment updates.

The project intentionally focuses on **backend correctness, webhook reliability, and clean architecture** rather than UI polish.

```
quickpay/
 ├── android/        ← Android app (Jetpack Compose, Kotlin, MVVM)
 ├── backend/        ← Ktor backend (Kotlin, Postgres, Webhooks)
 └── README.md
```

---

## Tech Stack

### Android
- Kotlin
- Jetpack Compose (Material 3)
- MVVM with ViewModel + StateFlow
- Retrofit + Moshi
- Navigation Component
- Chrome Custom Tabs for hosted checkout
- Basic unit and instrumentation tests

### Backend
- Kotlin
- Ktor server
- Exposed ORM
- PostgreSQL
- Flyway migrations
- REST APIs for payment links and orders
- Webhook ingestion and processing (Finix-style)

---

## High-Level Payment Flow

1. Android requests payment link creation
2. Backend creates a hosted payment link via Finix
3. Backend persists the payment link and internal order
4. User completes checkout in the browser
5. Finix sends webhook events to the backend
6. Backend updates order and payment state
7. Android polls backend for final status

---

## Key Features

### Payment Link Creation
- Android sends amount, currency, and description
- Backend creates a Finix Payment Link
- Internal `order_id` is injected into Finix tags
- Checkout URL returned to Android

### Hosted Checkout
- Checkout page opens in Chrome Custom Tab
- No card data ever touches the Android app

### Order Status Polling
- Android polls `/v1/orders/{id}`
- Backend acts as the source of truth
- Order transitions driven by webhook events

### Webhook Handling (Finix-Style)
- Backend exposes `/v1/finix/webhook`
- Handles:
  - Payment Link created / updated
  - Transfer created / updated
- Raw webhook payloads are persisted before processing
- Webhooks are idempotent using Finix event IDs
- Business logic is isolated from transport concerns

### Developer Utilities
- Dev endpoint to manually mark orders for testing
- Enables Android UI testing without waiting for webhooks

---

## Database Design

### Orders
- Internal order lifecycle (`CREATED`, `CAPTURED`, `FAILED`)
- Currency and amount stored in cents
- Updated exclusively via webhook events

### Payment Links
- Maps internal order IDs to Finix payment links
- Stores checkout URL and current status

### Webhook Events
- Stores raw webhook payloads
- Tracks processing state
- Supports idempotency and safe retries
- Designed for production-style observability

---

## Running the Backend (Local)

```bash
cd backend
./gradlew run
```

Backend runs at:

```
http://127.0.0.1:8080
```

### Example: Create a Payment Link

```bash
curl -X POST http://127.0.0.1:8080/v1/links   -H "Content-Type: application/json"   -d '{
    "amountCents": 999,
    "currency": "USD",
    "description": "Coffee"
  }'
```

### Example: Dev Order Status Update

```bash
curl -X POST   "http://127.0.0.1:8080/v1/dev/orders/{id}/mark?status=CAPTURED"
```

---

## Running the Android App

The Android emulator uses the special host mapping:

```
BASE_URL = http://10.0.2.2:8080/
```

Open the `android` directory in Android Studio and run the app.

---

## API Endpoints

| Method | Path | Description |
|------|------|-------------|
| POST | /v1/links | Create a payment link |
| GET | /v1/orders/{id} | Get order status |
| GET | /v1/orders | List recent orders |
| POST | /v1/finix/webhook | Receive Finix webhook events |
| POST | /v1/dev/orders/{id}/mark | Dev utility for testing |

---

## Deployment (Render)

The backend is deployed using **Render** as a containerized Ktor service.

### Deployment Details
- Docker-based deployment
- Managed HTTPS
- Managed Postgres instance
- Automatic deploys from GitHub

### Environment Variables

Configured in Render:

```
FINIX_USERNAME
FINIX_PASSWORD
DATABASE_URL
```

Secrets are never committed to the repository.

### Webhook Configuration
- Finix webhooks point to the Render deployment:

```
https://<render-service>.onrender.com/v1/finix/webhook
```

- Webhook events are persisted before processing
- Duplicate and retry events are handled safely

### Notes
- Free-tier instances may cold start
- Webhooks are designed to tolerate retries and delays

---

## Repository Workflow

Single Git repository containing both Android and backend.

| IDE | Path |
|---|---|
| Android Studio | quickpay/android |
| IntelliJ IDEA | quickpay/backend |

Example commits:

```bash
git commit -m "android: add payment link flow and order polling"

git commit -m "backend: add webhook ingestion and order state handling"
```

---

## Project Status

This project is actively evolving and emphasizes:
- Backend correctness
- Payment lifecycle modeling
- Webhook-driven state transitions
- Clean separation of concerns

UI polish is intentionally secondary.

---

## Roadmap

- Webhook signature verification (Finix)
- Background webhook replay worker
- Admin dashboard for merchants
- Deployment hardening and observability
