# ADR 0006: Phase 4 – Explicit Session Model & Token Rotation

## Status

Proposed (2026-03-XX)

## Context

Current authentication relies on:

Short-lived JWT access tokens.

Magic link identity verification.

No refresh-token based session model.

This requires re-authentication once access tokens expire,
creating unnecessary friction for users.

LifeLinq prioritizes a low-friction, family-friendly experience.
Authentication should be device-trusted and largely invisible
after initial identity verification.

## Decisions

- Magic link remains identity proof only.
- Access tokens (JWT) remain short-lived (~15 minutes).
- A refresh-token based session model will be introduced.
- Refresh tokens:
  - Are long-lived (~30 days).
  - Are stored hashed in persistence.
  - Are rotated upon use.
  - Support sliding expiration.
- Logout revokes refresh tokens.
- Access token expiration does not force re-authentication if refresh token is valid.
- Identity lifecycle and session lifecycle remain decoupled.

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
