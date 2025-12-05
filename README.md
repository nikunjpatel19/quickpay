# QuickPay • Android and Backend Monorepo

Full stack payment flow demo inspired by Finix and Stripe concepts.  
The repository contains the Android client and the Ktor backend that work together to simulate a merchant payment process.

```
quickpay/
 ├── android/        ← Android app (Jetpack Compose, Kotlin, MVVM)
 ├── backend/        ← Ktor backend server (Kotlin)
 └── README.md
```

---

## Tech Stack

### Android
- Kotlin  
- Jetpack Compose (Material 3)  
- MVVM with StateFlow and ViewModel  
- Retrofit with Moshi  
- Navigation Component  
- Custom Tabs checkout launch  
- Basic unit tests and instrumentation tests  

### Backend
- Kotlin  
- Ktor server  
- Exposed ORM with Postgres (in progress)  
- REST endpoints for payment link creation and order status  
- Webhook style simulation for success and failure callbacks  

---

## Features

| Flow Step | Description |
|----------|-------------|
| Create Payment Link | User enters amount, currency and description and the backend returns a hosted link. |
| Checkout URL | The link opens a hosted checkout page in a browser tab or Custom Tab. |
| Status Polling | The Android app polls the backend until the order becomes succeeded or failed. |
| Developer Webhook Simulation | Backend exposes endpoints that simulate webhook updates to test the full payment lifecycle. |

---

## Running the Backend

```bash
cd backend
./gradlew run
```

Backend will respond at:  
`http://127.0.0.1:8080`

Webhook simulation example:

```bash
curl -X POST "http://127.0.0.1:8080/v1/dev/orders/{id}/mark?status=succeeded"
```

---

## Running the Android App

The Android emulator uses the special host mapping:

```
BASE_URL = http://10.0.2.2:8080/
```

Open the `android` folder in Android Studio and run the app.

---

## Repository Workflow

Single Git repository at the root. Android and backend can be opened in separate IDE windows.

| IDE | Path | Notes |
|-----|------|-------|
| Android Studio | quickpay/android | Android module |
| IntelliJ IDEA | quickpay/backend | Ktor backend |

Commit examples:

```bash
git add android/
git commit -m "android: add create link screen and polling logic"

git add backend/
git commit -m "backend: create link and order status endpoints"
```

---

## API Endpoints

| Method | Path | Purpose |
|--------|------|----------|
| POST | /v1/links | Create a payment link |
| GET | /v1/orders/{id} | Get payment order status |
| POST | /v1/dev/orders/{id}/mark?status=xxx | Developer tool for success or failure |

Sample request:

```json
{
  "amountCents": 999,
  "currency": "USD",
  "description": "Coffee"
}
```

---

## Roadmap

- Finix Checkout Pages integration  
- Complete webhook handling for state transitions  
- Merchant dashboard and analytics  
- Postgres storage for orders and webhooks  
- Google Pay integration  
- QR code sharing for hosted checkout  
