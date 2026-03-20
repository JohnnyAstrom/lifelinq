# UX Principles

## Scope & Authority

This document governs user experience decisions only.

It is the product experience and interaction principles document for LifeLinq.

It does not alter:
- domain logic
- API contracts
- data models
- transaction boundaries
- feature ownership

Architecture remains authoritative over system behavior.

UX principles guide:
- presentation
- labeling
- visibility
- interaction
- workflow clarity
- state communication

They do not define backend capability.

This document is normative for all UX-related decisions.

Read it together with:
- `docs/design/ui-foundation.md` for visual implementation rules
- `docs/design/ui-reference-framework.md` for practical review and prompt guidance

Use this document when deciding how the product should feel, what should lead, and how interaction/state should be communicated.

---

## 1. Product Experience Identity

LifeLinq is collaboration-native with frictionless solo entry.

The system is built for shared household coordination, but the experience must always begin personal, calm, and simple.

Architecture may be multi-context.  
Experience must feel single-place-first.

Users should feel:
- this is my place
- this can become ours
- the product helps me act
- the system stays in the background

LifeLinq must never feel:
- multi-tenant
- enterprise-configured
- administrative
- organization-driven
- system-first

---

## 2. Core Mental Model

Internal concepts may include groups, memberships, contexts, and governance structures.

These must not define the user-facing experience.

The UX mental model is:
- place first
- people first
- action first
- structure second

The system supports structure.
It must never become the main character.

Users should not need to understand internal object models to use the product naturally.

---

## 3. Solo-First by Default

If a user has only one space/context:
- no switching UI
- no role exposure
- no administrative labels
- no system explanation
- no visible multi-context scaffolding

Multi-space behavior is revealed only when it becomes relevant to action.

Capabilities should remain latent until the user must make a meaningful decision about them.

---

## 4. Progressive Disclosure of Complexity

Complexity should appear only when it becomes useful.

This applies not only to system structure, but also to:
- actions
- controls
- states
- workflow steps
- configuration depth

Principles:
- latent complexity is better than premature complexity
- do not show system capacity just because it exists
- do not expose advanced controls until they help the user act
- do not force users to process future complexity in present workflows

The user should encounter the next relevant layer, not the full model.

---

## 5. Human-Centered Representation

People are represented as people, not accounts.

Rules:
- names take priority over identifiers
- UUIDs are never shown
- system IDs are never shown
- users should be represented through familiar human identity, not platform identity

The product is person-centered, not account-centered.

---

## 6. Terminology Discipline

Backend and domain terminology must never surface directly in the UI unless explicitly translated into human-facing language.

Avoid:
- group
- membership
- active group
- tenant
- manage group
- internal system vocabulary

Prefer:
- members
- invite someone
- switch space
- human-facing action language

“Space” may be used when labeling is necessary, but implicit context is preferred when the user already understands where they are.

Defaults do not require naming.

---

## 7. Calm Interaction Philosophy

The system assists. It does not instruct unnecessarily.

The product should feel:
- calm
- brief
- human
- low-friction
- self-explanatory where possible

Guidelines:
- prefer obvious next steps over instructional copy
- prefer lightweight guidance over verbose explanation
- prefer confirmation through UI change over success messaging when possible
- reduce user thinking, not just user clicks
- avoid over-teaching the interface

The interface should help the user move forward without feeling coached by the system.

---

## 8. Information Hierarchy

LifeLinq should prioritize:
1. what the user is here to do
2. the content they are acting on
3. the minimum supporting state needed for good decisions
4. system structure last

This means:
- primary content should lead when content is the main purpose of the screen
- actions should be clear, but should not dominate content-first surfaces
- structural or system status should only appear when it changes what the user can do
- secondary states must not overpower the main workflow

The product is a tool for action and use, not a dashboard of system status.

---

## 9. Interaction Heuristics

These rules guide day-to-day UX decisions.

### 9.1 Prefer one strong signal over many weak ones
Do not stack:
- badge
- helper text
- summary text
- icon treatment
- background treatment

when one or two signals are enough.

### 9.2 Prefer context-carried state over explicit labeling
If the surrounding UI already makes state clear, do not repeat it with extra badges or labels.

### 9.3 Prefer affordance over explanation
If users can understand what to do from layout, action placement, and state treatment, do not add explanatory copy.

### 9.4 Frequent actions must optimize for rhythm
If a task is repeated often, optimize for continuation, focus flow, and speed — not only correctness.

### 9.5 Review states must be actionable and resolvable
A review state should not only signal “this needs attention.”
It should make it easy to:
- inspect
- resolve
- continue
- feel done

### 9.6 Preserve meaning over forced smartness
When the system is uncertain, it must prefer preserving user meaning over producing a cleaner but misleading result.

This is especially important in imported or inferred content.

### 9.7 Calm beats clever
Avoid local cleverness that makes one screen feel smarter but the product feel less coherent.

---

## 10. Surface-Type Principles

Different surfaces should optimize for different things.

### 10.1 Workspace surfaces
Workspace views should feel:
- orienting
- calm
- content-first
- easy to scan
- not tool-heavy

They should help users understand:
- where they are
- what they can do next
- what content matters now

They should not feel like admin panels or control dashboards.

### 10.2 Detail and read surfaces
Detail views should feel:
- content-first
- trustworthy
- quiet
- easy to read
- low in chrome

They should not drown content in controls or metadata.

### 10.3 Editor surfaces
Editors should feel:
- low-friction
- rhythmic
- lightweight
- fast for repeated household use

They should optimize for:
- continuation
- editing flow
- natural section order
- low cognitive interruption

They should not feel like dense forms or admin configuration screens.

### 10.4 Review surfaces
Review surfaces should feel:
- calm
- confidence-building
- nearly ready
- assistive
- easy to resolve

They should not feel like:
- parser cleanup
- triage tools
- warning-heavy workflows
- technical validation screens

### 10.5 Empty states
Empty states should feel:
- natural
- expected
- possibility-oriented
- lightweight

They should not imply failure, misconfiguration, or missing setup unless that is actually true.

---

## 11. Feature Visibility Rule

A capability should only be visible when the user must make a decision that depends on it.

Do not expose:
- latent settings
- latent governance
- latent structure
- latent workflow branches

just because they exist in the system.

System capacity is not product value on its own.

---

## 12. Roles & Governance

Roles are governance mechanisms, not identity markers.

Roles should:
- influence available actions
- remain mostly invisible unless relevant
- appear only when they change what the user can do or decide

Roles protect system integrity.
They do not define the person.

---

## 13. Feedback & Confirmation

Feedback must be:
- brief
- calm
- human
- non-technical

Success should often be implicit through state change, visible completion, or continuity.

Avoid verbose success messaging when the interface already makes the outcome clear.

Errors should:
- explain what happened
- suggest the next step when useful
- never blame the user
- reduce anxiety, not increase it

Examples of correct tone:
- “Something went wrong. Please try again.”
- “You need to log in to continue.”
- “You don’t have permission to do that.”

---

## 14. Accessibility & Clarity

Accessibility is not optional.

Interfaces must:
- support keyboard navigation where relevant
- support screen readers
- maintain sufficient contrast
- avoid exclusionary interaction patterns
- keep states understandable without relying on fragile cues alone

Clarity and accessibility are aligned goals.

---

## 15. Current Internal UX References

These are current internal reference surfaces for LifeLinq.

### 15.1 Meals Plan week view
Reference for:
- workspace hierarchy
- control/content separation
- calm overview structure
- content-first feature orientation

### 15.2 Saved recipe read mode
Reference for:
- content-first detail/read surfaces
- low-chrome reading hierarchy
- quiet sectioning
- actions staying secondary to content

### 15.3 Recipes overview
Reference for:
- sibling workspace structure
- overview parity inside one feature family
- controls supporting content instead of dominating it

These references are not permanent truth.
They are the current best internal examples and should guide near-term frontend decisions.

---

## 16. Anti-Patterns

LifeLinq should avoid:
- over-labeled UI
- helper text that repeats what layout already makes clear
- badge-heavy state communication
- enterprise or admin phrasing in normal household workflows
- visually loud secondary states
- tool-like review surfaces for everyday flows
- exposing system structure that does not change decisions
- stacking multiple weak signals when one strong signal would do
- dense management UI where calm usage UI is more appropriate
- aggressive smartness that destroys meaning

---

## 17. Decision Priority for UX Tradeoffs

When UX rules compete, prioritize in this order:

1. preserve meaning
2. reduce cognitive load
3. make the next step obvious
4. keep the surface calm
5. keep the product coherent with existing reference patterns

If a solution feels smarter but increases confusion, technical feel, or workflow heaviness, it is usually the wrong solution for LifeLinq.
