# Backend Structure

This document defines the **feature-based package structure** for the LifeLinq backend.

The backend is organized around **features**, not technical layers.
All code related to the same real-world capability lives together.

This structure is intentionally aligned with the FlowLinq model and optimized for:
- long-term scalability
- clear ownership
- low cognitive load
- AI-assisted development (e.g. Codex)

---

## Core principle

> If you are working on a feature, you should only need to look inside **one feature folder**.

Features are the primary unit of organization.
Technical layers exist **inside** each feature, never across features.

---

## Base package

All backend code lives under the base package:

`app.lifelinq`

---

## Top-level layout

```text
app/lifelinq/
  config/            # global configuration (security, JWT, web)
  common/            # shared primitives only

  features/          # all business functionality
```

---

## Features

Each feature represents a real-world capability of the system.

Current core features:

- auth
- household
- todo
- shopping
- meals
- documents

New functionality must either:
- fit into an existing feature, or
- justify the creation of a new feature

---

## Canonical feature structure

Every feature follows the same internal structure:

```text
<feature>/
  api/              # HTTP controllers, request/response DTOs
  application/      # use cases and orchestration
  domain/           # domain models, rules, interfaces
  infrastructure/   # technical implementations (JPA, storage, messaging)
```

This structure is mandatory for all features.

---

## Domain layer (`domain`)

The domain layer:
- models real-world concepts
- defines business rules and invariants
- contains repository interfaces

Architectural note:
The domain layer may define repository interfaces as pure contracts.
Implementations belong to the infrastructure layer and are not part of the domain.

Rules:
- no Spring annotations
- no persistence annotations
- no HTTP concepts

This is the most stable part of the system.

---

## Application layer (`application`)

The application layer:
- implements use cases
- coordinates domain objects
- enforces workflows

It may:
- call multiple domain objects
- depend on domain interfaces

It must not:
- contain HTTP logic
- depend on infrastructure implementations

---

## Application use cases

Use cases live in `features/<feature>/application` and are plain classes without Spring annotations.
Inputs are simple command objects (passive data‑carriers) and validation happens inside the use case.
Use cases compose domain objects and return minimal results needed by callers.
Example: `CreateHouseholdUseCase` builds a `Household` and an owner `Membership`, then returns only `householdId`.
Tests are plain unit tests under `src/test/java` and do not require Spring or database configuration.

---

## API layer (`api`)

The API layer:
- exposes functionality via HTTP
- maps requests to use cases
- maps results to response DTOs

Controllers:
- must be thin
- contain no business rules
- must not make authorization decisions

---

## Infrastructure layer (`infrastructure`)

The infrastructure layer contains technical details:
- JPA repository implementations
- database mappings
- file and document storage
- notification adapters

Infrastructure:
- implements interfaces defined in `domain`
- is replaceable without affecting domain or application code

Wiring note (current pattern):
Manual wiring lives in `infrastructure` via a clearly named factory (e.g. `HouseholdInMemoryWiring`).
Persistence adapters implement ports and are not exposed through getters.
Application use cases depend only on ports and abstractions, never on adapter internals.
Wiring is a technical responsibility, separate from both domain and use cases.

---

## Cross-feature rules

- Features must not access another feature’s infrastructure
- Cross-feature interaction happens via:
  - application-level interfaces, or
  - domain events

Direct repository access across features is forbidden.

---

## Shared code (`common`)

The `common` package is intentionally small.

Allowed contents:
- error types
- identifiers
- time utilities

Business logic must never live in `common`.

---

## Intent

This structure enforces separation of concerns through **placement**, not convention.

If the structure feels strict or verbose, it is doing its job.
