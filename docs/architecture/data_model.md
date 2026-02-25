# Conceptual Data Model

This document describes the **conceptual data model** of LifeLinq.

It focuses on relationships and ownership, with a compact persistence snapshot
to reflect the current storage structure.

---

## Core entities

### User

Represents a real person.

- authenticated via external provider
- may belong to multiple groups
- does not own domain data

---

### Group

Represents a shared context.

- owns all domain data
- exists independently of users
- persists even if users leave

---

### Membership

Connects a user to a group.

- defines role
- defines participation
- does not own data

---

## Domain entities

All domain entities belong to a group.

### TodoItem

- simple, flat item
- explicit planning scope (`DAY`, `WEEK`, `MONTH`, `LATER`)
- optional due date/time (DAY scope only)
- optional week target (scopeYear + ISO week)
- optional month target (scopeYear + month)
- optional recurrence
- optional assignee
- assignment is coordination only, not ownership
- any group member may reassign or clear assignment
- `createdAt` (required)
- `completedAt` (optional)
- optional calendar sync reference (external event id + provider)

---

### ShoppingItem

- represents a needed or planned purchase
- may be recurring
- may be marked as acquired

### MealPlan

- weekly planning surface for meals
- composed of day entries within a group
- supports history and forward planning (via week navigation)

### MealEntry

- planned meal on a specific date (e.g. lunch/dinner)
- may reference a Recipe

### Recipe

- named recipe with a list of ingredients
- ingredients can be pushed to ShoppingList
- In V0.5c, recipes serve primarily as a shopping-generator: ingredients listed on a recipe are used to push shopping items. Full culinary modeling (instructions, nutrition, comprehensive ingredient sets) is out of scope.

### Ingredient

- name + optional quantity/unit
- used by Recipe

### ShoppingList

- a named list within a group
- can be created ad hoc
- holds ShoppingItems derived from recipes or manual input

---

### DocumentItem

- represents a document, receipt, or record
- optimized for retrieval
- metadata-first in V0
- local attachments in V0.5 (creator-only storage)
- cloud attachments in V1 (group shared)

---

## Ownership rules

- Users never own domain entities
- Groups own all domain entities
- Membership controls access, not ownership

---

## Relationships

- User ↔ Group: many-to-many via Membership
- Group → Domain entities: one-to-many

---

## Design principles

- model reality, not databases
- allow incomplete data
- prioritize simplicity

---

## Long-term Core Schema (Guiding)

This section describes a **long-term, stable core** for each domain object.
It is a guiding target, not a strict implementation requirement.

### Group

- `id` (core)
- `name` (core)
- `createdAt` (core)
- `updatedAt` (later)

### Membership

- `groupId` (core)
- `userId` (core)
- `role` (core)
- `createdAt` (core)

### Invitation

- `id` (core)
- `groupId` (core)
- `inviteeEmail` (core)
- `token` (core)
- `expiresAt` (core)
- `status` (core)
- `createdAt` (core)

### TodoItem

- `id` (core)
- `groupId` (core)
- `text` (core)
- `status` (core)
- `createdAt` (core)
- `completedAt` (core)
- `deletedAt` (core)
- `scope` (core)
- `dueDate` (core for DAY)
- `dueTime` (core for DAY)
- `scopeYear` (core for WEEK/MONTH)
- `scopeWeek` (core for WEEK)
- `scopeMonth` (core for MONTH)
- `recurrenceRule` (later)
- `assignedToUserId` (later)
- `createdByUserId` (later)
- `calendarSyncId` (later)
- `notes` (optional)
- `tags` (optional)

### ShoppingList

- `id` (core)
- `groupId` (core)
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
- `groupId` (core)
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
- `groupId` (core)
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
- `groupId` (core)
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
- `groups`: `id`, `name`
- `todos`: `id`, `group_id`, `text`, `status`, `scope`, `due_date`, `due_time`, `scope_year`, `scope_week`, `scope_month`, `created_at`, `completed_at`, `deleted_at`
- `documents`: `id`, `group_id`, `created_by_user_id`, `title`, `notes`, `date`, `category`, `external_link`, `created_at`
- `document_tags`: `document_id`, `tag`
- `week_plans`: `id`, `group_id`, `week_year`, `iso_week`, `created_at`
- `planned_meals`: composite key (`week_plan_id`, `day_of_week`, `meal_type`), `recipe_id`
- `recipes`: `id`, `group_id`, `name`, `created_at`
- `recipe_ingredients`: `id`, `recipe_id`, `name`, `quantity`, `unit`, `position`
- `shopping_lists`: `id`, `group_id`, `name`, `created_at`
- `shopping_items`: `id`, `list_id`, `name`, `status`, `quantity`, `unit`, `created_at`, `bought_at`
- `memberships`: composite key (`group_id`, `user_id`), `role`
- `invitations`: `id`, `group_id`, `invitee_email`, `token`, `expires_at`, `status`

### Relations (ID‑based)

- `todos.group_id` → `groups.id`
- `documents.group_id` → `groups.id`
- `document_tags.document_id` → `documents.id`
- `week_plans.group_id` → `groups.id`
- `planned_meals.week_plan_id` → `week_plans.id`
- `planned_meals.recipe_id` → `recipes.id`
- `recipes.group_id` → `groups.id`
- `recipe_ingredients.recipe_id` → `recipes.id`
- `shopping_lists.group_id` → `groups.id`
- `shopping_items.list_id` → `shopping_lists.id`
- `memberships.group_id` → `groups.id`
- `invitations.group_id` → `groups.id`

### ASCII Sketch

```text
groups (id)
  ↑            ↑            ↑             ↑             ↑
  │            │            │             │             │
todos          memberships  invitations   week_plans    recipes
(group_id) (group_id, user_id) (group_id) (group_id) (group_id)

week_plans (id)            recipes (id)
  ↑                         ↑
  │                         │
planned_meals               recipe_ingredients
(week_plan_id, recipe_id)   (recipe_id)

shopping_lists (id, group_id)
  ↑
  │
shopping_items (list_id)
```

### Constraints

- `memberships` primary key is (`group_id`, `user_id`)
- `invitations.token` is unique
- `invitations.status` is `ACTIVE` or `REVOKED`; expired is derived from `expires_at`

### Aggregate boundaries

- `group` is the aggregate root for `memberships` and `invitations`
- `todo` is its own aggregate, bound to `group_id`
- `shopping_list` is the aggregate root for `shopping_items`

---

## Persistence Snapshot (Planned)

This is a target structure based on the current architecture plan.

### Tables (planned)

- `meal_plans`: `id`, `group_id`, `week_start`
- `meal_entries`: `id`, `meal_plan_id`, `date`, `slot`, `recipe_id`
- `recipes`: `id`, `group_id`, `name`
- `recipe_ingredients`: `id`, `recipe_id`, `name`, `quantity`, `unit`
- `shopping_lists`: `id`, `group_id`, `name`
- `shopping_items`: `id`, `shopping_list_id`, `name`, `status`
- `document_items`: `id`, `group_id`, `title`, `notes`, `category`, `tags`, `external_link`

### Relations (planned, ID‑based)

- `meal_plans.group_id` → `groups.id`
- `meal_entries.meal_plan_id` → `meal_plans.id`
- `recipes.group_id` → `groups.id`
- `recipe_ingredients.recipe_id` → `recipes.id`
- `shopping_lists.group_id` → `groups.id`
- `shopping_items.shopping_list_id` → `shopping_lists.id`
- `document_items.group_id` → `groups.id`

---

## Summary

The data model reinforces LifeLinq’s group-first philosophy.

Technical persistence details are intentionally excluded.
