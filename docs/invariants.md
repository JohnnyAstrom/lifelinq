# INVARIANTS

This document defines the **non-negotiable rules** of the system.

These invariants must hold true regardless of:
- feature growth
- refactors
- technology changes
- tooling (including AI assistance)

If a design or implementation violates an invariant, **the design is wrong**.

---

## 1. Household is the primary context

- All domain data belongs to a **Household**, not to a User.
- Users may belong to one or more households.
- Users never own domain data directly.

**Implication:**
- Every domain operation is executed within a household context.
- Household context is always derived server-side.

---

## 2. Clients are untrusted

- The backend is the single source of truth.
- Clients (mobile/web) may never:
  - choose a household
  - enforce business rules
  - bypass validation
- All authorization and scoping is enforced server-side.

**Implication:**
- Clients send intent, never authority.
- JWT claims define access, not request payloads.

---

## 3. Household context is derived from authentication

- Clients never send `householdId` explicitly in requests.
- Household context is extracted from the authenticated token.
- If household context is missing or invalid, the request is rejected.

**Implication:**
- JWT always includes household context when required.
- Switching households requires issuing a new token.

---

## 4. Features are isolated by domain boundaries

- Each feature owns its domain data and rules.
- Features may not directly modify another feature’s data.
- Cross-feature interactions must go through:
  - explicit service boundaries, or
  - domain events / orchestration logic.

**Implication:**
- No “reach-through” access across features.
- Avoid shared mutable state between features.

---

## 5. Backend owns all business rules

- All domain rules live in the backend.
- Frontend logic is limited to:
  - presentation
  - user interaction
  - request orchestration
- No business-critical decisions are duplicated client-side.

**Implication:**
- Backend logic must be testable without UI.
- Frontend remains replaceable.

---

## 6. Simplicity is a feature

- Prefer simple models over generalized abstractions.
- Avoid introducing indirection without a concrete need.
- Build for current, validated requirements — not hypothetical future ones.

**Implication:**
- It is acceptable to refactor later.
- It is not acceptable to over-engineer early.

---

## 7. Domain concepts reflect real life, not idealized workflows

- Todos are simple items, not projects.
- Shopping is driven by needs, not plans.
- “Important things” prioritize retrievability over structure.

**Implication:**
- The system must tolerate incomplete and messy data.
- UX and domain models should reduce mental load, not increase it.

---

## 8. Technology choices must not leak into the domain

- Domain models must not depend on:
  - frameworks
  - databases
  - transport protocols
- Infrastructure implements domain interfaces, not the other way around.

**Implication:**
- Technology can change without rewriting the domain.
- Domain logic remains stable over time.

---

## 9. Correctness beats cleverness

- Clear code is preferred over compact code.
- Explicit behavior is preferred over implicit magic.
- Debuggability is a first-class concern.

**Implication:**
- The system should be understandable by humans first.
- Tooling and automation are aids, not decision-makers.

---

## 10. These invariants evolve slowly and deliberately

- Changes to this document require conscious review.
- Invariants are not updated to “fit the code”.
- Code is updated to fit the invariants.

---

## 11. Documentation is part of the system

- Documentation is a first-class artifact.
- Any change that affects behavior, structure, or rules must be reflected in the documentation.
- Code and documentation must evolve together.

**Implication:**
- A change is not complete if the documentation is outdated.
- If code and documentation disagree, the documentation must be updated or the change reverted.

---

## 12. Refresh tokens are server‑side and rotation‑safe (future)

When authentication is introduced:
- refresh tokens are stored server‑side
- refresh tokens are rotated on use
- reuse detection is enforced to prevent replay

---

## 13. Feature application boundary is explicit

Each feature exposes exactly one `ApplicationService` as its public entry point.
Controllers may only depend on the feature’s `ApplicationService`.
Write use cases must be package‑private and not referenced outside the application layer.
Transactions are defined at the `ApplicationService` boundary.
