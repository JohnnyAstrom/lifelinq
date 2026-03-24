# AI Collaboration Workflow

## Purpose

This document defines how LifeLinq feature work is carried out across:
- the human product owner
- ChatGPT
- Codex

It exists to make the collaboration more consistent, more efficient, and less prone to vague iteration loops.

This document is about **working method**.
It is not a replacement for:
- architecture docs
- design docs
- feature docs
- roadmap docs

`AGENTS.md` holds the short binding repo-wide rules.
This document explains the operational working method in more detail.

Use this document together with:
- `AGENTS.md`
- `docs/design/ux-principles.md`
- `docs/design/ui-foundation.md`
- `docs/design/ui-reference-framework.md`

---

## Roles

### User
The user provides:
- product goals
- screenshots
- judgments about what feels right or wrong
- priority signals
- fast reality checks on whether a step is good enough to leave

The user should not have to perform deep repetitive validation for every small step.

### ChatGPT
ChatGPT acts as:
- product and UX analyst
- architecture-aware scope guide
- prompt writer for Codex
- reviewer of whether the result is product-right, not just technically acceptable

ChatGPT should:
- place each problem in the right product context
- decide whether the next step is analysis, implementation, review/polish, later V2.x, or V3/final direction
- keep scope disciplined
- prefer medium by default, and explicitly recommend high only when it is truly needed

### Codex
Codex acts as:
- codebase analyst
- implementation engine
- autonomous validator of the concrete change

Codex should:
- respect the requested task mode
- stay inside the agreed slice
- validate the implementation as much as possible before handoff
- be explicit about what changed, what was checked, and what stayed unchanged

---

## Core working model

Work iteratively:

1. The user describes a problem, goal, or shows screenshots.
2. ChatGPT places it in the right product and roadmap context.
3. ChatGPT decides whether the next step is:
   - read-only analysis
   - implementation
   - review/polish
   - later V2.x
   - V3/final direction
4. If needed, ChatGPT writes a Codex prompt.
5. Codex responds with analysis or implementation.
6. The user tests lightly where needed and/or provides screenshots.
7. ChatGPT reviews the result product-wise and decides:
   - leave it
   - do a pass 2
   - polish later
   - reopen the direction

---

## Program-based delivery model

When an area is mature enough that many small UI-led slices would become inefficient, work should shift from feature-by-feature iteration to capability-program delivery.

A capability program should usually be executed in this order:

1. read-only program definition
   - product purpose
   - program boundaries
   - domain model
   - key data concepts
   - API/contract map
   - delivery recommendation

2. backend/data/API foundation
   - durable domain entities
   - lifecycle/state semantics
   - scenario-based contracts
   - derived read models/projections

3. larger frontend/product slice
   - one or two meaningful user-facing slices built on the new foundation

4. stabilization pass
   - edge cases
   - contract cleanup
   - frontend/domain seam reduction
   - final bounded coherence pass

This model should be preferred over many tiny feature steps when the product area already has enough maturity to support larger capability work.

---

## Task modes

### Program definition
Use this when:
- a larger capability area needs to be defined before implementation
- domain model / API shape / program boundary are not yet locked
- working feature-by-feature would create fragmented execution

Rules:
- no implementation
- no code changes
- focus on product purpose, boundaries, domain model, API shape, and execution order

### Backend/data/API foundation
Use this when:
- a capability direction is already chosen
- the next correct step is to build durable model/contract support before frontend work
- later frontend slices depend on stable backend semantics

Rules:
- prioritize domain model, lifecycle/state semantics, scenario-based contracts, and read models
- do not widen into multiple adjacent product programs
- do not redesign frontend flows yet unless minimally necessary for compatibility

### Frontend/product slice
Use this when:
- a foundation already exists
- the goal is to build one or two meaningful user-facing slices on top of it
- the user-facing product move is clear enough to implement

Rules:
- stay inside the agreed program slice
- build on the existing foundation instead of reopening model questions
- preserve unchanged behavior unless the step explicitly changes it

### Stabilization pass
Use this when:
- the main capability slice is already in place
- the remaining work is seam cleanup, edge cases, contract friction, or final coherence
- the goal is to close a capability program cleanly

Rules:
- do not turn stabilization into new feature work
- focus on meaningful skavers, not low-value polish
- prefer one bounded cleanup pass over many tiny polish loops

### Small direct fix
Use this when:
- the solution is already known
- the change is small and concrete
- ambiguity would create wasted iteration

Rules:
- keep the instruction short
- keep the goal explicit
- avoid broad interpretive prompts

---

## Step sizing and slice discipline

Work on one meaningful bounded delivery at a time.

A good delivery:
- produces a clear product improvement
- is narrow enough to reason about
- is large enough to matter
- does not silently absorb the next roadmap step

For mature areas, a “slice” does not need to be tiny.
It may instead be:
- a backend/data/API foundation slice
- a larger frontend/product slice
- a stabilization pass

When direction is already clear, prefer:
- fewer, more meaningful bounded deliveries
over
- many tiny half-steps

For small direct fixes:
- keep the instruction short
- keep the goal explicit
- avoid broad interpretive prompts

Do not stay in small polish loops once the area is mature enough for capability-program work.

---

## Prerequisite-first planning

If a later-visible feature depends on a missing prerequisite, solve the prerequisite cleanly first.

Examples:
- servings data before portion scaling
- usage signal before recently used UI
- import completeness before stronger recipe scaling behavior

Do not hack around missing structure just to reach the visible feature faster.

When a prerequisite question exists, prefer a focused read-only analysis first.

---

## Prompt patterns

### Program definition prompt
Use when:
- a larger capability area needs to be defined before implementation
- domain model, API shape, and program boundaries are not yet locked

Should include:
- Goal
- Context
- Important boundaries
- domain/API/data questions
- explicit output format
- delivery recommendation

### Backend/data/API foundation prompt
Use when:
- the capability direction is chosen
- the next correct step is to build durable model/contract support before frontend work

Should include:
- Goal
- Context
- Important boundaries
- explicit domain/API scope
- lifecycle/state expectations
- read model / projection expectations
- output and verification structure

### Frontend/product slice prompt
Use when:
- the foundation is already defined
- the next step is a meaningful user-facing slice on top of it

Should include:
- Goal
- Context
- Important boundaries
- explicit implementation scope
- what should remain unchanged
- output and verification structure

### Stabilization pass prompt
Use when:
- the main capability slice is already in place
- the remaining task is bounded cleanup of seams, edge cases, and friction

Should include:
- Goal
- Context
- Important boundaries
- specific stabilization targets
- what should not expand into new feature scope
- output and verification structure

### Small direct fix prompt
Use when:
- the solution is already known
- the change is small and concrete
- ambiguity would create wasted iteration

Should be:
- short
- explicit
- low-interpretation

Use this especially for:
- tiny UI fixes
- layout corrections
- exact behavior fixes
- wording changes where the desired output is already known

---

## Validation model

### Codex autonomous validation
Codex should validate as much as possible itself before handoff.

Examples:
- typecheck/build
- unchanged flow integrity where relevant
- state behavior that can be reasoned about locally
- logic correctness for the bounded change
- obvious regression-sensitive paths

### Human check
Human validation should stay:
- short
- relevant
- easy to understand

Human checks should focus on:
- product fit
- visual hierarchy
- flow naturalness
- whether the main intended improvement is actually felt

Avoid requiring long manual validation lists unless the change truly demands it.

### What stayed unchanged
Implementation handoff should make clear:
- what changed
- what the human should check
- what stayed unchanged

This reduces unnecessary retesting.

---

## Screenshot-first review for UI/UX work

For UI-heavy work, screenshots are first-class evidence.

Use screenshots especially for:
- visual hierarchy
- content-vs-controls balance
- chrome weight
- calmness
- read mode maturity
- workspace structure
- empty/no-results states
- support-vs-primary action balance

When screenshot review is the primary need, human testing should stay light.

The human should not have to deep-test the whole feature when the real question is visual/product fit.

---

## Medium vs High

### Medium is the default
Use medium for:
- standard product judgments
- screenshot/UI review
- bounded read-only analysis
- small or medium implementation prompts
- parity/polish
- sequential next-step decisions
- stabilization passes when the scope is already clear

### High is for larger decisions and program-level work
Use high when:
- multiple major directions compete
- a new capability program must be defined
- a backend/data/API foundation needs to be planned
- scope ordering is risky if analyzed too narrowly
- a larger implementation prompt could go wrong without stronger upfront reasoning
- a capability slice is large enough that weak framing would create rework

Do not overuse high.
But for program-definition work and foundation work, high is often the correct default.

---

## When a step is good enough to leave

A step is good enough to leave when:
- the main intended product value is present
- the direction feels right
- remaining issues are secondary polish
- further iteration would likely produce diminishing returns

At review time, explicitly decide between:
- leave it now
- do a pass 2
- polish later
- reopen the direction

For capability programs, a program is good enough to leave when:
- the foundation exists
- at least one meaningful frontend/product slice is using that foundation
- the main seams have been reduced in a stabilization pass
- the area is coherent enough that it does not require continued small-duttande

Do not keep iterating on a step or program out of habit once the main value is already there.

---

## What to avoid

Avoid:
- widening a step into adjacent roadmap work
- using broad prompts for tiny direct fixes
- requiring deep manual testing for small UI iterations
- turning polish into unbounded redesign
- solving visible features by hacking around missing prerequisites
- making every workflow detail live inside one giant top-level file
- staying in small polish loops after the area is mature enough for capability-program work
- using frontend discovery as a substitute for missing domain/API definition
- mixing multiple capability programs into one vague implementation step
- treating platform/foundation work as optional when later frontend depends on it
- building many half-finished frontend entry points before the shared model is clear
