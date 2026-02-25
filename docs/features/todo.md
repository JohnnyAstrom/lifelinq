# Todo

This document defines the detailed intent and behavior for the Todo feature.

## Purpose

Todos are a **shared group memory**, not project management.
The system must be fast to capture and easy to recall.

## Core views

- **Daily**: execution view for a selected day, focused on day-scoped todos and completion.
- **Weekly**: planning + overview across the current week, including day-grouped items and week-scoped planning items.
- **Monthly**: calendar overview and navigation layer for month planning and day navigation.
- **Later / Unplanned**: items without a scheduled day/week/month remain first-class and must be easy to access.
- Target: switching between time scopes must be frictionless.

## Month Calendar API (Current)

- `GET /todos/calendar/{year}/{month}`
- `month` uses ISO numbering `1..12`; invalid values return `400`.
- Month boundaries are computed using `YearMonth` in UTC.
- Monthly calendar responses include:
  - `DAY`-scoped todos whose `dueDate` falls within the month range
  - `MONTH`-scoped todos for the requested year/month
- `LATER` and `WEEK` todos are excluded from the month grid API.

## Data shape (conceptual)

- `text` (required)
- `status` (OPEN, COMPLETED)
- `scope` (DAY, WEEK, MONTH, LATER) — persisted and authoritative for scheduling context
- `dueDate` / `dueTime` (DAY only; `dueTime` is optional)
- `scopeYear` + `scopeWeek` (WEEK only)
- `scopeYear` + `scopeMonth` (MONTH only)
- `createdAt` (required)
- `completedAt` (optional)
- `recurrence` (optional)
- `assignedTo` (optional userId)
- `createdBy` (userId)

## Assignment rules

- Assignment is **coordination only**, never ownership.
- Any group member can assign, reassign, or clear assignment.

## Completion and deletion

- **Complete** means the item was done and should remain visible in history.
- **Delete** means the item was intentionally removed and not completed.
- Completion and deletion are distinct actions with distinct meaning.
- Delete is lifecycle-only and does not use a separate status value.
- Deleted items must not allow further state mutations (complete/toggle).
- Completing a todo must not affect `deletedAt`.
- Completing a todo sets `completedAt`; reopening clears `completedAt`.
- Default list queries exclude items where `deletedAt` is set.

## Decisions

Decision: Todo is historical (soft delete).
Rationale: Completed work must remain visible for accountability and recall.
Consequences: Use `deletedAt` (soft delete) and keep completed items in history.

Decision: Default views list non-deleted items.
Rationale: Keep daily views clean while preserving history.
Consequences: Filters can include completed and deleted history explicitly.

## Calendar sync

- **Google Calendar sync** is planned for scheduled items.
- Sync is for items with date/time only.
- Exact sync direction (one-way vs two-way) can be decided later, but must stay minimal.

## UX principles

- Capture must be faster than remembering.
- Incomplete data is allowed.
- Items without dates are still first-class.
