# LifeLinq Mobile App

## Project Structure (`src/`)

### `src/bootstrap`

Contains app entry/composition logic, including the root app shell and screen composition. This is where top-level providers and navigation composition are wired together.

### `src/features`

Contains feature-owned frontend code using the structure `features/<feature>/{api,hooks,components,utils,screens}`. Features should import their domain API through their own `api/` façade, not directly from shared domain API modules.

### `src/shared`

Contains generic shared code used across features, such as UI primitives, shared hooks, auth context, and generic API client/helpers. `shared/` should not become a home for feature-specific domain logic.

## Navigation Model

Navigation is composed in `src/bootstrap/App.tsx` using app state and screen composition rather than a feature-owned navigator per module. Feature screens live under `src/features/<feature>/screens` and are imported into the app shell.

## Dev Client Workflow

### Start Metro

Use `npm start` (or `npx expo start`) for normal frontend iteration. This is the default workflow for JS/TS/UI changes that do not touch native configuration.

### When `expo run:android` is required

Run `npx expo run:android` when you change native configuration or add/change native modules. This rebuilds and reinstalls the Android dev client so native changes actually take effect.

### What triggers a native rebuild

Typical rebuild triggers include changes to Expo config, Android manifest/native config, Gradle/native dependencies, or libraries that require native installation/linking. Pure screen/component/hook logic changes usually only need Metro reload.

## Frontend Architecture (Short Summary)

Screens should be thin and focused on composition, navigation state, and render order. Data hooks handle fetch/mutate/reload, while workflow hooks handle UI state machines and use-case orchestration above data hooks.

Feature code should follow the API façade rule: `screens -> hooks -> feature api -> shared api client`. Shared UI and generic helpers may be used across features, but feature boundaries should remain explicit.

## Canonical Frontend Architecture Reference

See `../docs/architecture/frontend-architecture.md` for the current frontend architecture rules, dependency direction, workflow-hook guidance, and feature structure conventions.
