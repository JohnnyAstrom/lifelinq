# AGENTS.md (LifeLinq)

## Read-first docs
Before doing any work, read:
- README.md
- docs/INVARIANTS.md
- docs/architecture/backend-structure.md
- docs/architecture/frontend-architecture.md
- docs/architecture/groups.md
- docs/architecture/auth.md

Note: `docs/v0.5.md` is a historical milestone document and not a current architectural source of truth.

## Non-negotiables
- Group-first: all domain data belongs to a group.
- Clients are untrusted; backend is the source of truth.
- Feature-based architecture.
- Backend + frontend features map 1:1.
- Do not introduce new layers or cross-feature coupling.

## AI Guidance: Auth & User Feature Proposals

When proposing next steps related to authentication, users, or tokens, follow these rules strictly:

- Minimal identity read endpoints (e.g. `/me`) and JWT-based request scoping are allowed.
- Do not introduce full authentication features yet (OAuth flows, refresh tokens, session management).
- Auth-related domain objects beyond the current minimal `User` must not be introduced until persistence and a security context are expanded.
- Avoid proposing auth use cases that depend on permissions or roles beyond current group membership checks.
- Access token and refresh token handling (rotation, reuse detection) must remain documented-only until auth implementation explicitly begins.
- Invite tokens are domain-specific, single-use tokens and must never be treated as auth or session tokens.
- Any auth-related proposal must clearly explain why it is architecturally significant at this point in the project.

This section defines how AI should reason about auth-related suggestions going forward.

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
