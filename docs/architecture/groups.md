# Groups and Membership

This document defines how groups and memberships work in LifeLinq.

Groups are the **primary organizational unit** of the system.

---

## Group

A group represents a shared living context.

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

- owner – manages group membership
- member – participates in the group

Roles control access, not ownership.
Roles do not gate everyday coordination actions like task assignment.

---

## Multiple groups

A user may belong to multiple groups.

See `docs/architecture/context-model.md` for the canonical active-context/scoping rules (current and intended behavior).

At any given time:
- exactly one group can be used for scoped operations
- the active group is resolved server‑side from membership data

If the user has multiple groups, active group selection is required
(not implemented yet).

---

## Invitations

Groups may invite new members.

Invitations:
- are token-based
- are time-limited
- grant membership upon acceptance

## Decisions

Decision: Invitations are a sub-aggregate within Group (Phase 1), not a separate feature.
Rationale: Invitations are a group-scoped onboarding tool, not an independent domain.
Consequences: Invitation logic stays in Group; no separate repositories or APIs outside the feature.
Future: May be extracted if resend, tracking, or pending-user workflows become core.

## Invitation Model (Phase 1)

### Decision

Identity: invitationId  
Token: random value used only for acceptance flow  
Ownership: only OWNER may create/revoke  
Expiry: default TTL applied if not provided  
Status: ACTIVE | REVOKED  
Duplicate rule: only one non-expired ACTIVE invitation per email per group  
Expiry evaluation: expired is derived when status is ACTIVE and now > expiresAt  
Default TTL applied in ApplicationService when expiresAt is not provided
Invitee email is normalized (trim + lowercase) in ApplicationService before duplicate checks  
Time policy (clock/now) is owned by ApplicationService and passed into the use case

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
- `ACTIVE` → `REVOKED` when an owner revokes an invitation
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
