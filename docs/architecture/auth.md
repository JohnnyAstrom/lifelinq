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

## CURRENT: Minimal request scoping (implemented)

The backend currently derives request context from a **JWT Bearer token**:

See `docs/architecture/context-model.md` for the canonical context model and current-vs-intended scoping behavior.

- `Authorization: Bearer <token>`
- Required claims: `userId`, `exp`
- Optional claims: `iss`, `aud`

Tokens are **validated for signature and expiration**. If missing or invalid, the request is rejected with **401**.

After validation, the server resolves **group context**:
- If the user has exactly one group membership → use it.
- If the user has none → continue with `groupId = null` (endpoints requiring scope return 401).
- If the user has multiple → return **401** (active group selection not implemented yet).

This is a **minimal scoping layer** only:
- no OAuth flow
- no refresh tokens
- minimal user persistence only (ensure user exists)

## Current identity endpoint (minimal)

`GET /me` returns the current request context (userId and groupId).
If there is no authenticated context, the endpoint returns **401**.

---

## CURRENT: Minimal OAuth2 login (implemented)

LifeLinq currently supports a **minimal OAuth2 login**:

- OAuth2 login is wired via Spring Security.
- On successful OAuth2 login, the backend:
  - derives a deterministic internal `userId` from provider + subject
  - ensures the user exists
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
- owner
- member

---

## JWT usage

JSON Web Tokens are used for stateless authorization.

Tokens contain:
- user identity
- expiration (`exp`)

Tokens do not contain:
- business data
- group context (current)
- roles or permissions (current)

---

## Group context

Clients never choose a group.

Group context is always:
- derived server-side from membership
- validated server-side

Switching groups requires an explicit active-group selection (not implemented yet).

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

## JWT‑based request scoping (minimal)

This is the **current** request‑scoping mechanism described above.
