# ADR 0003: Phase 1 Locked Decisions

## Status

Accepted (2026-02-12)

## Context

Phase 1 introduces core household, shopping, todo, meals, and documents behaviors.
Ambiguity in boundaries and lifecycle rules would create drift and future refactors.

## Decisions

- Invitations are a sub-aggregate within Household (not a separate feature).
- Shopping model is Household → ShoppingList → ShoppingItem.
- ShoppingList is the aggregate root; toggling is a state mutation within the list.
- Todo is historical; COMPLETED ≠ DELETED (soft delete with `deletedAt`).
- Meals integrates with Shopping via application-level command orchestration.
- Documents are household-shared with createdBy attribution.
- Documents V0 makes no storage decision; `externalLink` can point elsewhere.

## Consequences

- Feature isolation is preserved.
- Future refactors are minimized.
- Integration points are explicit and testable.

