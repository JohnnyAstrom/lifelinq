# LifeLinq – System Overview

LifeLinq is a **household-first life assistant**.

Its purpose is to reduce mental load by acting as a shared external memory for everyday life.
The system is designed around how real households function, not around idealized productivity workflows.

LifeLinq is **mobile-first**, designed to be carried with you, not managed from a desktop.

---

## What LifeLinq is

LifeLinq is a shared system for keeping track of things that otherwise live in people’s heads.

It focuses on three core domains:

### 1. Things to do / remember

Simple, everyday items that need to happen or be remembered.

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

This domain exists to **offload memory**, not to manage projects.

---

### 2. Food & shopping

Food is handled separately because it:
- happens every day
- creates automatic follow-up actions (shopping)
- causes disproportionate mental friction

The system prioritizes:
- shopping lists
- recurring staple items
- quick additions ("out of milk")

Meal planning and recipes are optional and secondary.
The primary flow is:

**what do we need → what should be bought**

---

### 3. Important things

A safety layer for items that are:
- expensive
- difficult to replace
- stressful to lose

Examples:
- contracts and agreements
- warranties and receipts
- subscriptions and bills
- important contacts

This domain prioritizes **retrievability over structure**.

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

