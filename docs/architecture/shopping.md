# Shopping

This document defines the detailed intent and behavior for the Shopping feature.

## Purpose

Shopping lists must be **simple, fast, and shared**.
They should accept both manual input and items derived from recipes.
Shopping is a separate feature from Meals, with an explicit integration point.

## Core behavior

- Multiple shopping lists are supported.
- Manual add must be fast and low-friction.
- Items can be marked as acquired.
- Items can be generated from Meals/Recipes.
- Marking acquired moves the item to a "bought" section within the same list.
- Items can be toggled back to "to buy" with a single action.

## Data shape (conceptual)

- `ShoppingList`: `name`, `householdId`
- `ShoppingItem`: `name`, `status`, `shoppingListId`

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

## UX principles

- Shared by default (household list, not personal).
- Avoid duplication where possible.
- Optimize for quick in-store use.
