# Authentication and Authorization

This document defines how identity, authentication, and authorization work in LifeLinq.

Authentication answers **who a user is**.
Authorization answers **what a user may do**.

---

## Identity model

LifeLinq distinguishes clearly between:

- User – a real person
- Household – a shared context
- Membership – the relationship between them

Users do not own data.
Households own data.

---

## Authentication

LifeLinq uses external identity providers via OAuth2.

Primary provider:
- Google

Authentication flow:
1. User authenticates with external provider
2. Backend validates identity
3. User record is created or retrieved
4. A token is issued

---

## Authorization

Authorization is enforced server-side.

Access is determined by:
- household membership
- role within the household

Roles are intentionally simple:
- owner
- member

---

## JWT usage

JSON Web Tokens are used for stateless authorization.

Tokens contain:
- user identity
- household context
- role

Tokens do not contain:
- business data
- permissions beyond role

---

## Household context

Clients never choose a household.

Household context is always:
- derived from the token
- validated server-side

Switching households requires issuing a new token.

---

## Security principles

- Clients are untrusted
- Backend is authoritative
- All checks are server-side
- Tokens are short-lived

---

## Summary

Authentication establishes identity.
Authorization establishes context.

Both are enforced strictly to protect shared household data.

---

## Future Auth Model (Not Implemented Yet)

This section describes the intended direction for LifeLinq authentication. It is **not implemented yet**.

Planned model:
- Short‑lived **access token** for API authorization.
- Long‑lived **opaque refresh token** stored **server‑side** with rotation and reuse‑detection.
- Refresh token delivered as **HttpOnly cookie** (web) or **secure storage** (React Native).

These are architectural guidelines only and do not reflect current code.

---

## Invite Tokens vs Auth Tokens

Invite tokens are **domain‑specific, one‑time tokens** used only to accept household invitations.

Invite tokens:
- are **not** access or refresh tokens
- do **not** create sessions
- are **not** used for API authorization

---

## TEMPORARY: Header‑based household scoping (pre‑auth)

Until JWT‑based scoping is introduced, the backend accepts **temporary request headers** to populate server‑side household context:

- `X-Household-Id`
- `X-User-Id` (optional)

These headers are **temporary scaffolding only**. They will be removed once JWT‑derived household scoping is implemented.
