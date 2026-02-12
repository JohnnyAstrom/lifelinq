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

