# AGENTS.md (LifeLinq)

## Purpose
This file guides AI coding agents working in this repository.

It defines architectural boundaries and workflow expectations.
Detailed behavior and domain rules live in the documentation under `docs/architecture`.

If any conflict occurs between this file and architecture docs,
**docs/architecture is the source of truth.**

Current project phase: **Phase 5 – platform hardening**

Agents should prioritize:
- stability
- architecture clarity
- small improvements

Agents must **not introduce architectural changes unless explicitly requested.**

---

# Read-first documentation

Before proposing or implementing changes, read:

- README.md
- docs/INVARIANTS.md
- docs/architecture/backend-structure.md
- docs/architecture/frontend-architecture.md
- docs/architecture/groups.md
- docs/architecture/auth.md

Note:
`docs/v0.5.md` is a historical milestone document and not a current architecture source.

---

# Core invariants (non-negotiable)

These principles must never be violated.

- **Group-first model**  
  All domain data belongs to a group.

- **Clients are untrusted**  
  The backend is the source of truth for validation and state.

- **Feature-based architecture**  
  Each domain feature lives in its own module.

- **Explicit domain boundaries**  
  Avoid cross-feature coupling.

---

# Backend architectural boundaries

Backend code lives under:

```text
app.lifelinq
```

Feature structure:

```text
features/<feature>/
  api/
  application/
  domain/
  infrastructure/
```

Rules:

- Controllers must call **ApplicationService** only.
- Controllers must not access repositories directly.
- Controllers must not depend on infrastructure classes.
- Cross-feature interaction must occur through **contract interfaces**, not direct imports.

---

# Frontend architecture rules

Frontend features live under:

```text
src/features/<feature>/
```

Structure:

```text
api/
hooks/
components/
utils/
```

Rules:

- Screens should remain **thin orchestration layers**.
- Feature logic must live inside feature modules.
- Global orchestration (auth, deep links, navigation) belongs in `bootstrap/`.

Bootstrap responsibilities include:

- authentication gate
- deep-link routing
- invitation orchestration
- app-level navigation state

Feature screens must not reimplement these responsibilities.

---

# Auth architecture (implemented)

Authentication infrastructure already exists.

The current auth model includes:

- magic-link login
- JWT access tokens
- refresh token rotation
- refresh sessions
- logout endpoint
- deep-link login completion

Rules:

- Access tokens must remain short-lived.
- Refresh tokens must remain server-controlled and rotated.
- Do not bypass refresh/session logic.
- OAuth providers (Google / Apple) must integrate with the existing session model.

Auth implementation details are documented in:


docs/architecture/auth.md


---

# Invitation system invariants

Invitation behavior is defined in:


docs/architecture/groups.md


Important invariants:

- Invite retrieval endpoints must be **side-effect free**.
- Invitation creation must be **explicit**.
- Invite tokens are **domain tokens**, not authentication tokens.
- Invite acceptance must be validated by backend rules.

Agents must not introduce implicit invitation creation.

---

# Workflow expectations

When implementing changes:

1. Propose a short plan before coding.
2. Prefer small, reviewable changes.
3. Avoid repository-wide refactors unless explicitly requested.
4. Preserve existing behavior unless the change explicitly modifies it.

Prefer clarity and correctness over cleverness.

---

# Documentation rule

If code behavior or architecture changes:

- Update the relevant documentation in `docs/architecture`.
- Do not leave documentation stale.
- Documentation and code must remain aligned.
