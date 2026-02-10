# AGENTS.md (LifeLinq)

## Read-first docs
Before doing any work, read:
- README.md
- docs/INVARIANTS.md
- docs/architecture/backend-structure.md
- docs/architecture/frontend-structure.md
- docs/architecture/households.md
- docs/architecture/auth.md

## Non-negotiables
- Household-first: all domain data belongs to a household.
- Clients are untrusted; backend is the source of truth.
- Feature-based architecture.
- Backend + frontend features map 1:1.
- Do not introduce new layers or cross-feature coupling.

## Backend structure
All code lives under `app.lifelinq`.
Use: `features/<feature>/{api,application,domain,infrastructure}`.

## Frontend structure
Use: `src/features/<feature>/{api,hooks,components,utils}` and keep screens thin.

## Workflow expectations
- Propose a short plan first.
- Make small, reviewable changes.
- Prefer correctness and clarity over cleverness.

## Documentation rule
When making changes:
- Update relevant documentation files.
- If behavior or structure changes, update docs/architecture or decisions.
- Do not leave documentation stale after code changes.