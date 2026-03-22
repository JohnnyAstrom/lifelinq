# LifeLinq

LifeLinq is a mobile-first app for shared everyday coordination.

It helps people keep track of the things that otherwise live in their heads:
- things to do
- meals
- shopping
- important household documents

The product is built for shared use, but the experience should always feel calm, personal, and easy to start with.

---

## What LifeLinq is

LifeLinq is designed to reduce everyday mental load.

It acts as a shared external memory for home and household life:
- what needs to be done
- what we are eating
- what needs to be bought
- what is important to keep

It is not a project-management tool or a productivity system.
It is a practical coordination product for real life.

---

## Core product areas

### Todos

Simple everyday items with three time views:
- Daily
- Weekly
- Monthly

Todos are for remembering and coordinating, not managing projects.

### Meals

Meal planning with week view as the primary surface.

Recipes support planning, reuse, and ingredient-aware follow-up into shopping.

### Shopping

Simple shared shopping lists.

Lists are fast to update, easy to reuse, and designed for real household use.

### Documents

A lightweight household archive for things that are stressful to lose or hard to replace.

The current product is metadata-first, with attachment evolution planned later.

---

## Product direction

LifeLinq should feel:
- calm
- human
- low-friction
- content-first
- household-close

It should not feel:
- enterprise-like
- admin-heavy
- dashboard-first
- system-explanatory

The system architecture may be group-first, but the product experience should feel simple and natural to use.

---

## Current internal references

The strongest current product references in the app are:
- Meals Plan week view
- saved recipe read mode
- Recipes overview

These currently represent the clearest direction for workspace hierarchy, calm structure, and content-first UX.

---

## Architecture

LifeLinq uses a feature-based architecture across backend and frontend.

Core rules:
- all business rules are enforced server-side
- clients are untrusted
- domain data belongs to a group context
- frontend features stay thin and feature-owned

Authoritative documentation lives in `docs/`:

- `docs/00_overview.md`
- `docs/invariants.md`
- `docs/roadmap.md`
- `docs/architecture/`
- `docs/design/`
- `docs/decisions/`

If code and docs disagree, the docs must be updated or the change is wrong.

---

## Design guidance

Frontend and product work is guided by the design document set in `docs/design/`:

- `docs/design/ux-principles.md`
- `docs/design/ui-foundation.md`
- `docs/design/ui-reference-framework.md`

These define:
- product experience principles
- visual UI rules
- practical review and implementation guidance

---

## Workflow guidance

AI-assisted product and implementation workflow guidance lives in:

- `docs/workflow/ai-collaboration.md`

---

## Technology stack

- Backend: Spring Boot (Java)
- Frontend: React Native
- Database: PostgreSQL

---

## Repository structure

```text
lifelinq/
  backend/
  mobile-app/
  infra/
  docs/
```

---

## Current project status

LifeLinq is under active development.

The current phase is focused on product maturity and platform hardening:
- stable architecture boundaries
- stronger feature UX
- improved workspace, detail, and editor quality
- continued backend correctness and import quality work

See `docs/roadmap.md` for the longer-term direction.

---

## Local development

### Mobile app

- Copy `.env.example` to `.env`
- Adjust `EXPO_PUBLIC_API_BASE_URL` if needed

### Dev auth flow

1. Start backend in dev profile.
2. Start Expo in `mobile-app/`.
3. Log in with a dev email.
4. Ensure group or place context exists before using scoped features.

### PostgreSQL

Use the PostgreSQL Docker compose setup in `infra/` and run the backend with the documented profiles when needed.

---

## Guiding rule

LifeLinq should evolve without drifting.

If a change conflicts with documented invariants, architecture, or design principles, the change is wrong.
