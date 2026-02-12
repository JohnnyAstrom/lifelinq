# Frontend Structure

This document defines the **feature‑based frontend structure** for LifeLinq.

LifeLinq is **mobile‑first**.
The frontend mirrors backend features to keep mental models aligned.

---

## Core principle

> Backend features and frontend features should map 1‑to‑1 whenever possible.

The frontend is organized by **what the user does**, not by technical concerns.

---

## High‑level layout

```text
mobile/
  src/
    app/
    features/
    screens/
    shared/
```

---

## Features

Each backend feature has a corresponding frontend feature folder:

```text
features/
  todo/
  shopping/
  documents/
  household/
  auth/
  meals/
```

Each feature may contain:

- components – render‑only UI
- hooks – data fetching and mutations
- api – feature‑specific API wrappers
- utils – feature‑local helpers

Features must not depend on each other directly.

---

## Screens

Screens are routing and composition only.

Screens:
- read navigation params
- compose feature components
- invoke hooks

Screens must not:
- contain business rules
- call APIs directly

---

## Shared layer

The shared layer contains reusable, non‑domain‑specific code:

- primitive UI components
- generic hooks
- API client setup
- utilities
- i18n setup and translation resources

Shared code must not encode feature or business meaning.

---

## State management

Global state is limited to:
- authentication
- active household

All other state is feature‑local.

--- 

## Internationalization (i18n)

LifeLinq ships with **full Swedish and English support** from the start.

Principles:
- All user‑facing text must come from translation files.
- Feature‑local strings live under the feature, shared UI strings live in shared i18n.
- Keys are stable and semantic (not raw English strings).
- Locale selection defaults to the device language, with a manual override in settings.

Suggested structure:
- `src/shared/i18n/` for base setup and shared strings
- `src/features/<feature>/i18n/` for feature‑local strings

---

## Business rule

All authoritative business rules live in the backend.

The frontend:
- sends intent
- renders state
- reacts to results

---

## Intent

This structure:
- keeps frontend and backend aligned
- scales with feature growth
- remains easy to reason about over time
