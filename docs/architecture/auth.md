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

## CURRENT: Minimal request scoping (implemented)

The backend currently derives request context from a **JWT Bearer token**:

- `Authorization: Bearer <token>`
- Required claims: `householdId`, `userId`, `exp`

Tokens are **validated for signature and expiration**. If missing or invalid, the request is rejected with **401**.

This is a **minimal scoping layer** only:
- no OAuth flow
- no refresh tokens
- no user persistence yet

---

## FUTURE: Authentication via OAuth2 (not implemented yet)

LifeLinq will use external identity providers via OAuth2.

Primary provider:
- Google

Planned authentication flow (future):
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

## JWT‑based request scoping (minimal)

This is the **current** request‑scoping mechanism described above.
