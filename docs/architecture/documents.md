# Documents

This document defines the detailed intent and behavior for the Documents feature.

## Purpose

Documents is the household’s **retrieval-first archive** for:
- contracts and agreements
- warranties and receipts
- subscriptions and bills

## Core behavior (V0)

- Metadata-first records only.
- Required: `title`
- Optional: `notes`, `date`, `category`, `tags`, `externalLink`
- All household members can see and search records.

### Implementation status (current)

The backend currently implements a minimal placeholder model.
Only `text` is stored in `DocumentItem`.
No public API endpoints are exposed yet.

The metadata-first shape described above remains the intended V0 target and is not fully implemented.

## Decisions

Decision: Documents are household-shared with createdBy attribution.
Rationale: Shared access is required, while authorship still matters.
Consequences: Store `createdByUserId` for traceability.

Decision: V0 makes no storage decision.
Rationale: Storage choices are costly and premature.
Consequences: `externalLink` may point to Drive/iCloud/OneDrive or other external storage.

## Roadmap

### V0 (now)

- No file storage in LifeLinq.
- External links are allowed (Drive/iCloud/OneDrive, etc).

### V0.5 (future)

- Local attachments for the creator.
- Household sees: “stored locally by X”.
- Not shared access.

### V1 (future)

- Cloud attachments stored in object storage.
- Compression + previews.
- Full household sharing.

## UX principles

- Capture must be fast.
- Search and filter are the primary retrieval tools.
- Structure is optional; retrievability is the goal.
