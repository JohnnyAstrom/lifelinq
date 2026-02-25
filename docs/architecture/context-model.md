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

Today, the active context model is implemented through the group membership model.

- **Membership model:** users and groups are many-to-many via memberships.
- **Current context type:** group is the only context type used for domain scoping.
- **Implicit resolution:** request context is derived in backend request scoping/filtering.
- **Client assumption:** frontend currently assumes `0` or `1` active group context.

Current behavior (minimal scoping):

- **0 memberships:** no active group context; onboarding or group creation is required.
- **1 membership:** that group becomes the active context for the request.
- **>1 memberships:** request is rejected with `401` (ambiguous context; active selection not implemented).

This is a temporary, minimal scoping model and should be treated as transitional behavior.

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
