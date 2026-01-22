# QuickPay  
Android + Ktor Payment Link System

QuickPay is a full-stack payment link system that models how modern merchant platforms create, track, and resolve payments using hosted checkout flows.

The project focuses on payment lifecycle correctness, state-driven UI, and backend-authoritative order management, similar to systems used by fintech providers such as Finix or Stripe.

This repository demonstrates how an Android client and a Kotlin backend coordinate around payment links, QR-based checkout, webhooks, and terminal payment states.

---

## Overview

QuickPay supports creating hosted payment links that can be shared via QR code or URL.  
Payments are completed externally, while the system tracks status transitions internally and reflects them back to the merchant interface.

The design prioritizes:
- Clear payment state modeling
- Webhook-driven backend logic
- Explicit terminal states
- A clean, merchant-facing UI

---

## Screenshots

> Screenshots represent real application states from the merchant interface.

### Payment Creation
<p>
  <img src="screenshots/create_payment_empty.png" width="280" />
  <img src="screenshots/create_payment_filled.png" width="280" />
</p>

### Active Payment Link
<p>
  <img src="screenshots/payment_pending.png" width="320" />
</p>

### Payment Completed
<p>
  <img src="screenshots/payment_paid.png" width="320" />
</p>

### Payment Cancelled
<p>
  <img src="screenshots/payment_cancelled.png" width="320" />
</p>

### Transaction History
<p>
  <img src="screenshots/payment_history.png" width="360" />
</p>

---

## Core Capabilities

### Payment Link Creation
- Merchant defines amount, currency, description, and optional reference
- Backend creates a hosted payment link and persists an order record
- QR code and checkout URL are generated for customer use

### Hosted Checkout
- Payments are completed outside the Android app
- No sensitive payment data is handled by the client
- Android acts as a merchant control surface, not a payment processor

### Payment State Management
All payment states are controlled by the backend and updated via webhook events.

Supported states:
- CREATED
- PENDING
- CAPTURED
- FAILED
- CANCELLED

Terminal states (CAPTURED, FAILED, CANCELLED) are immutable.

### Cancel Payment Flow
- Active payment links can be cancelled by the merchant
- Cancellation invalidates the hosted checkout link
- Cancelled payments remain visible for audit and history

### Transaction History
- Chronological list of payment attempts
- Displays amount, currency, reference, status, and timestamp
- Timestamps are rendered in America/Toronto timezone

---

## Tech Stack

### Android
- Kotlin
- Jetpack Compose (Material 3)
- MVVM architecture
- ViewModel + StateFlow
- Retrofit + Moshi
- Navigation Component
- Chrome Custom Tabs

### Backend
- Kotlin
- Ktor
- PostgreSQL
- Exposed ORM
- Flyway migrations
- REST APIs
- Webhook ingestion and processing

---

## Architecture

```
Android Client
     |
     | REST API
     v
Ktor Backend
     |
     | Webhooks
     v
Payment Provider
```

### Architectural Principles
- Backend is the single source of truth
- Payment outcomes are webhook-driven
- Client state is derived, never assumed
- Terminal states cannot be reversed

---

## API Surface (High Level)

| Method | Endpoint | Purpose |
|------|---------|---------|
| POST | /v1/links | Create payment link |
| GET | /v1/orders/{id} | Retrieve order state |
| GET | /v1/orders | List recent orders |
| POST | /v1/links/{id}/cancel | Cancel active link |
| POST | /v1/webhooks | Process payment events |

---

## Running Locally

### Backend
```bash
cd backend
./gradlew run
```

Runs on:
```
http://127.0.0.1:8080
```

### Android
- Open android/ in Android Studio
- Emulator base URL:
```
http://10.0.2.2:8080
```

---

## Project Direction

QuickPay is structured as a production-style payment system intended for portfolio and technical review.

The emphasis is on:
- Payment lifecycle modeling
- Backend authority and correctness
- Clear merchant-facing UX
- Realistic constraints and trade-offs

---

## Planned Enhancements

- Webhook signature verification
- Background reconciliation jobs
- Analytics and reporting endpoints
- Improved observability and structured logging
