# ADR 0004: Phase 2 – Explicit Active Group & Context Separation

## Status

Accepted (2026-02-26)

## Context

Implicit membership-based group resolution previously existed.
It caused ambiguity for multi-group users.
`401` responses occurred due to context uncertainty.
Identity and context were previously coupled.
`DELETE /me` and other user-scoped operations could fail due to group ambiguity.

## Decisions

- Identity (JWT) establishes `userId` only.
- Operational context is derived exclusively from persisted `User.activeGroupId`.
- Implicit membership-based group resolution is removed.
- Filters are split into:
  - `AuthenticationFilter` (`JWT -> SecurityContext`)
  - `GroupContextFilter` (hydrates `activeGroupId` -> `RequestContext`)
- Group-scoped endpoints require explicit active group selection.
- Missing active group results in `409 Conflict`.
- User-scoped endpoints do not depend on active group.

## Consequences

- Multi-group ambiguity is eliminated.
- Identity and context are decoupled.
- Governance logic is independent from scoping ambiguity.
- Filters are technical and non-orchestrating.
- Context is explicit and user-controlled.

## Non-Goals

- No cross-group capabilities are introduced.
- No read-model/projection layer is introduced.
- No event-driven side effects are introduced.
