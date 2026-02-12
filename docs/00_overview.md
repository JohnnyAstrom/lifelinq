# LifeLinq – System Overview

LifeLinq is a **household-first life assistant**.

Its purpose is to reduce mental load by acting as a shared external memory for everyday life.
The system is designed around how real households function, not around idealized productivity workflows.

LifeLinq is **mobile-first**, designed to be carried with you, not managed from a desktop.

---

## What LifeLinq is

LifeLinq is a shared system for keeping track of things that otherwise live in people’s heads.

It focuses on four core domains:

### 1. Things to do / remember

Everyday items that need to happen or be remembered, with a **dual view**:
- a clear **all-items overview**
- a simple, legible **calendar view**

Examples:
- book a dentist appointment
- do the laundry
- return a package
- change water filter (Oct 1)

Characteristics:
- all items are simple rows
- no project hierarchy
- incomplete data is allowed
- recurring items are supported
- items may be scheduled on dates/times
- fast switching between list and calendar is core UX
- optional calendar sync (e.g. Google Calendar) for scheduled items
- assignment is coordination only; any household member may assign or clear it
- completion is distinct from deletion (done vs intentionally removed)

This domain exists to **offload memory**, not to manage projects.

---

### 2. Meals

Meals are handled separately because they:
- happen every day
- create automatic follow-up actions (shopping)
- cause disproportionate mental friction

Meal planning is a **core** use case:
- a clear **week view** is the primary planning surface
- day and month views exist for detail and overview
- history and future planning are supported via the same views (at least 3 months back)

Recipes can be attached to planned meals, and ingredients can be pushed to shopping lists.

Calendar sync for meals is optional and export-only (read-only); it is less critical than todo sync.

The primary flow is:

**plan meals → derive ingredients → update shopping list**

---

### 3. Shopping

Shopping lists are separate and must stay **simple, fast, and shared**.

The system prioritizes:
- shopping lists
- recurring staple items
- quick additions ("out of milk")

Shopping lists remain simple and fast, and can be created ad hoc.
Bought items stay in the same list (toggle back and forth).

---

### 4. Documents

A safety layer for items that are:
- expensive
- difficult to replace
- stressful to lose

Examples:
- contracts and agreements
- warranties and receipts
- subscriptions and bills

This domain prioritizes **retrievability over structure**.

### Documents roadmap

- **V0 (now)**: metadata-first records (title, notes, date, category, tags, external link). No file storage.
- **V0.5 (future)**: local attachments for the creator, visible to the household as "stored locally by X".
- **V1 (future)**: cloud attachments with compression, previews, and household sharing.

---

## Overview map

### User-facing navigation

- **Today/Overview**: today's todos, meals, and shopping needs in one place.
- **Todos**: list view + calendar view with fast switching.
- **Meals**: week view as primary, with day and month for detail/overview.
- **Shopping**: simple lists with items toggled between "to buy" and "bought".
- **Documents**: household archive for receipts, warranties, contracts, and bills.

### System intent

- Household-first data ownership.
- Backend-enforced scoping and rules.
- Bilingual by default (Swedish + English).

---

## What LifeLinq is not

LifeLinq is intentionally **not**:
- a project management tool
- a bookkeeping or accounting system
- a productivity or gamification app
- a task optimization engine

It does not aim to optimize people.
It aims to support them.

---

## Core design principles

- **Household-first**: all data belongs to a household, not an individual user
- **Low friction**: it must be easier to add something than to remember it
- **Tolerance for messiness**: real life is incomplete and inconsistent
- **Shared by default**: multiple people collaborate in the same household
- **Backend authority**: all business rules live server-side
- **Bilingual by default**: full support for Swedish and English from the start

---

## Technology overview

LifeLinq uses a simple, robust technology stack:

- **Backend**: Spring Boot (Java)
- **Frontend**: React Native (mobile-first)
- **Database**: PostgreSQL

Technology choices serve the domain, not the other way around.

---

## Scope and evolution

LifeLinq is designed to grow incrementally.

- New features must fit one of the existing domains or justify a new one
- Simplicity is preferred over early generalization
- Architecture decisions are documented explicitly

This document defines the intent of the system.
Detailed rules and constraints are defined in `INVARIANTS.md`.
The phased delivery plan lives in `docs/roadmap.md`.
