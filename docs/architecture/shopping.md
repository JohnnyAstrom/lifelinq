# Shopping

This document defines the detailed intent and behavior for the Shopping feature.

## Purpose

Shopping lists must be **simple, fast, and shared**.
They should accept both manual input and items derived from recipes.
Shopping is a separate feature from Meals, with an explicit integration point.

## Core behavior

- Multiple shopping lists are supported.
- Lists can be removed entirely.
- Manual add must be fast and low-friction.
- Items can be marked as acquired.
- Items can be generated from Meals/Recipes.
- Items can be edited (name, optional quantity/unit).
- Marking acquired moves the item to a "bought" section within the same list.
- Items can be toggled back to "to buy" with a single action.

## Data shape (conceptual)

- `ShoppingList`: `name`, `householdId`
- `ShoppingItem`: `name`, `status`, `shoppingListId`, `quantity` (optional), `unit` (optional)

## Decisions

Decision: Household → ShoppingLists → ShoppingItems.
Rationale: Supports multiple lists while keeping ownership clear and bounded.
Consequences: Every item belongs to exactly one list.

Decision: ShoppingList is the aggregate root.
Rationale: List-level invariants (toggle, ordering, bulk add) belong to the list.
Consequences: Toggling is a state mutation within the list, not a cross-aggregate event.

Decision: API contracts are list-centric.
Rationale: Frontend needs stable listId boundaries for 1:1 feature mapping.
Consequences: Endpoints accept `listId` for list and item mutations.

## Shopping Model (Phase 1)

### Aggregate root

- `ShoppingList` is the aggregate root.
- `ShoppingItem` belongs to exactly one list.
- No cross-list moves in V0 (to "move", create a new item in the target list).

### Item lifecycle

- Item is created in "to buy".
- Item can be toggled to "bought".
- Item can be toggled back to "to buy".
- Item can be removed entirely.

### Allowed state transitions

- `to_buy` → `bought`
- `bought` → `to_buy`

### Identity and uniqueness

- Item name must be unique within a list (case-insensitive, normalized).
- Name is normalized in ApplicationService before uniqueness checks.

### Household scoping

- List is household-scoped.
- Item inherits household through its list.
- All mutations must verify list belongs to current household.

### Invariants

- Item name is required and non-blank.
- List must exist before adding or mutating items.
- Item must exist before toggling or removing.
- Item name must be unique within its list (case-insensitive, normalized).
- Item must belong to the list being mutated.
- Toggle is only allowed within the owning list.
- List and item household scope must match the request context.
- Quantity and unit are optional, but when one is present the other must be present.

### Status representation

- Domain uses enum: `TO_BUY` | `BOUGHT`.
- Persistence may use boolean for V0.

### Toggle policy

- `boughtAt` is set when status becomes `BOUGHT`.
- `boughtAt` is cleared when toggled back to `TO_BUY`.

### Non-goals (V0)

- No moving items between lists.
- No multi-state lifecycle (archived/cancelled) beyond `to_buy`/`bought`.
- No per-user personal shopping lists.
- No historical retention of removed items (hard delete).

## UX principles

- Shared by default (household list, not personal).
- Avoid duplication where possible.
- Optimize for quick in-store use.

## API (Current)

- `DELETE /shopping-lists/{listId}` removes one household-scoped shopping list.
- Returns `204` on successful delete.
- Returns `404` when the list does not exist in the current household scope.
- `PATCH /shopping-lists/{listId}` updates the list name.
- Returns `200` with the updated list payload.
