# Decision 0002: Household-first Data Ownership

## Status
Accepted

---

## Context

LifeLinq is designed for shared everyday life.

Individual ownership of data does not reflect how households operate.

---

## Decision

All domain data is owned by households, not by individual users.

Users interact with data through household memberships.

---

## Rationale

- supports shared responsibility
- avoids data silos
- mirrors real-life collaboration

---

## Consequences

- all queries are household-scoped
- authorization is based on membership
- data persists independently of users

---

## Notes

This decision is foundational and reinforced by system invariants.

