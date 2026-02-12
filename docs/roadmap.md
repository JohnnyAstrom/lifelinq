# Roadmap

This roadmap describes the intended path to the long-term product vision.
It is a **guiding overview**, not a fixed contract.
Later phases are tentative and may shift as we learn.

## Phase 0 — Foundation Alignment (Now)

- Align documentation and feature naming with the agreed vision.
- Ensure backend/frontend feature structure is 1:1.
- Split "current" vs "planned" in data model documentation.

## Phase 1 — Core Foundations

- Household creation and membership flows are stable.
- Minimal auth scoping is consistent across all features.
- Todos baseline (list + calendar, completion vs deletion).

### Locked decisions (Phase 1)

- Invitations are a sub-aggregate within Household, not a separate feature. Why: keeps onboarding scoped to household domain.
- Shopping model is Household → ShoppingList → ShoppingItem. Why: supports multiple lists without cross-feature leakage.
- ShoppingList is the aggregate root; toggling bought/to-buy is a state mutation. Why: preserves list integrity.
- Todo is historical; COMPLETED ≠ DELETED. Why: keeps accountability and avoids data loss.
- Meals → Shopping integration is application-level command orchestration. Why: avoids cross-feature repo access.
- Documents are household-shared with createdBy attribution. Why: shared access with traceability.
- Documents V0 has no storage decisions; externalLink points elsewhere. Why: defer storage without blocking value.

## Phase 2 — Meals (V0)

- Week view as primary planning surface.
- Meal entries linked to recipes.
- Ingredients can generate shopping items.

## Phase 3 — Shopping (V0+)

- Multiple lists.
- Toggle bought/to-buy within the same list.
- Integration endpoint for Meals → Shopping.

## Phase 4 — Documents (V0)

- Metadata-first records (title, notes, date, category, tags, external link).
- Search and filter as primary retrieval.

## Phase 5 — Product Polish

- Today/Overview screen (todos + meals + shopping).
- i18n (Swedish + English) across all user-facing text.
- UX refinements and consistent navigation.

## Phase 6 — Long-term Phase (Target)

- Documents V1: cloud storage, compression, previews, household sharing.
- Active household selection (multi-household support).
- Meals enhancements (history tooling, week templates, faster planning flows).
- Observability, backups, and scaling hardening.
