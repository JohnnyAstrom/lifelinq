# Households and Membership

This document defines how households and memberships work in LifeLinq.

Households are the **primary organizational unit** of the system.

---

## Household

A household represents a shared living context.

Examples:
- a family
- a couple
- roommates

A household:
- owns all domain data
- exists independently of individual users

---

## Membership

A membership connects a user to a household.

A membership defines:
- which household the user belongs to
- the user’s role within that household

Memberships may change over time without affecting household data.

---

## Roles

Roles are intentionally minimal:

- owner – manages household membership
- member – participates in the household

Roles control access, not ownership.
Roles do not gate everyday coordination actions like task assignment.

---

## Multiple households

A user may belong to multiple households.

At any given time:
- exactly one household can be used for scoped operations
- the active household is resolved server‑side from membership data

If the user has multiple households, active household selection is required
(not implemented yet).

---

## Invitations

Households may invite new members.

Invitations:
- are token-based
- are time-limited
- grant membership upon acceptance

## Decisions

Decision: Invitations are a sub-aggregate within Household (Phase 1), not a separate feature.
Rationale: Invitations are a household-scoped onboarding tool, not an independent domain.
Consequences: Invitation logic stays in Household; no separate repositories or APIs outside the feature.
Future: May be extracted if resend, tracking, or pending-user workflows become core.

## Invitation Model (Phase 1)

### Decision

Identity: invitationId  
Token: random value used only for acceptance flow  
Ownership: only OWNER may create/revoke  
Expiry: default TTL applied if not provided  
Status: ACTIVE | REVOKED | EXPIRED  
Duplicate rule: only one ACTIVE invitation per email per household  
Expiry evaluation: EXPIRED is implicit when status is ACTIVE and now > expiresAt

### Rationale

invitationId separates domain identity from delivery mechanism  
token can be rotated/resend without changing identity  
ACTIVE constraint prevents inconsistent onboarding state

### Consequences

Revoke operates on invitationId  
409 returned when ACTIVE invitation already exists  
Future resend flow can reuse invitationId but regenerate token  
Expiry evaluation can be derived from expiresAt + status logic

---

## Invitation Lifecycle (Domain)

At the domain level, invitations follow a simple lifecycle:
- `PENDING` → `ACCEPTED` when a valid invitation is accepted
- `PENDING` → `EXPIRED` when the invitation has passed its expiry time

Invitations are time‑limited, and acceptance of a valid invitation creates a household membership.

---

## Design rationale

Separating users from households:
- enables shared ownership
- avoids data silos
- reflects real-life relationships

---

## Summary

Households own data.
Users participate via memberships.

This separation is foundational to LifeLinq.
