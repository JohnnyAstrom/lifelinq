# Meals

This document defines the detailed intent and behavior for the Meals feature.

## Purpose

Meals is a **meal planning** surface for the group.
It exists to turn planned meals into actionable shopping items.
Meals is a separate feature from Shopping, with an explicit integration point.
In V0.5c, recipes serve primarily as a shopping-generator: ingredients listed on a recipe are used to push shopping items. Recipes now also carry a first recipe-content foundation with `source`, `short note`, and `instructions`, plus explicit import-readiness provenance fields (`sourceName`, `sourceUrl`, `originKind`, `updatedAt`). Broader culinary modeling such as nutrition, rich media, and advanced organization remains out of scope; image support is intentionally deferred until the later import/media slice.

## Decisions

Decision: Meals integrates with Shopping via application-level command orchestration.
Rationale: Preserves feature isolation and avoids cross-feature repository access.
Consequences: Meals never accesses Shopping repositories; it calls a Shopping use case.
Integration is one-way (Meals → Shopping).
Current implementation: Meals calls `ShoppingApplicationService.addShoppingItem(...)`
when `targetShoppingListId` is provided, once per ingredient occurrence.
Current product reality: meals-pushed shopping items now carry narrow shopping provenance so Shopping can show that they came from meal planning. Shopping may also conservatively absorb compatible meal-plan intake into an existing open shopping item instead of always creating a new row.
Current frontend capture direction: ingredient entry is a lightweight structured row editor with `name`, optional `quantity`, optional `unit`, and implicit `position` from row order. Meals now has an explicit top-level `Plan` / `Recipes` split inside the feature: `Plan` remains the primary workspace for weekly/day planning, while `Recipes` provides a direct saved-recipe home inside Meals. The current Week / Calendar landing area is therefore framed as `Meals > Plan`, while saved-recipe browsing and direct recipe create/open flows live under `Meals > Recipes`. The `Plan a meal` sheet keeps day + meal type, recipe identity, recipe selection, and shopping handoff as meal-slot context. Recipe name, `source`, `short note`, `instructions`, and ingredients live in a dedicated recipe detail/edit sheet opened from that slot editor, and the same recipe destination can also be opened from Day detail for existing planned meals or directly from `Meals > Recipes`. Under the hood, recipe provenance is now more explicit than that UI label suggests: the editable Source field maps to `sourceName`, while recipes also carry `sourceUrl`, `originKind`, and `updatedAt` for later import/review flows. Recipes now also have a first lifecycle state via optional `archivedAt`: active recipes are the default in `Meals > Recipes` and normal recipe-picking flows, while archived recipes stay readable by id so already planned meals do not lose their recipe identity. `Meals > Recipes` now also provides a lightweight archived view and restore action so archive behaves like retirement rather than disappearance. Archived recipe detail inside the Recipes workspace is read-oriented rather than a normal live editor: the content stays readable in lightweight read-only sections, empty recipe content uses calm fallback copy instead of blank disabled fields, and editing pauses until the user restores the recipe back into the active workspace. Guarded delete is now a stronger final cleanup step on top of that lifecycle: it appears only in archived recipe detail, stays blocked while the recipe is still referenced by current or future planned meals, and only removes archived recipes that are no longer in active planning. To keep history understandable after a later delete, planned meals now also store a minimal recipe title snapshot; historical week/day views can fall back to that saved title if the live recipe record has been removed. The Recipes subspace now presents saved recipes more explicitly as reusable content: list rows identify entries as saved recipes, show basic ingredient and created-date context, and can surface a calm duplicate-name hint when multiple saved recipes share the same name. Direct creation inside `Meals > Recipes` is framed as creating a reusable recipe rather than indirectly planning a meal, and it now captures richer recipe content instead of only a title plus ingredient rows. In Day detail, slot editing remains the primary row action, while recipe is presented as a smaller linked next layer behind the planned meal. The recipe sheet itself is now framed as the recipe destination inside Meals rather than only as a transient helper overlay, and it explicitly communicates when the current meal is using a saved recipe and which meal attachment the recipe is being viewed through. The slot editor may also load an existing saved recipe into the current meal slot via a lightweight picker. If a saved recipe is left unchanged, the meal keeps using that recipe as-is. If the user changes a saved recipe's name, source, note, instructions, or ingredients inside this meal flow, the recipe destination shifts to meal-specific copy so it is clearer that this meal is now getting its own recipe version while the original saved recipe remains unchanged. The recipe destination also exposes a narrow explicit shared-edit action for saved recipes, so users can intentionally opt into updating the saved recipe itself instead of following the default meal-specific copy path. Inside `Meals > Recipes`, opening an existing recipe edits that saved recipe directly because the user is already in the Recipes subspace rather than a meal-attached customization flow. URL import now starts from `Meals > Recipes` as a separate import sheet: the user pastes a recipe URL, the frontend requests a backend-generated import draft, and the returned draft opens in the same recipe destination for review and editing before the user intentionally saves it through the normal recipe-create path. Import-v1 still prioritizes structured recipe data (JSON-LD / schema.org) rather than broad heuristic scraping, but now tolerates more nested structured-data layouts and does a better job normalizing common ingredient lines into the existing quantity/unit ingredient editor when the data is clean enough. Failed imports stay draft-only, surface calmer import-specific error guidance in the Recipes subspace, and continue to avoid partial recipe creation. No import draft is auto-saved, and import remains outside the planning surfaces. Shopping intake is triggered from a separate explicit review/confirm sheet rather than as a passive save-time toggle. That review step may selectively include only some ingredient positions; Meals still saves the full recipe content, but only the chosen ingredient occurrences are pushed to Shopping.

## Meals Model (Phase 1)

### Aggregate root

- `WeekPlan` is the aggregate root.
- Primary identity: UUID id.
- Unique constraint: (groupId, year, isoWeek).
- `createdAt` is stored on `WeekPlan` and set via application Clock.

### Scope

- Group-scoped.
- One `WeekPlan` per group per ISO week.

### Entities / value objects

- `WeekPlan`
  - `id`, `groupId`, `year`, `isoWeek`, `createdAt`, `meals`
- `PlannedMeal`
  - `dayOfWeek` (1–7, ISO)
  - `mealType` (BREAKFAST, LUNCH, DINNER)
  - `recipeId` (UUID reference)
  - `recipeTitleSnapshot` (minimal historical fallback label)
  - Identity within `WeekPlan` is `(dayOfWeek, mealType)` (no separate UUID in V0).
  - Inherits group scope from `WeekPlan` (no groupId on `PlannedMeal`).
- `Recipe`
  - `id`, `groupId`, `name`, `sourceName`, `sourceUrl`, `originKind`, `shortNote`, `instructions`, `createdAt`, `updatedAt`, `archivedAt`, `ingredients`
- `Ingredient`
  - `id`, `name`, `quantity` (BigDecimal, nullable), `unit` (shared quantity unit, nullable), `position`

### Invariants

- `dayOfWeek` must be 1–7.
- Max 1 planned meal per day + meal type within a week.
- Adding a meal for an existing day + type replaces the current meal.
- `WeekPlan` unique per group + ISO week.
- `isoWeek` must be valid for the given year (validated in application layer).
- No cross-week move in V0 (move = delete + re-add).
- Group scope is enforced at application layer.
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
- Duplicate ingredient names are not merged in Meals; one shopping item call is made per occurrence. Shopping may still conservatively absorb compatible meal-plan intake on receipt.
- Selective Meals → Shopping push is allowed by explicit ingredient-position selection. Omitted ingredient positions are not sent to Shopping.

### Non-goals (V0)

- No recurring meals.
- No nutrition tracking.
- No nutrition tracking or rich culinary recipe-system depth beyond the current lightweight content fields (`source`, `short note`, `instructions`, structured ingredients).
- No lifecycle synchronization between Meals and Shopping. Intake behavior may merge compatible meal-plan items conservatively, but remains one-way and shopping-owned.
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
- Planned meals now also persist a minimal `recipeTitleSnapshot` so historical views can remain readable if a recipe is later deleted.
- API responses still prefer the live recipe name when it exists, but historical views may fall back to the stored title snapshot.
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
Request body: `recipeId`, `mealType`, `targetShoppingListId` (optional; null means no push), `selectedIngredientPositions[]` (optional; when provided with a shopping target, only those ingredient positions are pushed).  
Response: `weekPlanId`, `year`, `isoWeek`, `meal`.  
Status: 200 OK.  
Errors: 400 invalid input, 401 missing context, 403 not a group member or shopping list not owned, 404 recipe not found in group.

Endpoint: `POST /meals/weeks/{year}/{isoWeek}/days/{dayOfWeek}`  
Purpose: Add or replace a meal when `mealType` is provided in the request body.  
Request body: `recipeId`, `mealType` (required), `targetShoppingListId` (optional), `selectedIngredientPositions[]` (optional).  
Response: `weekPlanId`, `year`, `isoWeek`, `meal`.  
Status: 200 OK.  
Errors: 400 invalid input, 401 missing context, 403 not a group member or shopping list not owned, 404 recipe not found in group.

Endpoint: `DELETE /meals/weeks/{year}/{isoWeek}/days/{dayOfWeek}/meals/{mealType}`  
Purpose: Remove the meal for a specific day + type.  
Response: none.  
Status: 204 No Content.  
Errors: 400 invalid input, 401 missing context, 403 not a group member, 404 meal not found.

Endpoint: `GET /meals/weeks/{year}/{isoWeek}`  
Purpose: Get the week plan.  
Response: `weekPlanId` (nullable), `year`, `isoWeek`, `createdAt` (nullable), `meals`.  
Status: 200 OK (returns an empty plan when missing).  
Errors: 400 invalid input, 401 missing context, 403 not a group member.

Endpoint: `POST /meals/recipes`  
Purpose: Create a group-scoped recipe with ordered ingredients.  
Request body: `name`, `sourceName` (optional), `sourceUrl` (optional), `originKind` (optional; defaults to `MANUAL` in current manual flows), `shortNote` (optional), `instructions` (optional), `ingredients[]` (`name`, `quantity`, `unit`, `position`).  
Response: `recipeId`, `groupId`, `name`, `sourceName`, `sourceUrl`, `originKind`, `shortNote`, `instructions`, `createdAt`, `updatedAt`, `ingredients`.  
Status: 200 OK.  
Errors: 400 invalid input, 401 missing context, 403 not a group member.

Endpoint: `GET /meals/recipes`  
Purpose: List active recipes for the current group. Archived recipes are omitted from this default browsing/selection list.  
Response: list of `Recipe` responses with ingredients and optional `archivedAt` (always null in this active-only list).  
Status: 200 OK.  
Errors: 401 missing context, 403 not a group member.

Endpoint: `GET /meals/recipes/archived`  
Purpose: List archived recipes for the current group inside the Recipes workspace.  
Response: list of archived `Recipe` responses including `archivedAt`.  
Status: 200 OK.  
Errors: 401 missing context, 403 not a group member.

Endpoint: `GET /meals/recipes/{recipeId}`  
Purpose: Get one recipe in the current group, including archived recipes that are still referenced by planned meals.  
Response: `Recipe` response with `sourceName`, `sourceUrl`, `originKind`, `shortNote`, `instructions`, `createdAt`, `updatedAt`, optional `archivedAt`, and ingredients.  
Status: 200 OK.  
Errors: 401 missing context, 403 not a group member, 404 recipe not found in group.

Endpoint: `PUT /meals/recipes/{recipeId}`  
Purpose: Replace recipe content for one group-scoped recipe.  
Request body: `name`, `sourceName` (optional), `sourceUrl` (optional), `originKind` (optional), `shortNote` (optional), `instructions` (optional), `ingredients[]` (`name`, `quantity`, `unit`, `position`).  
Response: updated `Recipe` response including `sourceName`, `sourceUrl`, `originKind`, `shortNote`, `instructions`, `createdAt`, `updatedAt`, optional `archivedAt`, and ingredients.  
Status: 200 OK.  
Errors: 400 invalid input, 401 missing context, 403 not a group member, 404 recipe not found in group.

Endpoint: `POST /meals/recipes/{recipeId}/archive`  
Purpose: Archive one saved recipe so it leaves the active Recipes workspace and normal picker flows without breaking already planned meals that still reference it.  
Request body: none.  
Response: updated `Recipe` response including `archivedAt`.  
Status: 200 OK.  
Errors: 401 missing context, 403 not a group member, 404 recipe not found in group.

Endpoint: `POST /meals/recipes/{recipeId}/restore`  
Purpose: Restore one archived recipe back into the active Recipes workspace and active recipe-picking flows.  
Request body: none.  
Response: updated `Recipe` response with `archivedAt = null`.  
Status: 200 OK.  
Errors: 401 missing context, 403 not a group member, 404 recipe not found in group.

Endpoint: `DELETE /meals/recipes/{recipeId}`  
Purpose: Permanently delete one archived recipe from the Recipes workspace when it is no longer referenced by any current or future planned meal. Historical-only usage does not block delete once the stored meal-title snapshot can preserve readable history.  
Request body: none.  
Response: none.  
Status: 204 No Content.  
Errors: 401 missing context, 403 not a group member, 404 recipe not found in group, 409 delete blocked because the recipe is still active or still used by current/future planned meals.

Endpoint: `POST /meals/recipes/import-drafts`  
Purpose: Fetch a remote recipe URL, parse recipe-oriented structured data, and return a normalized reviewable draft without saving a recipe yet.  
Request body: `url`.  
Response: `name`, `sourceName`, `sourceUrl`, `originKind` (`URL_IMPORT`), `shortNote`, `instructions`, `ingredients[]` (`name`, `quantity`, `unit`, `position`).  
Status: 200 OK.  
Errors: 400 invalid input, 401 missing context, 403 not a group member, 422 import failed because no usable recipe draft could be produced.
