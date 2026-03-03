# ADR 0007: Session Model – Refresh Tokens with Rotation

## Status

Proposed (2026-03-XX)

## Context

LifeLinq is passwordless and uses magic links for identity verification.

Current state:
- Access JWTs are short-lived (default 15 minutes).
- No refresh mechanism exists.
- When JWT expires, the client must re-authenticate via magic link.
- Frontend auth state is centralized in AuthContext and token storage is abstracted in tokenStore.

Problem:
Short-lived JWT without refresh causes session churn and unnecessary user friction.

Goal:
Introduce a modern session model that keeps the app “logged in” on trusted devices while preserving clean architecture boundaries.

## Decisions

### 1) Identity vs Session
- Magic link remains **identity proof only**.
- Session renewal must **not** rely on magic link.
- JWT remains a **stateless access token**.

### 2) Token Types
- Access token: short-lived JWT (≈ 15 minutes).
- Refresh token: opaque high-entropy token used to renew access tokens.

### 3) Refresh Token Storage
- Refresh tokens are **never stored in plaintext**.
- Only a **hash** of the refresh token is persisted.
- Hashing uses a keyed approach (HMAC or hash + server secret) and constant-time comparison.

### 4) Rotation (Single-Use)
- Refresh tokens are **single-use**.
- Every successful refresh rotates the token:
  - the presented token is consumed
  - a new refresh token is issued
  - a new access token is issued
- Rotation must be performed atomically (single transaction) and protected against races (optimistic locking or atomic consume).

### 5) Expiration Policy
- Sliding idle expiration: **30 days** since last successful refresh.
- Absolute session cap: **90 days** maximum lifetime regardless of activity.

### 6) Replay / Reuse Detection
- If a refresh token is presented after it has been used, this is treated as suspected replay.
- On reuse detection, the entire session/token family is revoked immediately.

### 7) Logout Semantics
- Logout revokes the current refresh session/family.
- “Logout everywhere” revokes all active refresh sessions for the user.
- Future hook: password reset or email change must revoke all refresh sessions.

### 8) Error Semantics
Refresh endpoint failures are treated as authentication failures:
- missing/malformed/expired/revoked token: 401 Unauthorized (generic message)
- token reuse / rotation race-loser: 401 Unauthorized + revoke session family
- DB/infra failure during rotation: fail closed (no partial issuance)

### 9) Data Model (Authoritative)
Persist refresh sessions and refresh tokens as separate lifecycle entities:

- `auth_refresh_sessions`
  - id, user_id, created_at
  - absolute_expires_at
  - revoked_at, revoke_reason
  - version (for concurrency control)

- `auth_refresh_tokens`
  - id, session_id
  - token_hash (unique)
  - issued_at
  - idle_expires_at
  - used_at
  - replaced_by_token_id
  - revoked_at
  - version

Constraints:
- unique(token_hash)
- indexes: session_id, user_id (via sessions), idle_expires_at, absolute_expires_at
- FK: tokens.session_id -> sessions.id

### 10) Clean Architecture Placement
- Domain (features/auth/domain): session + token models and repository interfaces.
- Application (features/auth/application): issue, rotate, revoke use cases.
- Orchestration (AuthApplicationService): coordinates verify/login → issue session and refresh → rotate.
- API (features/auth/api): thin controllers calling AuthApplicationService only.
- Infrastructure (features/auth/infrastructure): JPA entities, adapters, migrations.

Frontend integration point:
- Centralize refresh handling in AuthContext + API client layer (single source of truth).

## Consequences

- Users log in once per device in normal use.
- Access token expiry becomes invisible if refresh session is valid.
- Token replay attempts invalidate the session family (forced re-auth).
- Session authority becomes explicit and server-controlled.
- Boundaries remain consistent with existing architecture.

## Non-Goals

- No device fingerprinting at this stage.
- No IP-based anomaly detection.
- No refresh token transport changes (cookies vs headers) in this ADR.
- No enterprise-grade session management features.
