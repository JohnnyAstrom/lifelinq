# Decision 0002: Group-first Data Ownership

## Status
Accepted

---

## Context

LifeLinq is designed for shared everyday life.

Individual ownership of data does not reflect how groups operate.

---

## Decision

All domain data is owned by groups, not by individual users.

Users interact with data through group memberships.

---

## Rationale

- supports shared responsibility
- avoids data silos
- mirrors real-life collaboration

---

## Consequences

- all queries are group-scoped
- authorization is based on membership
- data persists independently of users

---

## Notes

This decision is foundational and reinforced by system invariants.

