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
  important/
  household/
  auth/
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

Shared code must not encode feature or business meaning.

---

## State management

Global state is limited to:
- authentication
- active household

All other state is feature‑local.

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

