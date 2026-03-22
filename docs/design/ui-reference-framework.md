# UI Reference Framework

## Purpose

This document is a practical decision framework for frontend and product UI work in LifeLinq.

It is built on:
- `docs/design/ux-principles.md`
- `docs/design/ui-foundation.md`

It is not a replacement for:
- `docs/design/ux-principles.md`
- `docs/design/ui-foundation.md`

It exists to make those documents easier to apply in real feature work.

Use this document when:
- reviewing screenshots
- planning UI changes
- deciding whether a screen feels “LifeLinq-right”
- writing Codex prompts for frontend work
- judging whether a UI problem is real UX debt or only polish

This document is especially written to reduce vague UI iteration loops.

Use it after the core design documents when reviewing screenshots, defining UI change direction, or writing Codex/frontend prompts.

---

## 1. Core Product UI Direction

LifeLinq should feel:

- calm
- household-close
- low-friction
- trustworthy
- modern
- content-first
- human
- quietly useful

LifeLinq should not feel:

- enterprise-like
- dashboard-heavy
- admin-like
- tool-first
- over-labeled
- warning-heavy
- feature-showy
- technically clever at the expense of clarity

When in doubt, choose the option that makes the surface feel:
- easier to use
- easier to scan
- less system-aware
- more obviously useful
- less cognitively demanding

---

## 2. The Primary UI Decision Rule

When a UI decision is unclear, prefer this order:

1. preserve meaning
2. reduce cognitive load
3. make the next step obvious
4. keep the surface calm
5. keep the product family consistent

This rule should override local cleverness.

If a solution is:
- richer
- smarter
- more expressive
- more “advanced”

but also:
- heavier
- noisier
- more technical
- more admin-like
- less obvious

then it is usually the wrong solution for LifeLinq.

---

## 3. Surface-Type Decision Model

Do not judge every screen by the same standards.

Different surfaces should be evaluated differently.

### 3.1 Workspace Surfaces
Examples:
- Meals Plan
- Recipes overview
- Shopping overview
- Todo overview

These should feel:
- orienting
- calm
- content-first
- easy to scan
- not like control panels

Ask:
- does content lead?
- do controls support rather than dominate?
- is the screen a place to use, not a place to manage?
- does the surface feel closer to a calm household workspace than a productivity dashboard?

### 3.2 Detail / Read Surfaces
Examples:
- saved recipe read mode
- content-heavy detail views

These should feel:
- content-first
- low-chrome
- trustworthy
- easy to read
- not over-managed

Ask:
- is the content clearly the main thing?
- are actions secondary?
- is metadata demoted?
- is the top quiet enough?

### 3.3 Editor Surfaces
Examples:
- create recipe
- edit recipe
- add/edit flows in sheets

These should feel:
- rhythmic
- fast enough for repeated use
- lightweight
- not like admin forms

Ask:
- does the editor support continuation?
- does repeated entry feel smooth?
- are metadata and lower-priority fields visually secondary?
- does the editor feel like everyday authoring, not configuration?

### 3.4 Review Surfaces
Examples:
- imported recipe review
- flagged item flows
- any “check this before saving” state

These should feel:
- assistive
- nearly ready
- easy to resolve
- calm
- not technical

Ask:
- does review feel like help, not process?
- is the state actionable and finishable?
- is the user told too much, or just enough?
- does the system expose too much uncertainty?

### 3.5 Empty States
Examples:
- empty lists
- empty libraries
- no-content states

These should feel:
- natural
- expected
- possibility-oriented
- lightweight

Ask:
- does this feel like a starting point rather than a failure?
- does the copy create calm possibility rather than absence anxiety?

---

## 4. Current Internal Reference Surfaces

These are current internal examples of “most right so far.”

They are not permanent truth.
They are the best current working references.

### 4.1 Meals Plan week view
Use as reference for:
- workspace hierarchy
- control/content separation
- calm feature overview structure
- content-first workspace screens

Why it works:
- clear orientation
- controls are present but secondary
- content remains the focus
- the screen feels usable, not managed

### 4.2 Saved recipe read mode
Use as reference for:
- content-first detail/read surfaces
- low-chrome content presentation
- quiet section rhythm
- secondary action placement

Why it works:
- content leads
- actions do not dominate
- metadata stays secondary
- the screen feels mature and trustworthy

### 4.3 Recipes overview
Use as reference for:
- sibling workspace structure
- parity between two subspaces in one feature
- calm control/header + content body relationship

Why it works:
- clear structure
- content leads
- actions are visible but subordinate
- it feels like the same family as Plan

---

## 5. External Direction Calibration

These are not copy targets.
They are directional references.

### 5.1 Workspace overview direction
Primary direction:
- closer to **AnyList** than **Samsung Food**

Meaning:
- lower-friction
- less platform-heavy
- less dashboard-like
- more household-useful
- less feature-showy

### 5.2 Detail/read direction
Primary direction:
- closer to **Paprika** than **Samsung Food**

Meaning:
- more content-first
- lower chrome
- more recipe/library calmness
- less system-presence over content

### 5.3 Editor direction
Primary direction:
- between **AnyList** and **Paprika**
- clearly not Samsung Food as the main model

Meaning:
- faster household editing rhythm
- recipe-aware, but not pro-tool heavy
- lightweight authoring, not structured form machinery

### 5.4 Review/state direction
Primary direction:
- closer to **AnyList/Paprika** than **Samsung Food**

Meaning:
- review should feel assistive and resolvable
- not like a product process layer
- not like a warning/validation environment
- not like the app is more visible than the content

---

## 6. UI Heuristics for Real Decisions

These heuristics should be used directly in reviews and prompts.

### 6.1 Fewer stronger signals beat many weak ones
Do not stack:
- badges
- helper text
- summary text
- icons
- background changes

when one or two clear signals are enough.

### 6.2 Context beats labels
If the surrounding UI already makes the state obvious, do not add more labels.

### 6.3 Affordance beats explanation
If layout and action treatment already tell the user what to do, do not add explanatory copy.

### 6.4 Content should lead where content is the point
Do not let controls, summaries, or metadata visually dominate content-first screens.

### 6.5 Review must be finishable
Review state is not just “something is marked.”
The user must be able to:
- inspect
- resolve
- continue
- feel done

### 6.6 Editors must optimize for rhythm
A frequent editing task should feel like a flow, not a set of separate form stops.

### 6.7 Preserve meaning over forced structure
If the system is uncertain, keep the user’s meaning intact rather than producing cleaner but misleading output.

### 6.8 Calm beats clever
Do not introduce local smartness that makes one screen look improved while making the product feel less coherent.

### 6.9 Status must earn its place
If a summary or status element does not change a user decision, it probably should not be prominent.

### 6.10 Secondary state must stay secondary
Archived, review, error-adjacent, or support states must not visually hijack the main workflow unless the situation is truly critical.

---

## 7. Common Problems to Diagnose

Use these labels when reviewing a UI problem.

### 7.1 Workspace hierarchy problem
The screen is too control-heavy, too summary-heavy, or too management-like.

### 7.2 Content-vs-controls balance problem
The controls are competing too much with what the user actually came to use or read.

### 7.3 State communication problem
The UI is using too many signals, weak signals, or unclear signals to express state.

### 7.4 Utility-feel problem
The surface is clean and functional, but feels generic, mechanical, or less productized than it should.

### 7.5 Over-labeling problem
The screen relies too much on helper text, badges, or explanatory labels for things the UI should carry.

### 7.6 Boxiness problem
The screen is becoming “box inside box inside box” and losing calmness.

### 7.7 Form-heaviness problem
The editor or input flow feels too much like structured data entry and too little like natural everyday use.

### 7.8 Library identity problem
A list or overview no longer makes entries distinct enough as the content set grows.

### 7.9 Review-process problem
A review flow feels too much like system process or validation instead of a lightweight assistive check.

### 7.10 Polish-only issue
The UI is functionally and structurally right, and the remaining issue is mostly visual refinement.

---

## 8. Anti-Patterns

These should be treated as warning signs in reviews.

### 8.1 Over-labeled tops
A top section uses too many:
- labels
- subtitles
- badges
- helper lines
to explain state that should already be clear.

### 8.2 Badge for obvious context
A badge repeats what the user already knows from the screen they are in.

### 8.3 Toolbar + list drift
A screen becomes a toolbar with content underneath, when the content should clearly lead.

### 8.4 Gray utility blocks
Normal household review or editing states are presented as tool-like utility containers.

### 8.5 Stacked weak state signals
A screen uses multiple low-confidence signals instead of one clear one.

### 8.6 Box-inside-box-inside-box
Cards and subcards create fragmentation instead of hierarchy.

### 8.7 Actions overpower content
Primary content is visually weaker than secondary actions or controls.

### 8.8 Summary without decision value
A summary/status element exists mainly because the state exists, not because the user needs it.

### 8.9 Local visual cleverness
A screen introduces a unique visual trick that breaks product-family consistency.

### 8.10 Technical exposure
The UI exposes system uncertainty, structure, or machine output too directly.

---

## 9. How to Review a Screen

Use this checklist when reviewing screenshots or proposed UI changes.

### 9.1 What surface type is this?
Choose one first:
- workspace
- detail/read
- editor
- review
- empty state

Do not judge it with the wrong model.

### 9.2 What should lead here?
Identify whether the screen should be led by:
- content
- action
- review state
- orientation

If the wrong thing is leading, the hierarchy is likely wrong.

### 9.3 Is the next step obvious?
If the user needs explanation to know what to do, the UI probably needs stronger affordance or better structure.

### 9.4 Are we using too many state signals?
If yes, simplify.

### 9.5 Does it feel like LifeLinq?
Ask:
- does it feel calm?
- does it feel household-close?
- does it feel less admin-like than a typical productivity tool?
- does it feel like a place to use, not to manage?

### 9.6 Is this a real UX issue or only polish?
Separate:
- hierarchy problems
- flow problems
- state communication problems
from
- styling-only tweaks

---

## 10. How to Use This with Codex

When writing Codex prompts:
- name the surface type
- name the current problem type
- reference the relevant internal reference surface
- name the anti-pattern to avoid
- name what should lead: content, action, review, or orientation

Good prompt language:
- “This is a workspace hierarchy problem, not a visual polish problem.”
- “Move this closer to our Meals Plan reference and away from toolbar-plus-list behavior.”
- “Reduce stacked weak state signals.”
- “Keep this content-first and do not let controls dominate.”
- “This review state should feel assistive and resolvable, not tool-like.”
- “Avoid over-labeled top framing.”
- “Preserve meaning over forced structure.”

Avoid vague prompt language like:
- “make it nicer”
- “make it cleaner”
- “improve UX”
- “make it modern”

Those are too open for reliable frontend direction.

---

## 11. UI Review and Prompt Discipline

### 11.1 Screenshots are primary evidence for many UI decisions
For UI-heavy work, screenshots should be treated as first-class review evidence.

They are especially useful for judging:
- workspace hierarchy
- content-vs-controls balance
- chrome weight
- calmness
- action placement
- empty / no-results fit
- whether a surface feels LifeLinq-right

Do not default to heavy manual validation when the real question is visual/product fit.

### 11.2 Use different prompt shapes for different UI tasks
Not all frontend prompts should be shaped the same way.

Use a broader bounded prompt when:
- the surface direction is still unclear
- the main issue is hierarchy, state communication, or product fit
- Codex needs room to solve a real design problem within clear boundaries

Use a short explicit prompt when:
- the desired fix is already known
- the issue is a small direct layout/behavior/wording fix
- interpretive freedom would likely create unnecessary iteration

### 11.3 Human review should focus on the key product signal
When reviewing screenshots, prefer asking:
- what should lead here?
- what still feels wrong?
- does this now feel more calm / content-first / trustworthy?

Do not turn screenshot review into a full manual QA pass when the real decision is product/UI fit.

---

## 12. What This Document Is For

This document exists so that LifeLinq can move from:
- taste-based iteration only
to
- principle-driven UI decisions

It should reduce:
- slow local UI loops
- inconsistent frontend decisions
- repeated rediscovery of the same principles
- vague feedback that is hard for Codex to implement

It should improve:
- prompt clarity
- UI consistency
- confidence in reviews
- alignment between features
- quality of first-pass implementations
