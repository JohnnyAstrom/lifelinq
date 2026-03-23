# Meals

This document defines the detailed intent and behavior for the Meals feature.

## Purpose

Meals is a **meal planning** surface for the group.
It exists to turn planned meals into actionable shopping items.
Meals is a separate feature from Shopping, with an explicit integration point.
In V0.5c, recipes serve primarily as a shopping-generator: ingredients listed on a recipe are used to push shopping items. Recipes now also carry a first recipe-content foundation with `servings`, `source`, `short note`, and `instructions`, plus explicit import-readiness provenance fields (`sourceName`, `sourceUrl`, `originKind`, `updatedAt`). Broader culinary modeling such as nutrition, rich media, and advanced organization remains out of scope; image support is intentionally deferred until the later import/media slice.

## Decisions

Decision: Meals integrates with Shopping via application-level command orchestration.
Rationale: Preserves feature isolation and avoids cross-feature repository access.
Consequences: Meals never accesses Shopping repositories; it calls a Shopping use case.
Integration is one-way (Meals → Shopping).
Current implementation: Meals calls `ShoppingApplicationService.addShoppingItem(...)`
when `targetShoppingListId` is provided, once per ingredient occurrence.
Current product reality: meals-pushed shopping items now carry narrow shopping provenance so Shopping can show that they came from meal planning. Shopping may also conservatively absorb compatible meal-plan intake into an existing open shopping item instead of always creating a new row.
Current frontend capture direction: ingredient entry is a lightweight structured row editor with `name`, optional `quantity`, optional `unit`, optional preserved `rawText`, and implicit `position` from row order. Meals now has an explicit top-level `Home` / `Plan` / `Recipes` framing inside the feature: entering Meals first lands on a calm domain home, while `Plan` remains the primary workspace for weekly/day planning and `Recipes` provides a direct saved-recipe home inside Meals. The current Week / Calendar landing area is therefore framed as `Meals > Plan`, while saved-recipe browsing and direct recipe create/open flows live under `Meals > Recipes`, and both are first reached through the new `Meals Home` entry surface. Those overview surfaces now also lean more on direct row/card structure and selection controls than on explanatory helper text: the main Meals top bar is shorter, the month surface does not add extra “how to use this” copy, week rows can stay calmly empty without filler sentences, and day-detail slot rows rely on the slot object itself plus action placement rather than `Tap to...` hints. The Recipes landing area now follows that same direction more closely: the top workspace controls are tighter and more unified, the Active view reads more like a native library page than a single large utility card, the search/action area stays supportive rather than dominant, one restrained support layer can sit lightly near the top, and one clearly primary saved-recipes list holds the durable center of the screen. Create/import actions stay light secondary entry points, repeated count chrome is reduced, local search supports re-finding by recipe title or source without feeling like a separate mode, and row recognition leans more on title/source hierarchy than on stacked metadata. The `Plan a meal` sheet now treats the planned meal title as the first-class planning object: day + meal type stay as slot context, the user can save a meal with just a title, and attaching recipe detail is optional enrichment rather than the default meaning of planning. A meal can therefore be title-only (`Tacos`, `Leftovers`, `Tomatsoppa`) or title-plus-recipe, while Recipes remain the richer reusable home for ingredients, instructions, source metadata, import provenance, lifecycle states, and optional servings metadata. When a meal does have recipe detail, recipe name, `servings`, `source`, `short note`, `instructions`, and ingredients still live in the dedicated recipe detail/edit sheet opened from the slot editor, and the same recipe destination can also be opened from Day detail for planned meals that actually have an attached recipe or directly from `Meals > Recipes`. Under the hood, recipe provenance is now more explicit than that UI label suggests: the editable Source field maps to `sourceName`, while recipes also carry `sourceUrl`, `originKind`, and `updatedAt` for later import/review flows. Recipes now also have a first lifecycle state via optional `archivedAt`: active recipes are the default in `Meals > Recipes` and normal recipe-picking flows, while archived recipes stay readable by id so already planned meals do not lose their recipe identity. `Meals > Recipes` now also provides a lightweight archived view and restore action so archive behaves like retirement rather than disappearance. Archived recipe detail inside the Recipes workspace is read-oriented rather than a normal live editor: the content stays readable in lightweight read-only sections, empty recipe content uses calm fallback copy instead of blank disabled fields, editing pauses until the user restores the recipe back into the active workspace, and the archived state now relies more on the archived identity/action structure than on extra helper paragraphs. Active saved recipes now also open read-first in the Recipes workspace: ingredients and instructions are presented as content before editing, and editing becomes an explicit next step rather than the default state. That same shared recipe destination now keeps create and saved-edit closer to that read destination too: new recipes carry an explicit saved-recipe identity and Recipes-library context from the start, while saved read mode now keeps edit available lower in the read surface so content leads before secondary actions. Direct creation inside `Meals > Recipes` still opens straight into editing because the primary task is authoring a new reusable recipe, while imported drafts also stay in edit/review mode so cleanup remains the main job there. That saved-recipe editor now aims to feel like the deliberate counterpart to read mode rather than an older generic form: title editing is lighter and closer to the header identity, new recipes open less empty by seeding the first ingredient row immediately, ingredients and instructions stay visually primary, instruction editing avoids a second duplicated readable preview in the same editor state, note/source metadata are grouped more clearly as secondary context, and the footer actions are calmer so save stays primary while close/archive no longer read as a generic stacked form block. Guarded delete is now a stronger final cleanup step on top of that lifecycle: it appears only in archived recipe detail, stays blocked while the recipe is still referenced by current or future planned meals, and only removes archived recipes that are no longer in active planning. To keep history understandable after a later delete, planned meals now also store both a meal title and a minimal recipe title snapshot; week/day views always prefer the planned meal title as the primary thing being eaten, while historical recipe labels can still fall back to the stored recipe title if the live recipe record has been removed. Planned meals now also keep a lightweight shopping-handoff snapshot after explicit shopping review: when ingredients are sent to Shopping, the meal remembers when that happened and which shopping list was used so the meal editor can show that shopping was already handled and reopen the same review path later. That state remains intentionally conservative rather than synchronized: if the meal content changes and the user saves without going through shopping review again, the handled snapshot is cleared instead of implying Shopping updated automatically. The Recipes subspace now presents saved recipes more explicitly as reusable content: list rows stay lightweight around the recipe title, ingredient count remains the calm supporting metadata, duplicate-name guidance stays subtle instead of turning the whole list into a status-heavy surface, one small support layer may surface near-term intention or recent reuse, and the main list remains the durable library center rather than becoming another parallel block. Direct creation inside `Meals > Recipes` is framed as creating a reusable recipe rather than indirectly planning a meal, and it now captures richer recipe content instead of only a title plus ingredient rows. In Day detail, slot editing remains the primary row action, while recipe is presented as a smaller linked next layer behind the planned meal when a recipe is actually attached. The recipe sheet itself is now framed as the recipe destination inside Meals rather than only as a transient helper overlay, and it explicitly communicates when the current meal is using a saved recipe and which meal attachment the recipe is being viewed through. The slot editor may also load an existing saved recipe into the current meal slot via a lightweight picker. If a saved recipe is left unchanged, the meal keeps using that recipe as-is. If the user changes a saved recipe's name, source, note, instructions, or ingredients inside this meal flow, the recipe destination shifts to meal-specific copy so it is clearer that this meal is now getting its own recipe version while the original saved recipe remains unchanged. The recipe destination also exposes a narrow explicit shared-edit action for saved recipes, so users can intentionally opt into updating the saved recipe itself instead of following the default meal-specific copy path. Inside `Meals > Recipes`, opening an existing recipe no longer drops the user straight into editing; it first presents the saved recipe as readable content, while explicit edit mode remains available as the next step inside the same sheet. URL import now starts from `Meals > Recipes` as a separate import sheet: the user pastes a recipe URL, the frontend requests a backend-generated import draft, and the returned draft opens in the same recipe destination for review and editing before the user intentionally saves it through the normal recipe-create path. Before a new imported draft or manually created recipe is saved into the reusable Recipes library, the frontend now also performs a narrow duplicate warning check: exact `sourceUrl` matches are treated as the strongest signal, and a lighter same-name + same-source check is used as a secondary fallback. When a likely duplicate is found, the user gets a calm choice to open the existing saved recipe or save another copy anyway rather than silently creating another collision. Import-v1 still prioritizes structured recipe data (JSON-LD / schema.org) rather than broad heuristic scraping, but now preserves the imported ingredient line as `rawText` while only normalizing `name`/`quantity`/`unit` when the parser is confident enough. Optional recipe-level `servings` can also flow in through that same structured pass when schema.org-style `recipeYield` data is clearly present, but the current import stays conservative and does not attempt fuzzy servings scraping beyond that structured source path. That import quality pass remains intentionally conservative, but common kitchen measures such as Swedish `msk`, `tsk`, `krm` and English `tbsp`, `tsp` now parse as real structured recipe units when the row shape is straightforward (`quantity + unit + ingredient name`) instead of being downgraded into fallback text. The next conservative cleanup pass also drops obvious non-ingredient labels such as `Till servering` / `Sauce:` before draft creation and avoids inventing weak `PCS` structure when imported lines look more like unsupported measure phrases than real countable ingredients. Kitchen-unit alias recognition is also broader for real imports now: nearby Swedish and English variants such as `matsk`, `tesk`, `kryddmått`, `tbs`, `tblsp`, and `teasp`, including attached forms like `2MSK` or `1kryddmått`, map into the same kitchen-friendly internal units rather than falling through to messy fallback rows. When a quantity-led line still uses an unsupported but meaning-bearing form like `klyfta` or `cloves`, the parser now keeps the full line as safe fallback instead of silently dropping that word and leaving a misleading ingredient name behind. Instruction import is now a little more review-friendly too: already good structured `HowToStep` / `HowToSection` data still wins, while weaker instruction text blobs can be conservatively split into calmer numbered steps when the source already gives strong delimiters like real line breaks or inline numbered step markers. Nearby structured source variants are also handled a bit more broadly now: import can fall back from `recipeIngredient` to `ingredients` and from `recipeInstructions` to `instructions`, and small `ItemList` / `ListItem`-style wrappers with `item`, `text`, `name`, or `value` can still produce a usable draft when the recipe data is clearly present but not in the narrowest happy-path shape. More ambiguous cooking phrases still fall back safely, and multi-step structured instructions are flattened into a numbered multiline block rather than a single undifferentiated text blob. Imported draft review now stays in that same shared recipe sheet, but the review-specific presentation is lighter and more self-explanatory: the imported-draft state is carried mainly by the sheet header itself, the recipe name and long-form content use less field-heavy chrome, ingredients show short “worth checking” cues instead of repeated warning text, expanded imported ingredient rows can also be collapsed directly from the row itself, and original-page details are presented as one quieter provenance section with less raw URL emphasis. Failed imports stay draft-only, surface calmer import-specific error guidance in the Recipes subspace, and continue to avoid partial recipe creation. No import draft is auto-saved, and import remains outside the planning surfaces. Shopping intake is triggered from a separate explicit review/confirm sheet rather than as a passive save-time toggle. That review step may selectively include only some ingredient positions; Meals still saves the full recipe content, but only the chosen ingredient occurrences are pushed to Shopping through a narrow Meals-side shopping projection that keeps recipe storage cooking-native while stripping low-risk shopping noise such as serving/frying context or common trailing prep clauses when it is safe to do so.

## Meals Model (Phase 1)

Recipes workspace note: in the shared recipe destination, top-of-sheet state orientation now lives primarily in the small uppercase label (`CREATE RECIPE`, `SAVED RECIPE`, `EDIT RECIPE`) rather than a separate recipe-state badge. The title remains the main visual anchor. Imported draft review also stays inside that same destination family: it keeps a lighter from-link framing and the same review cues such as flagged ingredients, but uses the calmer create/edit section rhythm instead of a separate utility-style import layout. Recipes capture entry is now also a little more natural than a plain import form: the flow is framed around saving a recipe you found, and a likely recipe link from clipboard may be prefilled when the user opens that capture sheet. Import framing should now read more like “mostly ready to save, with a few things to review” than like parser cleanup, and flagged rows should rely more on the row treatment itself than on stacked helper text. Review is now also lightly resolvable in-place: marked rows can be toggled as `Reviewed`, unresolved rows expose a clearer `Mark reviewed` action, and progress only appears when there is actually something left to review.

Current editor emphasis: the meal editor now uses the already chosen day/slot context instead of repeating a full day selector, keeps the meal title as the primary planning question, and presents a lighter `Details` companion block underneath that title-first flow. When no saved recipe is attached, the editor shows one calm details block with a clear way to either add details to this meal or attach a saved recipe explicitly. That detail path is intentionally meal-scoped in meaning: the user can add ingredients, cooking guidance, and notes as enrichment for the planned meal without the UI primarily framing the step as entering recipe management. In that first enrichment path, extra recipe-style structure such as a separate detail title or source metadata is intentionally deferred so the surface reads more like a deeper layer of meal planning. When a saved recipe is attached, the same block becomes the attached saved-recipe row and keeps open/swap behavior available without turning the editor into a cluster of separate recipe controls. A meal with meaningful local details may also be promoted explicitly into `Meals > Recipes` via a secondary `Save to Recipes` action from the meal-details sheet. That promotion path is user-driven and separate from the primary `Save meal details` action. After promotion, the user remains in the planning editor so the companion block can immediately reclassify into the saved-recipe state instead of hiding the transition behind an instant close. Under the hood, meal-local details may still be backed by a recipe record so the meal keeps its full detail content between sessions, but those backing records do not appear in the reusable Recipes workspace until the user explicitly saves them there.

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
  - `id`, `groupId`, `name`, `sourceName`, `sourceUrl`, `originKind`, `servings`, `shortNote`, `instructions`, `createdAt`, `updatedAt`, `archivedAt`, `ingredients`
- `Ingredient`
  - `id`, `name`, `rawText` (optional preserved source line), `quantity` (BigDecimal, nullable), `unit` (recipe ingredient unit, nullable), `position`

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
- Ingredient handoff into Shopping now uses a dedicated Meals-side projection step.
- Shopping-facing ingredient name reduction before push is intentionally conservative:
  - trim
  - collapse internal whitespace runs to a single space
  - lowercase using `Locale.ROOT`
  - strip low-risk trailing context such as `plus a little extra`, `to serve`, `for frying`, and matching comma-clause noise when it is clearly preparation/usage context rather than ingredient identity
  - strip low-risk leading preparation/size modifiers such as `finely chopped`, `crushed`, or `large` when they make the shopping-facing item more purchase-friendly
- Structured recipe quantity/unit is preserved only when it already maps cleanly to the current shopping-safe unit set. If the shopping-facing name only becomes usable by stripping recipe-measure/count phrasing such as `tbsp` or `clove`, Meals prefers dropping quantity/unit over inventing a weak fallback like `PCS`.
- Duplicate ingredient names are not merged in Meals; one shopping item call is made per occurrence. Shopping may still conservatively absorb compatible meal-plan intake on receipt.
- Selective Meals → Shopping push is allowed by explicit ingredient-position selection. Omitted ingredient positions are not sent to Shopping.

### Non-goals (V0)

- No recurring meals.
- No nutrition tracking.
- No nutrition tracking or rich culinary recipe-system depth beyond the current lightweight content fields (`servings`, `source`, `short note`, `instructions`, structured ingredients).
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
- Active saved recipes may also carry a lightweight user-owned `make soon` marker inside `Meals > Recipes`. It is a calm pre-planning signal only, not a dated plan or queue. In the Active workspace, `Make soon` is the one persistent top support section; `Recently used` stays conditional support and only appears when `Make soon` is not currently occupying that top slot.
- Saved recipe read mode may offer a small session-only portion adjuster when `servings` is clear enough to scale structured ingredient quantities safely. Ambiguous rows stay as originally written.
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
Request body: `name`, `sourceName` (optional), `sourceUrl` (optional), `originKind` (optional; defaults to `MANUAL` in current manual flows), `servings` (optional), `shortNote` (optional), `instructions` (optional), `ingredients[]` (`name`, optional `rawText`, `quantity`, `unit`, `position`).  
Response: `recipeId`, `groupId`, `name`, `sourceName`, `sourceUrl`, `originKind`, `servings`, `shortNote`, `instructions`, `createdAt`, `updatedAt`, `ingredients`.  
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
Response: `Recipe` response with `sourceName`, `sourceUrl`, `originKind`, optional `servings`, `shortNote`, `instructions`, `createdAt`, `updatedAt`, optional `archivedAt`, and ingredients.  
Status: 200 OK.  
Errors: 401 missing context, 403 not a group member, 404 recipe not found in group.

Endpoint: `PUT /meals/recipes/{recipeId}`  
Purpose: Replace recipe content for one group-scoped recipe.  
Request body: `name`, `sourceName` (optional), `sourceUrl` (optional), `originKind` (optional), `servings` (optional), `shortNote` (optional), `instructions` (optional), `ingredients[]` (`name`, optional `rawText`, `quantity`, `unit`, `position`).  
Response: updated `Recipe` response including `sourceName`, `sourceUrl`, `originKind`, optional `servings`, `shortNote`, `instructions`, `createdAt`, `updatedAt`, optional `archivedAt`, and ingredients.  
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
Response: `name`, `sourceName`, `sourceUrl`, `originKind` (`URL_IMPORT`), optional `servings`, `shortNote`, `instructions`, `ingredients[]` (`name`, optional `rawText`, `quantity`, `unit`, `position`).  
Status: 200 OK.  
Errors: 400 invalid input, 401 missing context, 403 not a group member, 422 import failed because no usable recipe draft could be produced.
