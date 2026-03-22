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

## Task modes

### Read-only analysis
Use this when:
- the right next step is not yet clear
- there are competing directions
- a prerequisite may be missing
- the user is asking what should come next

Rules:
- no implementation
- no code changes
- focus on diagnosis, ranking, and next-step recommendation

### Implementation
Use this when:
- the direction is already chosen
- the slice is clear enough to build
- the goal is a bounded product move

Rules:
- stay within the agreed slice
- do not widen into adjacent roadmap work
- preserve unchanged behavior unless the step explicitly changes it

### Review / polish
Use this when:
- the main direction is already correct
- the remaining issues are refinement, clarity, or fit
- the goal is maturity, not new product scope

Rules:
- do not turn polish into a new feature
- do not reopen adjacent roadmap questions unless a real problem is uncovered

---

## Step sizing and slice discipline

Work on **one meaningful slice at a time**.

A good slice:
- produces a clear user-visible improvement
- is narrow enough to reason about
- is large enough to matter
- does not silently absorb the next roadmap step

When direction is already clear, prefer:
- fewer, more meaningful implementation steps
over
- many tiny half-steps

But for small direct fixes:
- keep the instruction short
- keep the goal explicit
- avoid broad interpretive prompts

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

### Read-only analysis prompt
Use when:
- the next product step is unclear
- a feature area needs diagnosis first
- a prerequisite may exist or be missing

Should include:
- Goal
- Context
- Important boundaries
- specific analysis questions
- explicit output format

### Bounded implementation prompt
Use when:
- the next step is already chosen
- the change should be built now

Should include:
- Goal
- Context
- Important boundaries
- explicit implementation scope
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

### Prerequisite analysis prompt
Use when:
- a later feature depends on unclear underlying support

Goal:
- determine whether the prerequisite already exists
- locate where data/structure is missing or dropped
- recommend the best first move

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
- implementation prompts with clear scope
- parity/polish
- sequential next-step decisions

### High is for larger decisions
Use high when:
- multiple major directions compete
- a new product phase must be defined
- a larger architecture/product framing question is open
- scope ordering is risky if analyzed too narrowly

Do not overuse high.

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

Do not keep iterating on a step out of habit once the main value is already there.

---

## What to avoid

Avoid:
- widening a step into adjacent roadmap work
- using broad prompts for tiny direct fixes
- requiring deep manual testing for small UI iterations
- turning polish into unbounded redesign
- solving visible features by hacking around missing prerequisites
- making every workflow detail live inside one giant top-level file
