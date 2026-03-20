# UI Foundation

## Scope & Authority

This document governs visual interface construction for LifeLinq.

It is the visual system and UI construction rules document for LifeLinq.

UX principles define:
- product philosophy
- interaction intent
- behavioral direction

UI foundation defines:
- visual implementation rules
- surface behavior rules
- hierarchy rules
- state communication rules
- screen and component pattern rules

Both are authoritative for UI work and must be followed together.

Reference:
- `docs/design/ux-principles.md`
- `docs/design/ui-reference-framework.md`

This document is normative for all visual decisions.

Use this document when deciding how screens, cards, states, hierarchy, and component-level UI should be visually built.

---

## 1. Design Character

LifeLinq UI must feel:
- calm
- human
- warm
- lightweight
- modern
- steady
- household-close

Visual tone must support trust, repeated daily use, and low-friction action.

The interface must not feel:
- technical
- administrative
- enterprise-like
- utility-heavy
- overly decorative
- over-labeled
- dashboard-heavy

Modern does not mean flashy.  
Minimal does not mean vague.  
Warm does not mean playful.

LifeLinq should feel clear, quiet, and intentional.

---

## 2. Surface System

LifeLinq uses a restrained two-layer surface model:
- app background surface
- primary content/card surface

Rules:
- background surface stays quiet and neutral
- primary surfaces carry content, workflow, and interaction
- contrast must create clarity without harsh separation
- surfaces should support calm orientation, not visual fragmentation

Suggested reference:
- background surface → warm neutral
- card/content surface → clean light surface

These are guidance values, not hardcoded requirements.

Additional rules:
- cards should create structure, not clutter
- not every subsection needs its own card
- avoid “box inside box” layering unless interaction clearly requires it
- special states should usually be expressed through light variation, not extra heavy containers

---

## 3. Surface Roles

LifeLinq should use a small number of clear surface roles.

### 3.1 Workspace Surfaces
Purpose:
- orient the user
- frame a feature area
- support browsing, switching, and reuse

Rules:
- should feel calm and content-first
- should support overview, not management overload
- controls may be present, but must not overpower the main content

### 3.2 Content Surfaces
Purpose:
- present primary readable or usable content
- support understanding, scanning, and reuse

Rules:
- content leads
- actions support
- metadata stays secondary
- avoid top-heavy chrome or summary blocks that push content down unnecessarily

### 3.3 Action Surfaces
Purpose:
- support direct user action or repeated entry
- host active workflows such as input, editing, or list interaction

Rules:
- action hierarchy must be clear
- frequent actions should feel fast and low-friction
- action density is acceptable only if the flow remains scannable and calm

### 3.4 Support Surfaces
Purpose:
- provide lifecycle controls, secondary metadata, supporting context, or less-frequent controls

Rules:
- must remain visually secondary
- must not compete with the main task
- must not become the real center of a content-first screen

---

## 4. Feature Identity & Accent

Each feature may use one accent family for recognition and continuity.

Required mapping:
- todos → calm task-oriented accent
- meals → warm planning accent
- shopping → practical action accent
- economy → financial-state accent
- documents → archive/reference accent

Suggested palette reference:
- todos → green
- meals → orange
- shopping → blue
- economy → purple
- documents → neutral gray

Rules:
- accent is supportive, not dominant
- accent must remain consistent inside a feature
- accent must never carry hierarchy by itself
- accent must never replace structure, spacing, or typography
- accent should support recognition, emphasis, and continuity — not become decoration

Accent usage is strongest when it:
- improves orientation
- supports a small number of key signals
- connects related surfaces across the same feature

Accent usage is weakest when it:
- decorates every element
- communicates state on its own
- substitutes for hierarchy

---

## 5. Spacing & Rhythm

LifeLinq uses an 8px grid spacing system.

Base unit:
- 8px

Common steps:
- 8px → tight spacing
- 16px → default spacing
- 24px → section spacing
- 32px → large separation

Rules:
- spacing must remain rhythmically consistent
- spacing must communicate grouping, not only visual neatness
- predictable spacing improves scanability and trust
- denser spacing is acceptable inside high-frequency editing surfaces if clarity remains strong
- looser spacing should separate distinct mental groups, not just create visual air

Avoid:
- ad hoc spacing values outside the system
- uneven rhythm that makes hierarchy unclear
- using spacing as decoration instead of meaning

---

## 6. Corner Radius & Elevation

LifeLinq surfaces use soft corners and subtle depth.

Rules:
- standard card radius is soft and consistent
- shadows/elevation must remain subtle
- depth is used for clarity of layering, not for effect
- surfaces should feel grounded and quiet, not glossy or floating-dashboard-like

The interface must remain light and calm.

---

## 7. Typography & Copy Discipline

UI text must follow a clear hierarchy:
- screen title
- section or card title
- primary body text
- secondary/supporting text

Rules:
- hierarchy must be visually obvious
- size and weight changes must be purposeful
- secondary text must remain readable but clearly de-emphasized
- copy should orient and support action, not over-explain the interface

Additional rules:
- secondary text must earn its place
- helper text should not repeat what layout, labels, or state already make obvious
- the cleanest hierarchy often comes from removing text, not styling more text
- headings should orient; they should not carry unnecessary narrative or instruction

Avoid:
- stacked microcopy
- duplicate helper lines
- top-of-screen explanation that the UI could carry through structure

---

## 8. Iconography & Affordance

Icons should support recognition, scanability, and quick orientation.

Rules:
- icons must support labels, not replace them for core actions
- icon meaning must stay stable across the product
- decorative-only icon use is not allowed
- icon-only actions are acceptable only when meaning is already highly obvious from context or repeated product use
- icons should improve clarity, not create ambiguity

Affordance rules:
- the user should understand what is tappable from structure, placement, and interaction treatment
- action state must be clearer than decorative styling
- small actions must still feel intentional and touch-safe
- subtle is good, but ambiguity is not

---

## 9. Screen & Surface Patterns

LifeLinq should use predictable visual structures, but different surface types require different hierarchy.

### 9.1 Workspace Screens
Typical order:
1. orientation / top bar
2. primary workspace controls if needed
3. main content body
4. secondary support or lifecycle areas

Rules:
- content must remain the center
- controls should support navigation and decisions, not dominate the page
- workspace surfaces must not feel like control panels

### 9.2 Content / Detail Screens
Typical order:
1. orientation
2. main content
3. contextual actions
4. metadata / secondary support

Rules:
- readable or usable content leads
- metadata must not visually compete with main content
- avoid summary-heavy tops if they push meaningful content down

### 9.3 Editor Screens
Typical order:
1. orientation
2. primary authoring content
3. secondary metadata/configuration
4. lifecycle controls last

Rules:
- editing should feel rhythmic and low-friction
- high-frequency sections should come earlier
- metadata and lower-priority fields should be visually demoted
- editors must not feel like administrative forms

### 9.4 Review Screens
Typical order:
1. orientation / light trust framing
2. reviewable content
3. resolvable states/actions
4. supporting provenance or metadata

Rules:
- review should feel assistive, not alarming
- content should feel nearly ready
- states should be easy to inspect and resolve
- avoid validation-tool or parser-tool aesthetics

---

## 10. State Communication

State should be communicated with as little noise as possible.

Rules:
- prefer fewer stronger signals over many weak layered signals
- prefer context-carried state over explicit labeling when possible
- prefer structure, emphasis, and affordance before extra explanatory text
- special states should usually feel like light variation, not like a different product surface
- resolved or completed states should feel clearly done, but not loud

Examples of good state communication:
- changed layout emphasis
- clear action state
- one visible progress/status signal when it changes a decision
- subtle row treatment that still remains obvious

Avoid:
- badge + helper text + summary text + background treatment all saying the same thing
- labels for states that are already obvious from context
- visually dominant secondary states
- warning-heavy treatments for normal review flows

---

## 11. Editor UI Principles

Editors in LifeLinq should feel:
- calm
- fast
- low-friction
- continuation-friendly
- lightweight rather than form-heavy

Rules:
- optimize for rhythm, not only correctness
- frequent repeated tasks should support continuation
- active editing states may expose more structure, but should not become visually noisy
- inactive states should remain compact and calm
- metadata should remain secondary to primary authoring content
- repeated input flows should minimize tap cost and cognitive interruption

Editors should not feel like:
- dense admin forms
- generic enterprise configuration panels
- heavy blocks stitched together without flow

---

## 12. Review UI Principles

Review surfaces should feel:
- calm
- trustworthy
- nearly ready
- assistive
- resolvable

Rules:
- review items must be easy to inspect and resolve
- the user should be able to tell what still needs attention
- completion should feel clear without becoming celebratory or loud
- imported or inferred content should feel close to usable, not like raw machine output
- review should minimize cognitive burden, not expose system uncertainty more than necessary

Avoid:
- passive warning-only states
- unclear review affordances
- utility-box styling for ordinary review
- excessive explanation of system uncertainty
- making the user feel they must repair everything before they can proceed

---

## 13. Home Pattern

Home must follow this structural order:
1. place header
2. today summary
3. quick actions
4. feature modules

Rules:
- order must remain stable
- primary daily orientation appears before navigation depth
- modules must be scannable and balanced

### Home Is Not a Dashboard
Home is an orientation surface, not a dashboard.

Its role is:
- quick orientation
- fast entry to actions
- light contextual awareness

Detailed management belongs inside feature screens.

Rules:
- Home must not accumulate dense operational detail
- Home should summarize, not manage
- Home should direct users into features rather than replace them

### Feature Module Structure
Feature modules on Home must be implemented as card-based entry points.

Each module must include:
- feature icon
- feature title
- short contextual status text
- navigation affordance

Rules:
- feature modules are entry points, not dashboards
- module content must support quick recognition and navigation

### Feature Identity Continuity
Feature identity must remain visually continuous from Home module to feature screen.

Each feature may carry subtle identity through:
- icon treatment
- highlight color
- summary/status accent
- primary CTA emphasis

Rules:
- continuity must improve orientation and recognition
- accent identity must remain supportive, not dominant
- feature screens must feel connected to their Home entry point

---

## 14. Visual Restraint

LifeLinq favors calm minimal UI over decoration.

Rules:
- avoid visual noise
- avoid unnecessary ornamentation
- prioritize legibility, spacing, hierarchy, and action clarity
- use fewer stronger elements instead of many weak ones
- quiet UI should still feel deliberate and modern, not plain by accident

Restraint means:
- less clutter
- clearer emphasis
- more confident hierarchy

Restraint does not mean:
- vague structure
- weak affordance
- flatness without purpose

---

## 15. Current Internal UI References

These are current internal reference surfaces for LifeLinq.

### 15.1 Meals Plan week view
Reference for:
- workspace shell
- control/content separation
- calm overview hierarchy
- content-first feature screen structure

### 15.2 Saved recipe read mode
Reference for:
- content-first detail/read surfaces
- low-chrome hierarchy
- quiet section rhythm
- actions staying secondary to content

### 15.3 Recipes overview
Reference for:
- sibling workspace parity
- feature-level overview shell
- controls supporting content without dominating it

These references are not permanent truth.
They are the current best internal examples and should guide near-term frontend decisions.

---

## 16. Consistency Rule

Consistency is a product requirement, not a style preference.

Consistency means more than:
- shared colors
- shared spacing
- shared radii

It also requires consistency in:
- hierarchy
- surface roles
- action placement
- state communication
- editor rhythm
- review treatment
- visual restraint
- feature continuity

Rules:
- reuse established surfaces, spacing, typography, and card logic
- new patterns require explicit justification
- feature-level variation must not break global product coherence
- different surface types may differ, but they must still feel like the same product family

A screen is inconsistent not only when it looks different, but when it behaves with a different visual logic than the rest of LifeLinq.

---

## 17. Anti-Patterns

LifeLinq should avoid:
- box inside box inside box
- over-labeled top sections
- helper text that repeats visible context
- badge-heavy state communication
- toolbar + list layouts when content should lead
- visually loud secondary states
- utility-style review blocks in calm household flows
- actions visually overpowering content on content-first surfaces
- dense management UI in everyday use flows
- local visual cleverness that breaks feature continuity
- using accent color as a substitute for real hierarchy
- adding summary or status UI that does not change user decisions

---

## 18. Decision Priority for UI Tradeoffs

When UI rules compete, prioritize in this order:

1. preserve calm content hierarchy
2. keep the next action obvious
3. reduce visual noise
4. maintain product-family consistency
5. preserve feature identity without overusing accent or chrome

If a solution feels richer or more expressive but makes the surface heavier, more tool-like, or less coherent with the rest of LifeLinq, it is usually the wrong solution.
