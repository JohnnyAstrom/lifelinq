# LifeLinq

LifeLinq is a **household‑first life assistant**.

It is designed to reduce mental load by acting as a shared external memory for everyday life — the kind of things that otherwise live in people’s heads and cause friction, stress, or forgetfulness.

LifeLinq is **mobile‑first**, collaborative by default, and built for long‑term use.

---

## What LifeLinq solves

LifeLinq focuses on three core areas of everyday household life:

### 1. Things to do / remember

Simple, non‑project tasks and reminders.

Examples:
- book a dentist appointment
- do the laundry
- return a package
- change water filter (Oct 1)

These are not projects.
They are memory offloading.
The todo experience is **list-first with a simple calendar view**, and switching between them should be effortless.
Scheduled items should be able to **sync with Google Calendar**.
Assignment is for coordination only; any household member can assign or clear it.

---

### 2. Meals & shopping

Meals are handled separately because they:
- happen every day
- create automatic follow‑up actions (shopping)
- cause disproportionate mental friction

The system prioritizes:
- shopping lists
- recurring staple items
- quick additions ("out of milk")

Meal planning is a core use case with a week view as the primary planning surface.
Recipes can feed ingredients into shopping lists.
Meals calendar sync is optional and export-only (read-only).

---

### 3. Documents

A safety layer for things that are:
- expensive
- hard to replace
- stressful to lose

Examples:
- contracts and warranties
- subscriptions and bills
- receipts and guarantees

The focus is on **retrievability**, not structure.

---

## Core principles

- **Household‑first**: all data belongs to a household, not an individual user
- **Low friction**: it must be easier to write something down than to remember it
- **Shared by default**: multiple people collaborate in the same household
- **Backend authority**: all business rules live on the server
- **Long‑term thinking**: architecture favors clarity and durability over shortcuts
- **Bilingual by default**: full support for Swedish and English from the start

---

## Architecture overview

LifeLinq is built using a strict **feature‑based architecture**.

Everything related to the same real‑world capability lives in the same feature folder — both on the backend and the frontend.

Authoritative documentation lives in `docs/`:

- `docs/INVARIANTS.md` – system laws (non‑negotiable rules)
- `docs/00-overview.md` – system purpose and scope
- `docs/architecture/` – backend & frontend structure
- `docs/decisions/` – architectural decision records (ADRs)

If something is unclear, **the docs are the source of truth**.

---

## Technology stack

- **Backend**: Spring Boot (Java)
- **Frontend**: React Native (mobile‑first)
- **Database**: PostgreSQL

The stack is intentionally conservative and well‑supported.

---

## Repository structure

```text
lifelinq/
  backend/        # Spring Boot application
  mobile/         # React Native app
  infra/          # Docker, database, deployment helpers
  docs/           # Architecture and system documentation
```

---

## Project status

LifeLinq is under active development.

The focus is currently on:
- establishing a solid domain model
- enforcing household‑first data ownership
- building a durable foundation for future features

---

## Guiding rule

If a change conflicts with the documented invariants or architecture, **the change is wrong**.

LifeLinq is designed to evolve — but never drift.
## Mobile dev setup

- Copy .env.example to .env.
- Adjust EXPO_PUBLIC_API_BASE_URL if the backend runs elsewhere.

## Dev auth flow (local)

1. Start backend in dev profile:
   - `mvn spring-boot:run "-Dspring-boot.run.profiles=dev"`
2. Start Expo:
   - `npm start` in `mobile-app/` and press `w` for web.
3. Log in with any email in the UI (dev-only):
   - The app calls `POST /auth/dev-login` and stores a JWT.
4. If `/me` returns `householdId: null`, create a household once.
5. Todos and other scoped endpoints require a household context.

