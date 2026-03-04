# ADR 0005: Phase 3 Closed – Social Layer Stabilized

## Status

Accepted (2026-03-04)

## Context

Phase 3 focused on frontend UX stabilization and social onboarding flows.
The objective was to ensure deterministic authentication gating,
predictable deep-link behavior, and stable invite orchestration.
Prior to Phase 3, authentication, invite handling, and screen transitions
contained edge-case instability and lifecycle ambiguity.

## Decisions

- Authentication root-gating is enforced (hydrating, unauthenticated, authenticated).
- Deep-link handling is centralized at root and guarded against duplicate execution.
- Pending invite state is explicitly modeled via PendingInviteContext.
- Auto-accept behavior is transition-gated and idempotent.
- Login and profile completion screens use consistent fullscreen layout.
- Error states (auth and invite) are lifecycle-controlled and reset on logout.
- No screen bypasses the AuthGate root boundary.

## Consequences

- Authentication flow is deterministic.
- Invite onboarding is stable across app restarts.
- Deep-link replay does not cause duplicate session mutations.
- UI structure across auth-related screens is consistent.
- Phase 3 scope is considered complete and locked.

## Non-Goals

- No session persistence model was introduced.
- No refresh-token mechanism was introduced in Phase 3 itself.
- JWT lifecycle remained short-lived in Phase 3.

## Follow-up status

Phase 4 introduced the explicit refresh session model.
See `ADR-0006` and `ADR-0007` for the implemented session architecture.
