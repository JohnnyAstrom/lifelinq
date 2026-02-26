# Context Model (Canonical)

## Purpose

This document defines the canonical context model for LifeLinq.

LifeLinq is built around explicit user contexts (currently called **groups**).
All domain logic runs within an **active context** for the request.

This document focuses on context rules and behavior.
It does not replace the detailed group or auth documentation.

---

## Core Principles

- A user may belong to multiple contexts.
- Exactly one context must be active per request.
- All domain features operate within a context.
- The backend validates context membership and request scope.
- The client does not have authority to invent or assert unauthorized context.

---

## Current Implementation

The active context model is now explicit and persisted on the user.

- **Membership model:** users and groups are many-to-many via memberships.
- **Current context type:** group is the only context type used for domain scoping.
- **Context source:** active request context is derived from persisted `User.activeGroupId`.
- **Request scoping:** backend filter chain separates identity and context hydration.

Current behavior (Phase 2):

- **AuthenticationFilter:** validates JWT and establishes authenticated `userId`.
- **GroupContextFilter:** loads the user and hydrates `RequestContext.groupId` from `User.activeGroupId`.
- **Scoped endpoints:** require a non-null active group and return `409` if no active group is selected.
- **`/me`:** returns the memberships list and current `activeGroupId` (which may be `null`).

`0` memberships is now an edge case rather than the normal onboarding path, because first login provisioning creates a default group and membership and sets `activeGroupId`.

---

## Intended Direction

LifeLinq is moving toward an explicit context model.

- A user may belong to multiple contexts (groups today, potentially other context types later).
- The active context is selected explicitly, not guessed by the backend.
- Exactly one context must be active for each scoped request.
- The client may maintain active context state for UX flow, but the backend remains authoritative.
- The backend validates membership and scope, but does not perform implicit ambiguity resolution.

This enables:

- clear multi-group behavior
- predictable request scoping
- future per-context feature activation/configuration (for example, enabling or disabling features per group)

---

## References

- `docs/architecture/groups.md`
- `docs/architecture/auth.md`
- `docs/invariants.md`
- `docs/architecture/data_model.md`
