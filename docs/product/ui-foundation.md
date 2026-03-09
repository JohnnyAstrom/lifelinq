# UI Foundation

## Scope & Authority

This document governs visual interface construction for LifeLinq.

UX principles define product philosophy and interaction intent.

UI foundation defines visual implementation rules.

Both are authoritative for UI work and must be followed together.

Reference:

- `docs/ux-principles.md`

This document is normative for all visual decisions.

## 1. Design Character

LifeLinq UI must feel:

- calm
- human
- warm
- lightweight
- responsive

Visual tone must support trust and daily use.

The interface must not feel technical, administrative, or decorative-heavy.

## 2. Surface System

The interface uses two primary surface layers:

- App background surface
- Card surface

Rules:

- Background surface stays neutral and quiet.
- Card surface carries content and actions.
- Contrast must be clear without harsh separation.
- Surface usage must be consistent across features.

Suggested reference:

- Background surface -> warm neutral (example: #F7F6F3)
- Card surface -> white (example: #FFFFFF)

These values are reference guidance, not hardcoded requirements.

## 3. Card System

LifeLinq uses three card types.

### Status Cards

Purpose:

- communicate current state
- summarize what matters now

Rules:

- concise content
- low interaction density
- placed early in screen hierarchy

### Action Cards

Purpose:

- drive user action
- host primary list and task interactions

Rules:

- primary CTA must be clear
- action density can be high if scannable
- should dominate feature workflows where user input is frequent

### Info Cards

Purpose:

- provide secondary context, configuration, or lifecycle controls

Rules:

- lower visual priority than action cards
- no critical primary action hidden only in info cards

## 4. Feature Accent Colors

Each feature uses one accent family for recognition and orientation.

Required mapping:

- todos: calm task-oriented accent
- meals: warm planning accent
- shopping: practical list/action accent
- economy: financial-state accent
- documents: stable archive/reference accent

Suggested palette reference:

- todos: green
- meals: orange
- shopping: blue
- economy: purple
- documents: neutral gray

Rules:

- Accent color is supportive, not dominant.
- Accent usage must be consistent inside each feature.
- Accent must never reduce readability or contrast.
- Palette reference guides consistency, but does not force hardcoded implementation values.

## 5. Spacing System

LifeLinq uses an 8px grid spacing system.

Base unit:

- 8px

Allowed spacing steps should follow grid multiples (for example 8, 16, 24, 32).

Common spacing steps:

- 8px -> tight spacing
- 16px -> default spacing
- 24px -> section spacing
- 32px -> large spacing

Rules:

- spacing must be rhythmically consistent
- intra-card spacing and inter-card spacing must remain predictable
- ad hoc spacing values outside the system are not allowed

## 6. Corner Radius & Elevation

Cards use soft corners and subtle depth.

Rules:

- standard card corner radius is approximately 16px
- elevation/shadow must be subtle
- depth is used for layering clarity, not decoration

The interface must remain light and calm.

## 7. Typography Hierarchy

UI text must follow a strict hierarchy:

- screen title
- card title
- body text
- secondary/subtle text

Rules:

- hierarchy must be visually obvious
- size/weight changes must be purposeful
- secondary text must remain readable but clearly de-emphasized

## 8. Iconography

Icons represent feature intent and quick recognition.

Rules:

- each feature should use stable icon semantics
- icons must support labels, not replace them for core actions
- icon style must remain consistent across the product
- decorative-only icon use is not allowed

## 9. Home Layout Pattern

Home must follow this structural order:

1. Place Header
2. Today Summary
3. Quick Actions
4. Feature Modules

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
- summary/status card accent
- primary CTA emphasis

Rules:

- continuity must improve orientation and recognition
- accent identity must remain supportive, not dominant
- feature screens must feel connected to their Home entry point

## 10. Feature Screen Pattern

Feature screens follow a common structure:

1. top bar (title, context, back/navigation control)
2. summary/status section (if relevant)
3. primary action/content section
4. secondary info/control section
5. overlays/sheets for create/edit flows

Rules:

- primary workflow content must appear before secondary controls
- backend/domain concepts must not dominate visual hierarchy
- screens must stay composition-oriented and predictable

## 11. Visual Restraint

LifeLinq favors calm minimal UI over decoration.

Rules:

- avoid visual noise
- avoid unnecessary ornamentation
- prioritize legibility, spacing, and action clarity

The interface should feel steady and intentional.

## 12. Consistency Rule

All new UI components must reuse this foundation.

Rules:

- reuse established surfaces, spacing, typography, and card patterns
- new patterns require explicit justification
- feature-level variation must not break global consistency

Consistency is a product requirement, not a style preference.
