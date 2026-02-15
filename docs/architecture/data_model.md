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
- assignment is coordination only, not ownership
- any household member may reassign or clear assignment
- optional due time (scheduled items)
- optional calendar sync reference (external event id + provider)

---

### ShoppingItem

- represents a needed or planned purchase
- may be recurring
- may be marked as acquired

### MealPlan

- weekly planning surface for meals
- composed of day entries within a household
- supports history and forward planning (via week navigation)

### MealEntry

- planned meal on a specific date (e.g. lunch/dinner)
- may reference a Recipe

### Recipe

- named recipe with a list of ingredients
- ingredients can be pushed to ShoppingList

### Ingredient

- name + optional quantity/unit
- used by Recipe

### ShoppingList

- a named list within a household
- can be created ad hoc
- holds ShoppingItems derived from recipes or manual input

---

### DocumentItem

- represents a document, receipt, or record
- optimized for retrieval
- metadata-first in V0
- local attachments in V0.5 (creator-only storage)
- cloud attachments in V1 (household shared)

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

## Long-term Core Schema (Guiding)

This section describes a **long-term, stable core** for each domain object.
It is a guiding target, not a strict implementation requirement.

### Household

- `id` (core)
- `name` (core)
- `createdAt` (core)
- `updatedAt` (later)

### Membership

- `householdId` (core)
- `userId` (core)
- `role` (core)
- `createdAt` (core)

### Invitation

- `id` (core)
- `householdId` (core)
- `inviteeEmail` (core)
- `token` (core)
- `expiresAt` (core)
- `status` (core)
- `createdAt` (core)

### TodoItem

- `id` (core)
- `householdId` (core)
- `text` (core)
- `status` (core)
- `createdAt` (core)
- `completedAt` (later)
- `deletedAt` (core)
- `dueDate` (core)
- `dueTime` (core)
- `recurrenceRule` (later)
- `assignedToUserId` (later)
- `createdByUserId` (later)
- `calendarSyncId` (later)
- `notes` (optional)
- `tags` (optional)

### ShoppingList

- `id` (core)
- `householdId` (core)
- `name` (core)
- `createdAt` (core)

### ShoppingItem

- `id` (core)
- `shoppingListId` (core)
- `name` (core)
- `status` (core)
- `createdAt` (core)
- `boughtAt` (later)
- `quantity` (optional)
- `unit` (optional)
- `source` (optional)

### MealPlan

- `id` (core)
- `householdId` (core)
- `weekStart` (core)
- `createdAt` (core)

### MealEntry

- `id` (core)
- `mealPlanId` (core)
- `date` (core)
- `slot` (core)
- `recipeId` (later)
- `notes` (optional)

### Recipe

- `id` (core)
- `householdId` (core)
- `name` (core)
- `createdAt` (core)
- `instructions` (later)
- `sourceUrl` (optional)

### Ingredient

- `id` (core)
- `recipeId` (core)
- `name` (core)
- `quantity` (optional)
- `unit` (optional)

### DocumentItem

- `id` (core)
- `householdId` (core)
- `title` (core)
- `createdAt` (core)
- `notes` (later)
- `date` (later)
- `category` (later)
- `tags` (optional)
- `externalLink` (core for V0)
- `attachmentRef` (later, V0.5/V1)

---

## Persistence Snapshot (Current)

This is a structural overview of tables and relations that are implemented today.

### Tables

- `users`: `id`
- `households`: `id`, `name`
- `todos`: `id`, `household_id`, `text`, `status`, `due_date`, `due_time`, `deleted_at`
- `documents`: `id`, `household_id`, `created_by_user_id`, `title`, `notes`, `date`, `category`, `external_link`, `created_at`
- `document_tags`: `document_id`, `tag`
- `shopping_lists`: `id`, `household_id`, `name`, `created_at`
- `shopping_items`: `id`, `list_id`, `name`, `status`, `quantity`, `unit`, `created_at`, `bought_at`
- `memberships`: composite key (`household_id`, `user_id`), `role`
- `invitations`: `id`, `household_id`, `invitee_email`, `token`, `expires_at`, `status`

### Relations (ID‑based)

- `todos.household_id` → `households.id`
- `documents.household_id` → `households.id`
- `document_tags.document_id` → `documents.id`
- `shopping_lists.household_id` → `households.id`
- `shopping_items.list_id` → `shopping_lists.id`
- `memberships.household_id` → `households.id`
- `invitations.household_id` → `households.id`

### ASCII Sketch

```text
households (id)
  ↑            ↑            ↑
  │            │            │
todos          memberships  invitations
(household_id) (household_id, user_id) (household_id)

shopping_lists (id, household_id)
  ↑
  │
shopping_items (list_id)
```

### Constraints

- `memberships` primary key is (`household_id`, `user_id`)
- `invitations.token` is unique
- `invitations.status` is `ACTIVE` or `REVOKED`; expired is derived from `expires_at`

### Aggregate boundaries

- `household` is the aggregate root for `memberships` and `invitations`
- `todo` is its own aggregate, bound to `household_id`
- `shopping_list` is the aggregate root for `shopping_items`

---

## Persistence Snapshot (Planned)

This is a target structure based on the current architecture plan.

### Tables (planned)

- `meal_plans`: `id`, `household_id`, `week_start`
- `meal_entries`: `id`, `meal_plan_id`, `date`, `slot`, `recipe_id`
- `recipes`: `id`, `household_id`, `name`
- `recipe_ingredients`: `id`, `recipe_id`, `name`, `quantity`, `unit`
- `shopping_lists`: `id`, `household_id`, `name`
- `shopping_items`: `id`, `shopping_list_id`, `name`, `status`
- `document_items`: `id`, `household_id`, `title`, `notes`, `category`, `tags`, `external_link`

### Relations (planned, ID‑based)

- `meal_plans.household_id` → `households.id`
- `meal_entries.meal_plan_id` → `meal_plans.id`
- `recipes.household_id` → `households.id`
- `recipe_ingredients.recipe_id` → `recipes.id`
- `shopping_lists.household_id` → `households.id`
- `shopping_items.shopping_list_id` → `shopping_lists.id`
- `document_items.household_id` → `households.id`

---

## Summary

The data model reinforces LifeLinq’s household-first philosophy.

Technical persistence details are intentionally excluded.
