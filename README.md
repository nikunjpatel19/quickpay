# QuickPay — Android + Backend Monorepo

Full stack demo payment flow app inspired by Finix / Stripe concepts.

This monorepo contains **both** the Android client and Ktor backend server.

```
quickpay/
 ├── android/        ← Android app (Jetpack Compose / Kotlin)
 ├── backend/        ← Ktor backend server
 └── README.md
```

---

## Tech Stack

### Android
- Kotlin
- Jetpack Compose
- MVVM (StateFlow + ViewModel)
- Retrofit + Moshi
- Material 3 UI
- Custom Tabs checkout launch

### Backend
- Kotlin
- Ktor server
- In-memory order store (mock simulation)
- REST endpoints for link creation + order status polling

---

## Features

| Flow Step | Description |
|----------|-------------|
| Create Payment Link | Enter amount (in cents), currency + description |
| Launch Checkout URL | Opens mock checkout page (CustomTab) |
| Poll Status | App auto polls backend until order `succeeded` or `failed` |
| Dev Mode Webhook Simulation | Backend endpoint to manually mark success/failed |

---

## Running Backend Locally

```bash
cd backend
./gradlew run
```
Backend responds at: `http://127.0.0.1:8080`

### Dev Test (simulate payment result)
```bash
curl -X POST "http://127.0.0.1:8080/v1/dev/orders/{id}/mark?status=succeeded"
```

---

## Running Android App

Android emulator connects to host using:
```
BASE_URL = http://10.0.2.2:8080/
```
Open `/android` in Android Studio → Run app.

---

## Repo / Monorepo Workflow

Single Git repository at **root**. You can open Android + Backend in separate IDE windows:

| IDE | Open path | Notes |
|-----|-----------|-------|
| Android Studio | `quickpay/android` | normal Android project |
| IntelliJ IDEA  | `quickpay/backend` | normal Ktor project |

Both IDEs commit/push to the **same** root repo (ensure only one `.git/` at the root).

### Example commits
```bash
# Android only
git add android/
git commit -m "android: UI + create link flow"
git push

# Backend only
git add backend/
git commit -m "backend: /v1/links + order polling"
git push
```

---

## API Endpoints

| Method | Path | Purpose |
|--------|------|----------|
| POST | /v1/links | Create payment link |
| GET | /v1/orders/{id} | Get payment order status |
| POST | /v1/dev/orders/{id}/mark?status=xxx | Dev tool to mark succeeded / failed |

Sample JSON body:
```json
{
  "amountCents": 999,
  "currency": "USD",
  "description": "Coffee"
}
```

---

## Next Roadmap

- Web checkout UI for real simulation
- Finix/Stripe real integration
- Saving history & analytics
- Auth + Merchant management

---
