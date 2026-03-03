# Groups and Membership

This document defines how groups and memberships work in LifeLinq.

Groups are the **primary organizational unit** of the system.

---

## Group

A group represents a shared context for coordination.

Examples:
- a household
- a family cottage
- an association

A group:
- owns all domain data
- exists independently of individual users

---

## Membership

A membership connects a user to a group.

A membership defines:
- which group the user belongs to
- the user’s role within that group

Memberships may change over time without affecting group data.

---

## Roles

Roles are intentionally minimal:

- `ADMIN` – manages group membership
- `MEMBER` – participates in the group

Roles control access, not ownership.
Roles do not gate everyday coordination actions like task assignment.

## Governance (Phase 1)

Current governance rules:

- `ADMIN -> ADMIN` removal is always forbidden (intentional governance rule).
- Removing a member is blocked if that member is the **sole `ADMIN`** in a group with **more than one member**.
- Removing the sole `ADMIN` is allowed when they are the only member in the group.
- Account deletion is blocked if the user is the sole `ADMIN` in any group with more than one member.
- Successful account deletion:
  - removes all memberships for the user
  - deletes groups that become empty
  - deletes the user account

## Place governance (current)

Current place-governance operations are context-driven and use the active group from request context:

- `PATCH /me/place` - rename current place (ADMIN only, non-blank name)
- `POST /me/place/leave` - leave current place
- `DELETE /me/place` - delete current place (ADMIN only)

Policy rules:

- Default (`Personal`) place is account-bound:
  - may be renamed
  - cannot be left
  - cannot be deleted
  - is removed only during account deletion
- Leaving current place is blocked when:
  - it is default place
  - user has only one place
  - user is sole `ADMIN` while other members exist
- If the last member leaves, the place is deleted.
- After leave:
  - if user has remaining memberships, `activeGroupId` is switched to another membership
  - otherwise default place is provisioned and activated
- Deleting current place is blocked when:
  - actor is not `ADMIN`
  - place is default
  - actor has only one place
- Deleting place removes the place and all memberships transactionally.
- For affected users, if `activeGroupId` pointed to the deleted place, it is cleared (`null`).

---

## Multiple groups

A user may belong to multiple groups.

See `docs/architecture/context-model.md` for the canonical active-context/scoping rules (current and intended behavior).

At any given time:
- exactly one group can be used for scoped operations
- the active group is taken from persisted `User.activeGroupId`

If `activeGroupId` is missing while memberships exist, scoped endpoints return `409` until an active group is selected.

---

## Invitations

Groups may invite new members.

Invitations:
- are token-based
- are time-limited
- grant membership upon acceptance
- switch the user's active group to the invited group upon successful acceptance (Phase 2)
- support two types:
  - `EMAIL` (targeted invite, requires invitee email)
  - `LINK` (open invite, no invitee email)
- support public, non-mutating preview via `GET /invite/{token}` (server-rendered HTML)
- include a human-friendly short invite code (`shortCode`, 6 uppercase alphanumeric) for manual code resolution

Invite web namespace contract:
- `/invite/**` is public **GET-only**
- it is an SSR preview gateway, not an accept/mutation API
- it must remain strictly read-only (no writes, counters, or touch-style persistence)
- accept/mutation endpoints must never live under `/invite/**`

## Decisions

Decision: Invitations are a sub-aggregate within Group (Phase 1), not a separate feature.
Rationale: Invitations are a group-scoped onboarding tool, not an independent domain.
Consequences: Invitation logic stays in Group; no separate repositories or APIs outside the feature.
Future: May be extracted if resend, tracking, or pending-user workflows become core.

## Invitation Model (Phase 1)

### Decision

Identity: invitationId  
Token: random value used only for acceptance flow  
Ownership: only ADMIN may create/revoke  
Expiry: default TTL applied if not provided  
Status: ACTIVE | REVOKED  
Duplicate rule: only one non-expired ACTIVE invitation per email per group  
Expiry evaluation: expired is derived when status is ACTIVE and now > expiresAt  
Default TTL applied in ApplicationService when expiresAt is not provided
Invitee email is normalized (trim + lowercase) in ApplicationService before duplicate checks  
Time policy (clock/now) is owned by ApplicationService and passed into the use case
Invitation usage model:
- `EMAIL` invitations are implicitly single-use (`maxUses = 1` internally)
- `LINK` invitations are unlimited by default (`maxUses = null`)
- `usageCount` tracks acceptances and increments only when `maxUses` is set
- acceptance is rejected when `maxUses` is set and `usageCount >= maxUses`
- exhaustion auto-revokes only when `maxUses` is set
- `inviterDisplayName` is stored as a nullable snapshot at creation time (used for read-only preview copy)
- `shortCode` is stored as a nullable unique value (6 uppercase alphanumeric), generated on creation for `EMAIL` and `LINK`
- default endpoint behavior:
  - `POST /groups/invitations` creates `EMAIL` invitations
  - `GET /groups/{groupId}/invitations/active` returns the current active `LINK` invitation for the active context (read-only; no creation)
  - `POST /groups/invitations/link` explicitly creates a new `LINK` invitation
    - returns `409` when an accept-allowed `LINK` invitation already exists
  - `POST /groups/invitations/resolve-code` resolves metadata by `shortCode` (read-only, no mutation)

### Rationale

invitationId separates domain identity from delivery mechanism  
token can be rotated/resend without changing identity  
ACTIVE constraint prevents inconsistent onboarding state

### Consequences

Revoke operates on invitationId  
409 returned when ACTIVE invitation already exists  
Future resend flow can reuse invitationId but regenerate token  
Expiry evaluation can be derived from expiresAt + status logic
Acceptance validates: invitation exists, status is ACTIVE, and it is not expired

---

## Invitation Lifecycle (Domain)

At the domain level, invitations follow a simple lifecycle:
- `ACTIVE` → `REVOKED` when an admin revokes an invitation
- `ACTIVE` → expired (derived) when `now > expiresAt`

Invitations are time‑limited, and acceptance of a valid invitation creates a group membership.

---

## Design rationale

Separating users from groups:
- enables shared ownership
- avoids data silos
- reflects real-life relationships

---

## Summary

Groups own data.
Users participate via memberships.

This separation is foundational to LifeLinq.

