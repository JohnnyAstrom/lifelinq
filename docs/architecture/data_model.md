# Conceptual Data Model

This document describes the **conceptual data model** of LifeLinq.

It focuses on relationships and ownership, with a compact persistence snapshot
to reflect the current storage structure.

---

## Core entities

### User

Represents a real person.

- authenticated via external provider
- may belong to multiple households
- does not own domain data

---

### Household

Represents a shared context.

- owns all domain data
- exists independently of users
- persists even if users leave

---

### Membership

Connects a user to a household.

- defines role
- defines participation
- does not own data

---

## Domain entities

All domain entities belong to a household.

### TodoItem

- simple, flat item
- optional due date
- optional recurrence
- optional assignee

---

### ShoppingItem

- represents a needed or planned purchase
- may be recurring
- may be marked as acquired

---

### ImportantItem

- represents a document, record, or reference
- optimized for retrieval
- may include attachments and metadata

---

## Ownership rules

- Users never own domain entities
- Households own all domain entities
- Membership controls access, not ownership

---

## Relationships

- User ↔ Household: many-to-many via Membership
- Household → Domain entities: one-to-many

---

## Design principles

- model reality, not databases
- allow incomplete data
- prioritize simplicity

---

## Persistence Snapshot (Current)

This is a structural overview of tables and relations (no SQL, no code).

### Tables

- `households`: `id`, `name`
- `todos`: `id`, `household_id`, `text`, `status`
- `memberships`: composite key (`household_id`, `user_id`), `role`
- `invitations`: `id`, `household_id`, `invitee_email`, `token`, `expires_at`, `status`

### Relations (ID‑based)

- `todos.household_id` → `households.id`
- `memberships.household_id` → `households.id`
- `invitations.household_id` → `households.id`

### ASCII Sketch

```text
households (id)
  ↑            ↑            ↑
  │            │            │
todos          memberships  invitations
(household_id) (household_id, user_id) (household_id)
```

### Constraints

- `memberships` primary key is (`household_id`, `user_id`)
- `invitations.token` is unique

### Aggregate boundaries

- `household` is the aggregate root for `memberships` and `invitations`
- `todo` is its own aggregate, bound to `household_id`

---

## Summary

The data model reinforces LifeLinq’s household-first philosophy.

Technical persistence details are intentionally excluded.
