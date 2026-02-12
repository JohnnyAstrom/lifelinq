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

## UX principles

- Shared by default (household list, not personal).
- Avoid duplication where possible.
- Optimize for quick in-store use.
