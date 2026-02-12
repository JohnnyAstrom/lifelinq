# LifeLinq

LifeLinq is a **household‑first life assistant**.

It is designed to reduce mental load by acting as a shared external memory for everyday life — the kind of things that otherwise live in people’s heads and cause friction, stress, or forgetfulness.

LifeLinq is **mobile‑first**, collaborative by default, and built for long‑term use.

---

## What LifeLinq is

LifeLinq focuses on four core domains of everyday household life:

### 1. Things to do / remember

Simple, non‑project tasks and reminders with a **dual view**:
- a clear all‑items list
- a simple, legible calendar view

Completion is distinct from deletion, and scheduled items may sync with Google Calendar.

---

### 2. Meals

Meal planning is a **core** use case with week view as the primary planning surface.
Recipes can be attached to meals and ingredients can be pushed to shopping lists.
Calendar sync for meals is optional and export‑only.

---

### 3. Shopping

Shopping lists are **simple, fast, and shared**.
Bought items stay in the same list (toggle back and forth).

---

### 4. Documents

A safety layer for items that are expensive, hard to replace, or stressful to lose.

Documents roadmap:
- **V0 (now)**: metadata only (title, notes, date, category, tags, external link)
- **V0.5 (future)**: local attachments (visible to household as "stored locally by X")
- **V1 (future)**: cloud attachments with compression, previews, and sharing

---

## Core principles

- **Household‑first**: all data belongs to a household, not an individual user
- **Low friction**: it must be easier to write something down than to remember it
- **Tolerance for messiness**: real life is incomplete and inconsistent
- **Shared by default**: multiple people collaborate in the same household
- **Backend authority**: all business rules live on the server
- **Bilingual by default**: full support for Swedish and English from the start

---

## Architecture overview

LifeLinq is built using a strict **feature‑based architecture**.

Everything related to the same real‑world capability lives in the same feature folder — both on the backend and the frontend.

Authoritative documentation lives in `docs/`:

- `docs/00_overview.md` – system purpose and scope
- `docs/invariants.md` – system laws (non‑negotiable rules)
- `docs/roadmap.md` – phased delivery plan
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
  mobile-app/     # React Native app
  infra/          # Docker, database, deployment helpers
  docs/           # Architecture and system documentation
```

---

## Project status

LifeLinq is under active development.

The current focus is Phase 1 (see `docs/roadmap.md`):
- solid domain model and household scoping
- invitation flow and onboarding foundation
- durable feature boundaries for future growth

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

