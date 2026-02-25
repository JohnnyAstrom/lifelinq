# Frontend Architecture (Canonical)

This document is the canonical frontend architecture source for LifeLinq.
It describes the structure that exists in `mobile-app/src/` today and the rules used to keep the frontend aligned with backend feature boundaries.

---

## Purpose

The frontend uses a feature-based structure so that each domain (todo, meals, shopping, documents, group, auth) can evolve with clear ownership.
This document defines the dependency rules and responsibility boundaries used in the current codebase.

---

## Core principles

- Frontend features map 1:1 to backend features whenever possible.
- Backend is the source of truth for domain state and business rules.
- Screens should stay thin and focus on composition, navigation, and view state.
- Feature code should depend on feature-owned APIs, not shared domain-specific API modules.

---

## Current `mobile-app/src` structure

At a high level, the frontend is organized into:

- `bootstrap/` – app composition and navigation entry (`App.tsx`)
- `features/` – feature-owned frontend code
- `shared/` – reusable UI, shared hooks, auth context, and generic API client helpers
- `screens/` – remaining root-level screens that are not yet feature-owned (e.g. cross-feature/home-level screens)

---

## Feature-based screen placement

Feature screens live under:

`src/features/<feature>/screens/`

This is now the default placement for feature-owned screens (e.g. Meals, Todo, Shopping, Documents, Group, Auth).
Root `src/screens/` is reserved for cross-feature orchestration screens or screens that do not naturally belong to a single feature.

---

## Canonical feature layout

Each frontend feature should use this structure:

```text
src/features/<feature>/
  api/
  hooks/
  components/
  utils/
  screens/
```

### `api/`

Feature-owned API façade/wrapper for that domain.
This is the only API entrypoint feature code should import for domain-specific backend calls and types.

### `hooks/`

Hooks for data loading/mutations and workflow/application orchestration.
This folder may contain both data hooks and workflow hooks (see below).

### `components/`

Presentational components and view-level UI composition blocks.
These should be render-focused and avoid business logic.

### `utils/`

Feature-local pure helpers such as formatting, parsing, and date calculations.
These should be side-effect free.

### `screens/`

Feature-owned screens that compose hooks and components.
Screens should not become mini-application layers.

---

## Dependency direction (required)

The canonical dependency flow in frontend features is:

**screens -> hooks -> feature api -> shared api client**

This direction keeps UI composition separate from domain API integration and prevents screens from owning low-level network concerns.

### Allowed shared dependencies

Feature code may depend on shared modules for:

- `shared/ui/*` (reusable UI primitives and overlays)
- `shared/hooks/*` (generic hooks such as back-handler helpers)
- `shared/auth/*` (auth context)
- `shared/api/client.ts` and generic API helpers

### Not allowed (default rule)

Feature code should not import another feature’s API/data internals directly.
If cross-feature orchestration is required, it must be explicit and limited to workflow hooks.

---

## Feature API façade rule

Each feature must expose a feature-owned API module under `src/features/<feature>/api/`.
Feature hooks and feature screens should import domain-specific API functions and types through that façade, not from `shared/api/<domain>.ts`.

This rule exists to:

- keep boundaries auditable
- allow internal implementation moves without touching feature callers
- keep `shared/api` focused on generic infrastructure concerns

---

## Data hooks vs workflow hooks

### Data hooks

Data hooks are responsible for reading/mutating backend state and syncing UI with backend responses.
They typically own fetch/mutate/reload behavior and expose loading/error/data state.

Examples in current codebase:

- `useTodos`
- `useWeekPlan`
- `useShoppingLists`
- `useDocuments`

### Workflow hooks

Workflow hooks are thin application-layer hooks above data hooks.
They own UI workflow state machines and mutation sequencing, while leaving rendering and navigation in screens.

Examples in current codebase:

- `useMealsWorkflow`
- `useShoppingListDetailWorkflow`
- `useShoppingListsWorkflow`

Workflow hooks may orchestrate multiple actions and resets, but should not render UI or own navigation.

---

## Thin screen rule

Screens are composition/orchestration entrypoints for a user-facing route.
They should coordinate feature hooks and render feature components, not encode domain workflows directly.

### Screens should do

- hold route/view state (e.g. selected date, active tab/view mode)
- compose presentational components
- wire callbacks to workflow/data hooks
- manage overlay render order when multiple sheets/modals are present

### Screens should not do

- branch directly on create/update API flows
- own multi-step mutation workflows
- contain large parsing/formatting helper collections
- duplicate feature grouping/progress derivation logic

---

## Cross-feature orchestration policy

Default rule: features should not depend on other features.

Allowed exception: explicit orchestration in a workflow hook when one feature intentionally coordinates another feature’s behavior (for example Meals -> Shopping sync after saving a meal).

When this happens:

- keep the dependency in a workflow hook, not a screen
- keep it narrow and practical
- document it in feature docs / architecture notes

Cross-feature orchestration must not bypass backend feature boundaries.

---

## Shared layer boundary

`shared/` exists for generic, reusable frontend concerns:

- UI primitives and shared visual patterns
- generic hooks
- auth context
- generic API client and helpers

`shared/` should not accumulate feature-specific business rules or domain workflows.
If a module is domain-specific (e.g. shopping or meals API), it should be accessed through a feature-owned API façade.

---

## Current architecture maturity (summary)

- **Todo**: strongest separation so far (feature screens, data hooks, grouping/progress hooks, presentational components, utils).
- **Meals**: feature-based with extracted views/components/utils and a workflow hook; still refining screen thinning and feature-owned API usage patterns.
- **Shopping**: boundary cleanup and Phase A/B refactor underway; screens are becoming thinner via presentational extraction and workflow hooks.
- **Documents / Group / Auth**: functional but less refactored; more screen-centric in places.

---

## Maintenance rule

Update this document when frontend structure, dependency direction, or hook layering rules change.
This file is intended to describe the actual architecture in the codebase, not a future wishlist.

