# INVARIANTS

This document defines the **non-negotiable rules** of the system.

These invariants must hold true regardless of:
- feature growth
- refactors
- technology changes
- tooling (including AI assistance)

If a design or implementation violates an invariant, **the design is wrong**.

---

## 1. Group is the primary context

- All domain data belongs to a **Group**, not to a User.
- Users may belong to one or more groups.
- Users never own domain data directly.

**Implication:**
- Every domain operation is executed within a group context.
- Group context is always derived server-side.

---

## 2. Clients are untrusted

- The backend is the single source of truth.
- Clients (mobile/web) may never:
  - choose a group
  - enforce business rules
  - bypass validation
- All authorization and scoping is enforced server-side.

**Implication:**
- Clients send intent, never authority.
- JWT claims define access, not request payloads.

---

## 3. Group context is derived server‑side

- Clients never send `groupId` explicitly in requests.
- Group context is resolved server‑side from membership data.
- If group context is missing or ambiguous, scoped endpoints are rejected.

**Implication:**
- JWT does **not** need to carry group context.
- Active group selection is a separate, explicit step (not implemented yet).

**Transitional note (temporary):**
- Some endpoints still accept `groupId` in the URL path.
- This is temporarily allowed only when **validated against server‑derived context**.
- The long‑term goal is full server‑derived scoping with no client‑provided groupId.

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
- “Documents” prioritize retrievability over structure.

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

---

## 14. A group must always have an owner

- A group cannot be left without an OWNER.
- Removing the last OWNER is forbidden.
