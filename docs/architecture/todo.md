# Todo

This document defines the detailed intent and behavior for the Todo feature.

## Purpose

Todos are a **shared household memory**, not project management.
The system must be fast to capture and easy to recall.

## Core views

- **List view**: the primary overview of all items.
- **Calendar view**: simple scheduling view for dated items (month API implemented).
- Target: switching between list and calendar must be frictionless.

## Calendar API (V0.5b)

- `GET /todos/calendar/{year}/{month}`
- `month` uses ISO numbering `1..12`; invalid values return `400`.
- Month boundaries are computed using `YearMonth` in UTC.
- Filtering is based on `dueDate` (`LocalDate`) only, with no timezone conversion.

## Data shape (conceptual)

- `text` (required)
- `status` (OPEN, COMPLETED)
- `dueDate` (optional)
- `dueTime` (optional)
- `recurrence` (optional)
- `assignedTo` (optional userId)
- `createdBy` (userId)

## Assignment rules

- Assignment is **coordination only**, never ownership.
- Any household member can assign, reassign, or clear assignment.

## Completion and deletion

- **Complete** means the item was done and should remain visible in history.
- **Delete** means the item was intentionally removed and not completed.
- Completion and deletion are distinct actions with distinct meaning.
- Delete is lifecycle-only and does not use a separate status value.
- Deleted items must not allow further state mutations (complete/toggle).
- Completing a todo must not affect `deletedAt`.
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
