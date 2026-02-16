# Meals

This document defines the detailed intent and behavior for the Meals feature.

## Purpose

Meals is a **meal planning** surface for the household.
It exists to turn planned meals into actionable shopping items.
Meals is a separate feature from Shopping, with an explicit integration point.
In V0.5c, recipes serve primarily as a shopping-generator: ingredients listed on a recipe are used to push shopping items. Full culinary modeling (instructions, nutrition, comprehensive ingredient sets) is out of scope.

## Decisions

Decision: Meals integrates with Shopping via application-level command orchestration.
Rationale: Preserves feature isolation and avoids cross-feature repository access.
Consequences: Meals never accesses Shopping repositories; it calls a Shopping use case.
Integration is one-way (Meals → Shopping).
Current implementation: Meals calls `ShoppingApplicationService.addShoppingItem(...)`
when `targetShoppingListId` is provided, once per ingredient occurrence.

## Meals Model (Phase 1)

### Aggregate root

- `WeekPlan` is the aggregate root.
- Primary identity: UUID id.
- Unique constraint: (householdId, year, isoWeek).
- `createdAt` is stored on `WeekPlan` and set via application Clock.

### Scope

- Household-scoped.
- One `WeekPlan` per household per ISO week.

### Entities / value objects

- `WeekPlan`
  - `id`, `householdId`, `year`, `isoWeek`, `createdAt`, `meals`
- `PlannedMeal`
  - `dayOfWeek` (1–7, ISO)
  - `mealType` (BREAKFAST, LUNCH, DINNER)
  - `recipeId` (UUID reference)
  - Identity within `WeekPlan` is `(dayOfWeek, mealType)` (no separate UUID in V0).
  - Inherits household scope from `WeekPlan` (no householdId on `PlannedMeal`).
- `Recipe`
  - `id`, `householdId`, `name`, `createdAt`, `ingredients`
- `Ingredient`
  - `id`, `name`, `quantity` (BigDecimal, nullable), `unit` (ShoppingUnit, nullable), `position`

### Invariants

- `dayOfWeek` must be 1–7.
- Max 1 planned meal per day + meal type within a week.
- Adding a meal for an existing day + type replaces the current meal.
- `WeekPlan` unique per household + ISO week.
- `isoWeek` must be valid for the given year (validated in application layer).
- No cross-week move in V0 (move = delete + re-add).
- Household scope is enforced at application layer.
- `Ingredient.position` must be unique within a recipe.
- `Ingredient.name` must be non-blank.

### Creation policy

- `WeekPlan` is created implicitly if missing when adding a meal.

### Integration rule

- Meals → Shopping only via application-level command.
- Shopping has no dependency on Meals.
- Ingredient push uses deterministic ordering: `position ASC`, then `ingredientId ASC`.
- Ingredient name normalization before push is:
  - trim
  - collapse internal whitespace runs to a single space
  - lowercase using `Locale.ROOT`
- Duplicate ingredient names are not merged in Meals; one shopping item call is made per occurrence.

### Non-goals (V0)

- No recurring meals.
- No nutrition tracking.
- No full culinary recipe modeling (instructions, nutrition, comprehensive ingredient sets).
- No advanced shopping merge logic in V0.5c (one item per ingredient occurrence).
- No calendar sync.

## Core views

- **Week view** is the primary planning surface.
- **Day view** for quick edits and detail.
- **Month view** for overview and navigation.

## Planning horizon

- Support both **future planning** and **history** in the same views.
- History should go at least **3 months back**.
- Copying a week forward should be easy (e.g. "use last week").

## Recipes

- Meals reference recipes via `recipeId` only.
- Recipe storage and ingredient modeling are implemented in V0.5c.
- API responses may include derived recipe name via runtime lookup; no title snapshot is stored on planned meals.
- Ingredients are pushed to a Shopping List via command orchestration.

## Calendar sync

- Meals calendar sync is optional and **export-only** (read-only).
- It is less critical than Todo sync.

## UX principles

- Keep the planning flow light and fast.
- Meal planning must not feel like a project tool.
- The value is in turning plans into shopping needs.

## API (V0)

Endpoint: `POST /meals/weeks/{year}/{isoWeek}/days/{dayOfWeek}/meals/{mealType}`  
Purpose: Add or replace the meal for a specific day + type. Implicitly creates the week plan if missing.  
Request body: `recipeId`, `mealType`, `targetShoppingListId` (optional; null means no push).  
Response: `weekPlanId`, `year`, `isoWeek`, `meal`.  
Status: 200 OK.  
Errors: 400 invalid input, 401 missing context, 403 not a household member or shopping list not owned, 404 recipe not found in household.

Endpoint: `POST /meals/weeks/{year}/{isoWeek}/days/{dayOfWeek}`  
Purpose: Add or replace a meal when `mealType` is provided in the request body.  
Request body: `recipeId`, `mealType` (required), `targetShoppingListId` (optional).  
Response: `weekPlanId`, `year`, `isoWeek`, `meal`.  
Status: 200 OK.  
Errors: 400 invalid input, 401 missing context, 403 not a household member or shopping list not owned, 404 recipe not found in household.

Endpoint: `DELETE /meals/weeks/{year}/{isoWeek}/days/{dayOfWeek}/meals/{mealType}`  
Purpose: Remove the meal for a specific day + type.  
Response: none.  
Status: 204 No Content.  
Errors: 400 invalid input, 401 missing context, 403 not a household member, 404 meal not found.

Endpoint: `GET /meals/weeks/{year}/{isoWeek}`  
Purpose: Get the week plan.  
Response: `weekPlanId` (nullable), `year`, `isoWeek`, `createdAt` (nullable), `meals`.  
Status: 200 OK (returns an empty plan when missing).  
Errors: 400 invalid input, 401 missing context, 403 not a household member.

Endpoint: `POST /meals/recipes`  
Purpose: Create a household-scoped recipe with ordered ingredients.  
Request body: `name`, `ingredients[]` (`name`, `quantity`, `unit`, `position`).  
Response: `recipeId`, `householdId`, `name`, `createdAt`, `ingredients`.  
Status: 200 OK.  
Errors: 400 invalid input, 401 missing context, 403 not a household member.

Endpoint: `GET /meals/recipes`  
Purpose: List recipes for the current household.  
Response: list of `Recipe` responses with ingredients.  
Status: 200 OK.  
Errors: 401 missing context, 403 not a household member.

Endpoint: `GET /meals/recipes/{recipeId}`  
Purpose: Get one recipe in the current household.  
Response: `Recipe` response with ingredients.  
Status: 200 OK.  
Errors: 401 missing context, 403 not a household member, 404 recipe not found in household.

Endpoint: `PUT /meals/recipes/{recipeId}`  
Purpose: Replace recipe name and ingredient set for one household-scoped recipe.  
Request body: `name`, `ingredients[]` (`name`, `quantity`, `unit`, `position`).  
Response: updated `Recipe` response.  
Status: 200 OK.  
Errors: 400 invalid input, 401 missing context, 403 not a household member, 404 recipe not found in household.
