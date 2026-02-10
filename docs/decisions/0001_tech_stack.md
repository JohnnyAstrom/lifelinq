# Decision 0001: Technology Stack

## Status
Accepted

---

## Context

LifeLinq requires a stable, long-lived technology stack that supports:
- shared household data
- strong consistency
- mobile-first usage
- long-term maintainability

---

## Decision

The following technology stack is used:

- Backend: Spring Boot (Java)
- Frontend: React Native
- Database: PostgreSQL

---

## Rationale

### Spring Boot

- mature ecosystem
- strong security support
- well-suited for domain-driven design

### React Native

- mobile-first development
- shared code across platforms
- strong ecosystem

### PostgreSQL

- strong consistency guarantees
- relational model fits household ownership
- mature and well-supported

---

## Consequences

- Backend logic is centralized
- Frontend remains thin
- Data integrity is enforced at the database level

---

## Notes

Technology choices may evolve, but changes must preserve system invariants.

