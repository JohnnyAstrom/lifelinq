# Authentication and Authorization

This document defines how identity, authentication, and authorization work in LifeLinq.

Authentication answers **who a user is**.
Authorization answers **what a user may do**.

---

## Identity model

LifeLinq distinguishes clearly between:

- User – a real person
- Group – a shared context
- Membership – the relationship between them

Users do not own data.
Groups own data.

---

## CURRENT: Request scoping (Phase 2 implemented)

The backend derives request identity from a **JWT Bearer token** and derives operational group context from persisted user state.

See `docs/architecture/context-model.md` for the canonical context model and current-vs-intended scoping behavior.

- `Authorization: Bearer <token>`
- Required claims: `userId`, `exp`
- Optional claims: `iss`, `aud`

Tokens are **validated for signature and expiration**. If missing or invalid, the request is rejected with **401**.

Request scoping is split into two filters:
- `AuthenticationFilter` (`JWT -> SecurityContext`)
- `GroupContextFilter` (`UserRepository -> activeGroupId -> RequestContext`)

There is no implicit membership-based group resolution in the filter chain.

Current scoping behavior:
- `userId` comes from JWT.
- `groupId` comes from persisted `User.activeGroupId`.
- Scoped endpoints return **409** if `activeGroupId` is missing (`NoActiveGroupSelected`).

This is the current explicit active-group scoping model.

## Current identity endpoint

`GET /me` returns the current authenticated user context:
- `userId`
- `activeGroupId` (nullable)
- `memberships`

If there is no authenticated context, the endpoint returns **401**.

`PUT /me/active-group` sets the active group for the authenticated user.
- Returns **200** with updated `/me` payload on success.
- Returns **401** if there is no authenticated context.
- Returns **409** if the selected group is not a membership of the current user.

`activeGroupId` may be `null` even when memberships exist.
In that case `/me` must reflect that state accurately, and scoped endpoints return **409** until an active group is selected.

`DELETE /me` deletes the authenticated user account.
- Returns **204** on success.
- Returns **401** if there is no authenticated context.
- Returns **409** if deletion is blocked by group governance (the user is the sole `ADMIN` in one or more groups with more than one member).

`DELETE /me` does not depend on implicit group resolution and is no longer blocked by multi-group ambiguity.

---

## CURRENT: Minimal OAuth2 login (implemented)

LifeLinq currently supports a **minimal OAuth2 login**:

- OAuth2 login is wired via Spring Security.
- On successful OAuth2 login, the backend:
  - derives a deterministic internal `userId` from provider + subject
  - ensures the user exists
  - ensures a default group membership exists (default group name: `Personal`)
  - sets `activeGroupId` if missing
  - issues a JWT (access token)

This is intentionally minimal and does **not** include refresh tokens or session management.

## OAuth2 configuration (local/dev)

Minimal OAuth2 setup requires provider registration properties.
Example (Google):

```
spring.security.oauth2.client.registration.google.client-id=...
spring.security.oauth2.client.registration.google.client-secret=...
spring.security.oauth2.client.registration.google.scope=openid,profile,email
spring.security.oauth2.client.provider.google.user-name-attribute=sub
```

These are required for local OAuth2 login but do not enable refresh tokens or session management.

## FUTURE: Full OAuth2 authentication flow (not implemented yet)

LifeLinq will expand OAuth2 to a full authentication flow.

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
- group membership
- role within the group

Roles are intentionally simple:
- admin
- member

---

## JWT usage

JSON Web Tokens are used for stateless authorization.

Tokens contain:
- user identity
- expiration (`exp`)

Tokens do not contain:
- business data
- group context
- roles or permissions

---

## Group context

Clients may request an active-group change, but the backend validates and applies it.

Group context is:
- stored as `User.activeGroupId`
- hydrated server-side into request context
- validated server-side for scoped operations

---

## Security principles

- Clients are untrusted
- Backend is authoritative
- All checks are server-side
- Tokens are short-lived

---

## Summary

Authentication establishes identity.
Authorization uses membership + role checks within the active group context.

Both are enforced strictly to protect shared group data.

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

Invite tokens are **domain‑specific, one‑time tokens** used only to accept group invitations.

Invite tokens:
- are **not** access or refresh tokens
- do **not** create sessions
- are **not** used for API authorization

---

## Request scoping summary (current)

The current request-scoping model is the explicit active-group model described above.
