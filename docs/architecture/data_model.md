# Conceptual Data Model

This document describes the **conceptual data model** of LifeLinq.

It focuses on relationships and ownership, not on database schemas or SQL.

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

## Summary

The data model reinforces LifeLinq’s household-first philosophy.

Technical persistence details are intentionally excluded.

