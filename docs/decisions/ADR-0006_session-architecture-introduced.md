# ADR 0006: Phase 4 – Explicit Session Model & Token Rotation

## Status

Accepted (2026-03-04)

## Context

Previous authentication relied on:

Short-lived JWT access tokens.

Magic link identity verification.

This required re-authentication once access tokens expired,
creating unnecessary friction for users.

LifeLinq prioritizes a low-friction, family-friendly experience.
Authentication should be device-trusted and largely invisible
after initial identity verification.

## Decisions

- Magic link remains identity proof only.
- Access tokens (JWT) remain short-lived (~15 minutes).
- A refresh-token based session model is introduced.
- Refresh tokens:
  - Are long-lived with sliding idle window (~30 days).
  - Are stored hashed in persistence.
  - Are rotated upon use.
  - Support sliding expiration.
- Logout revokes refresh tokens.
- Access token expiration does not force re-authentication if refresh token is valid.
- Identity lifecycle and session lifecycle remain decoupled.

Implementation notes:
- `POST /auth/refresh` is live.
- `POST /auth/logout` is live.
- OAuth2 success returns `{ accessToken, refreshToken }`.
- Magic-link verify redirect carries both tokens in fragment.

## Consequences

- Users log in once per device.
- Session renewal is transparent.
- Magic link is required only:
  - On new device
  - After logout
  - After refresh-token expiration
- Device trust becomes explicit and controlled.
- Clean Architecture boundaries remain intact.

## Non-Goals

- No device fingerprinting at this stage.
- No IP-based anomaly detection.
- No enterprise-level session governance.
