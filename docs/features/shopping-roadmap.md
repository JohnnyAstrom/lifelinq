# Shopping Roadmap

This document describes how the Shopping feature should evolve from its current foundation toward its long-term product direction.

It exists to clarify:

- what Shopping v1 must achieve
- what belongs in later phases
- what the long-term product direction is
- why the implementation order matters

This document is a roadmap and direction document.

It does **not** replace `shopping.md`, which remains the source of truth for the Shopping feature’s purpose, domain direction, interaction direction, and architectural ownership.

---

## Product Direction

Shopping should become the best shared group surface for common purchasing in LifeLinq.

It must work well across different group contexts, including:

- household
- family cottage
- hunting team
- sports club
- association
- other shared group settings

Grocery is a first-class and strongly optimized use case, but Shopping must not become grocery-only.

The long-term goal is a Shopping feature that feels:

- fast to capture into
- clear to scan
- easy to use during active purchasing
- adaptable to different list contexts
- naturally integrated with the rest of group life in LifeLinq

---

## Sequencing Principles

Shopping must not be built through provisional architecture that is known to be wrong.

The feature should evolve through:

- correct foundation before polish
- feature-owned domain logic before shared abstraction
- grouped orientation before advanced intelligence
- fast primary flows before secondary convenience features
- broad product direction with controlled implementation scope

This means:

- v1 should establish the right base
- v2 should make the experience smarter and more adaptive
- later phases should deepen the product’s differentiation

---

## V1 — Correct Foundation and First Strong Experience

### Purpose

V1 exists to establish the first real Shopping experience on top of the correct long-term foundation.

It is not a temporary CRUD phase.
It is the first intentional product layer.

### V1 outcomes

V1 should achieve the following:

- Shopping detail behaves like an active purchasing surface, not a raw item admin screen
- open items are organized through grouped orientation
- bought items are secondary
- quick add becomes the primary item entry path
- Shopping gains a feature-owned internal seam between transport reality and Shopping-specific UI/domain projections
- row interaction becomes more shopping-oriented and less edit-centric
- the model remains group-first and adaptable, not grocery-only

### V1 scope

V1 should include:

- feature-owned internal Shopping projection/model seam
- first practical grouping/category semantics
- grouped open sections
- separate secondary bought section
- quick add as the primary add flow
- stronger item row UX for scanning and toggle
- continued support for current working basics such as:
  - add
  - edit
  - toggle
  - remove
  - current overlays where still useful
  - current reorder where still tolerated

### V1 should not try to finish everything

V1 is not expected to include:

- retailer-specific store ordering
- advanced learning/personalization
- barcode scanning
- voice input
- rich source/provenance UX
- fully mature list-type specialization
- final visual polish of all shopping surfaces

### V1 success criteria

V1 is successful when:

- the feature has the right architectural base
- grouped shopping is real, not simulated
- quick add feels primary
- detail view feels closer to shopping than CRUD
- future work no longer requires rethinking the whole screen architecture

---

## V2 — Smarter and More Adaptive Shopping

### Purpose

V2 exists to make Shopping feel smarter, more context-aware, and more group-adaptive without changing its core architecture again.

### V2 outcomes

V2 should add:

- better category inference
- stronger effectiveCategory behavior
- list-type-driven defaults
- clearer adaptation between grocery, consumables, supplies, and mixed lists
- more helpful quick add behavior
- stronger distinction between planning rhythm and shopping rhythm
- improved bought/open behavior in grouped contexts
- cleaner handling of reorder in a grouped world

### V2 likely additions

Examples of likely V2 improvements:

- better keyword/category mapping
- group memory for repeated category corrections
- improved section naming and ordering
- context-sensitive defaults by list type
- more capable row metadata presentation
- better lightweight editing flows
- clearer meals → shopping integration semantics
- initial source/provenance behavior that becomes visible in the product where useful

### V2 still remains controlled

V2 should still avoid:

- heavy speculative intelligence
- retailer-specific assumptions as the default model
- feature leakage into `shared/`
- turning Shopping into a special-case companion to Meals instead of its own feature

### V2 success criteria

V2 is successful when:

- the feature feels meaningfully smarter than V1
- list context actually changes behavior in useful ways
- grouping feels more natural and less generic
- the product becomes more adaptive without becoming more confusing

---

## Final Direction

### Purpose

The final direction is not “more features”.
It is a mature Shopping product that feels best-in-class because its core use is excellent.

### Final product qualities

The mature Shopping feature should feel:

- fast
- calm
- obvious
- group-aware
- structurally smart
- adaptable to context
- strong for grocery
- still useful outside grocery

### Final product characteristics

A mature Shopping product likely includes:

- excellent grouped active-shopping UX
- clear support for both planning and active purchasing rhythms
- strong quick capture paths
- item rows that carry more meaning with low visual friction
- adaptive category/grouping behavior
- clean group-specific learning over time
- seamless intake from other group workflows where relevant
- richer but controlled item semantics
- optional higher-level convenience layers added only where they clearly improve the core experience

### Possible later differentiators

These may become valuable later, but are not part of the foundation:

- barcode scanning
- voice input
- richer history/activity visibility
- stronger undo/recent-action affordances
- retailer/store-order support where context makes it useful
- more advanced provenance and recurring-item behavior
- premium convenience layers around repeated purchasing patterns

These should only be added if the core shopping experience is already strong.

---

## What Must Stay True Across All Phases

The following must remain true across V1, V2, and later phases:

- Shopping remains a shared group feature
- Shopping remains feature-owned inside `features/shopping`
- Shopping semantics do not migrate into `shared/`
- Grocery remains a first-class specialization, not the whole model
- the feature keeps a clean integration boundary with Meals and other features
- architecture should become more expressive over time, not more coupled
- implementation should follow the product direction without relying on known temporary architecture

---

## Practical Interpretation

When making implementation decisions, this roadmap should be used to answer questions like:

- Is this a foundation problem or a polish problem?
- Is this a V1 responsibility or a later-phase enhancement?
- Does this change move Shopping toward the final direction, or sideways into temporary complexity?
- Are we making the feature more adaptable, or just more complicated?
- Are we preserving Shopping as a strong feature boundary?

If a decision improves short-term output but pushes Shopping away from this direction, it should be treated with caution.

---

## Summary

Shopping evolves in three broad layers:

### V1
Build the correct base and the first strong grouped Shopping experience.

### V2
Make Shopping smarter, more adaptive, and more context-aware.

### Final direction
Deliver a mature, best-in-class shared group purchasing surface that is especially strong for grocery, but not limited to it.
