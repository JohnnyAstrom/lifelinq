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

## Identity Strategy

Canonical identity:
- `userId` (UUID)

Login methods:
- `EMAIL_MAGIC_LINK`
- `GOOGLE` (future)
- `APPLE` (future)

Account linking rule:
- If an OAuth provider returns a verified email that matches an existing user, log in the existing user instead of creating a new account.
- Provider identities are persisted in `auth_identities` per `(provider, subject)` and reused on subsequent logins.

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

Place governance endpoints are context-driven and scoped by persisted `activeGroupId`:
- `PATCH /me/place` (rename current place)
- `POST /me/place/leave` (leave current place)
- `DELETE /me/place` (delete current place)

---

## CURRENT: OAuth2 login (implemented)

LifeLinq currently supports OAuth2 login:

- OAuth2 login is wired via Spring Security.
- On successful OAuth2 login, the backend:
  - resolves provider identity via `AuthIdentityRepository` using `(provider, subject)`
  - reuses existing linked user when identity exists
  - for first-time provider identities: links to existing `EMAIL` identity only when provider email is verified
  - otherwise creates a new `userId` and persists the provider identity link
  - ensures the user exists
  - ensures a default group membership exists (default group name: `Personal`)
  - sets `activeGroupId` if missing
  - issues an auth pair: `accessToken` + `refreshToken`

OAuth2 success payload is JSON:
- `accessToken` (short-lived JWT)
- `refreshToken` (opaque token)

## Identity resolution (current)

Authentication flows use a centralized identity resolver in `auth` application layer:

- `ResolveUserIdentityUseCase`
- command: `ResolveUserIdentityCommand`
- result: `ResolvedUserIdentity`

This resolver is used by:
- OAuth login
- magic-link verification
- dev login

Resolver responsibilities:
- resolve by provider + subject for OAuth
- resolve/link by email identity when applicable
- provision user via user provisioning contract
- persist new auth identity links when needed

Token/session issuance remains in `AuthApplicationService`.

## OAuth2 configuration (local/dev)

Minimal OAuth2 setup requires provider registration properties.
Example (Google):

```
spring.security.oauth2.client.registration.google.client-id=...
spring.security.oauth2.client.registration.google.client-secret=...
spring.security.oauth2.client.registration.google.scope=openid,profile,email
spring.security.oauth2.client.provider.google.user-name-attribute=sub
```

These are required for local OAuth2 login.

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

JWT TTL is configurable via:
- `lifelinq.jwt.ttlSeconds`

Default is 900 seconds (15 minutes).

---

## Session model (implemented)

LifeLinq now uses explicit auth sessions:

- access token: short-lived JWT
- refresh token: opaque token

Refresh behavior:
- refresh tokens are stored server-side as hashes
- refresh tokens are single-use and rotated on each refresh
- replay/second-use detection revokes the session
- refresh rotation is transactional and optimistic-lock safe

Session endpoints:
- `POST /auth/refresh` (public): `{ refreshToken }` -> `{ accessToken, refreshToken }`
- `POST /auth/logout` (authenticated): `{ refreshToken }` -> `204`

Magic-link verification redirect:
- `GET /auth/magic/verify?token=...` redirects to:
  - `mobileapp://auth/complete#token=<access>&refresh=<refresh>`
- response includes `Referrer-Policy: no-referrer`

Magic-link verification remains identity proof; session renewal uses refresh tokens.

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
- Access tokens are short-lived
- Secrets are fail-closed in production

Secret configuration:
- `lifelinq.jwt.secret` is required (no code fallback)
- `lifelinq.auth.refresh.secret` is required (no code fallback)
- Dev profile defines explicit dev-only values in `application-dev.properties`

---

## Summary

Authentication establishes identity.
Authorization uses membership + role checks within the active group context.

Both are enforced strictly to protect shared group data.

---

## Invite Tokens vs Auth Tokens

Invite tokens are **domain‑specific, one‑time tokens** used only to accept group invitations.

Invite tokens:
- are **not** access or refresh tokens
- do **not** create sessions
- are **not** used for API authorization

Public invite preview contract:
- `/invite/**` is a public **GET-only** SSR preview namespace
- it is read-only by design and must not perform state changes
- invitation acceptance remains a separate mutation flow outside `/invite/**`

---

## Request scoping summary (current)

The current request-scoping model is the explicit active-group model described above.
