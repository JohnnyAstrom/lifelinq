# Shopping

This document defines the intended direction and architectural ownership of the Shopping feature.

## Purpose

Shopping is a shared group feature for common purchasing.

It must support multiple shared group contexts, including:

- household
- family cottage
- hunting team
- sports club
- association
- other shared group settings

Grocery is a first-class and strongly optimized use case, but it is not the whole feature.

Shopping exists to help a group capture, organize, and complete shared purchasing work.
It is not only a grocery checklist and it is not primarily a CRUD admin surface.

---

## Scope & Authority

This document defines:

- the Shopping feature's purpose
- the intended domain model direction
- the intended interaction model direction
- the Shopping feature's architectural ownership

This document does not claim that all described future-ready concepts already exist in the current backend or transport API.

Where current implementation reality differs from the intended direction, that distinction is explicit in this document.

---

## Core Principles

- Shared by group, not personal by default.
- Quick capture is primary.
- Grouped orientation matters more than raw item CRUD.
- The feature must adapt to list context, not force every list into a grocery-only mental model.
- Grocery is a first-class specialization, not a universal assumption.
- The feature should optimize for active purchasing sessions, not for administration-heavy editing flows.
- Open work is primary; completed work is secondary.
- Shopping-specific semantics belong to the Shopping feature, not to shared infrastructure.

---

## Feature Model

Conceptually, Shopping consists of:

- multiple shopping lists
- each list owned by a group context
- each item belonging to exactly one list
- list-level ordering and list-level interaction behavior

Model direction:

- `ShoppingList` is the aggregate root.
- `ShoppingItem` belongs to exactly one list.
- Bought/open is item state inside the list.
- No cross-list move requirement exists in the initial model unless explicitly added later.

This keeps ownership explicit:

- group owns lists
- list owns items
- item state changes are mutations within its list

---

## Domain Model Direction

The future-ready Shopping domain direction is richer than the current transport model.
That richer model is conceptual guidance for the feature, not a claim about current API reality.

### Conceptual aggregate: `ShoppingList`

Future-ready internal direction:

- `id`
- `name`
- `type`
- `groupingMode`
- `items`

Meaning:

- `type` describes the dominant use case of the list
- `groupingMode` describes how open items are organized
- rendering and interaction may later support different shopping/planning presentations without requiring the transport model to expose them first

### Conceptual entity: `ShoppingItem`

Future-ready internal direction:

- `id`
- `title`
- `normalizedTitle`
- `status`
- `quantity`
- `unit`
- `note`
- `categorySuggestion`
- `categoryOverride`
- `effectiveCategory`
- `source`
- `createdAt`
- `boughtAt`

Meaning:

- `title` is the user-facing item label
- `normalizedTitle` is a canonical comparison/search form, not necessarily user-visible
- `status` expresses whether the item is still open or already bought
- `quantity` and `unit` remain optional, but paired when used
- `note` supports item-specific details without overloading the title
- `categorySuggestion` is a system-suggested classification
- `categoryOverride` is an explicit user or group correction
- `effectiveCategory` is the category actually used for rendering and grouping
- `source` captures provenance in a generic way

### Current transport reality

Current known transport fields are narrower.
See `API / Transport Reality` below for the actual known shape.

The current API does not yet expose all future-ready conceptual fields above.

---

## List Type Model

List type is central to how items should be interpreted.

Suggested initial types:

- `grocery`
- `consumables`
- `supplies`
- `mixed`

Meaning:

- `grocery` emphasizes food-oriented grouping and purchasing rhythm
- `consumables` emphasizes recurring practical items used up over time
- `supplies` fits broader non-grocery procurement such as association, team, cabin, or activity supplies
- `mixed` is a fallback when no single domain dominates

List type should influence:

- default taxonomy
- default grouping behavior
- useful suggestions
- how strongly grocery-specific assumptions apply

List type should not be treated as a cosmetic label only.

---

## Category / Taxonomy Model

Shopping must support taxonomy per list type rather than one universal grocery-only taxonomy.

This means:

- grocery taxonomy is a first-class case
- grocery taxonomy is not the only valid taxonomy
- consumables and supplies contexts may need different category sets
- mixed lists may use a broader or lighter taxonomy

List type should define a default taxonomy, but taxonomy/grouping behavior should remain configurable over time where needed.

Classification direction:

- `categorySuggestion` is produced by the system
- `categoryOverride` is an explicit user or group correction
- `effectiveCategory` is the value used by the UI

Longer-term direction:

- the group may develop memory over time
- repeated overrides may influence future suggestions
- learning should remain tied to group/list context, not assumed to be globally universal

The model should therefore support category behavior that is:

- context-aware
- type-aware
- overrideable
- capable of becoming more accurate over time

---

## Interaction Model Direction

Shopping is an active shared group purchasing surface.

Interaction direction:

- quick add is primary
- grouped open sections are the main orientation model
- bought items are secondary and typically shown in a separate lower section in v1
- detailed editing is secondary, not primary
- row interaction should prioritize toggle + readability over drag-centric behavior

The default experience should answer:

- what still needs to be purchased
- how items are grouped for the current list context
- what can be checked off quickly during an active session

The model should not be optimized around permanent visibility of edit controls or administration-heavy forms.

Manual ordering may remain supported, but it is secondary to grouped orientation in list types where grouped shopping is the primary experience.

---

## Planning vs Shopping Rhythm

Shopping has two rhythms:

### Planning rhythm

Planning rhythm is about:

- capturing items quickly
- refining quantities or notes
- organizing the list
- preparing for later purchase

### Shopping rhythm

Shopping rhythm is about:

- moving through the active open list
- understanding grouped sections quickly
- toggling bought items with minimal friction
- reducing interaction cost while actively purchasing

The model must support both rhythms even if the first production UI emphasizes one primary experience first.

These rhythms should be understood as interaction needs first, not necessarily as persistent transport-level fields from the beginning.

---

## Invariants / Rules

- A shopping list must exist before items can be added or mutated.
- A shopping item must belong to exactly one list.
- A shopping item must belong to the list being mutated.
- Group scoping is enforced through list ownership.
- Item title/name is required and non-blank.
- Quantity and unit are optional, but when one is used the other must also be used.
- Toggling to bought sets `boughtAt`.
- Toggling back to open clears `boughtAt`.
- Open/bought is a state transition inside the list, not a cross-list event.
- Removing an item removes it from its owning list.

Important correction:

- This document does not assume item title uniqueness within a list.

The older uniqueness assumption is too strong for current reality and too rigid for practical shared shopping behavior.
Different groups may intentionally keep repeated entries until a stronger merge/deduplication policy is explicitly introduced.

---

## Current Reality vs Future Direction

### Current backend/API reality

Current known frontend transport shape includes:

- list: `id`, `name`, `items`
- item: `id`, `name`, `status`, `quantity`, `unit`, `createdAt`, `boughtAt`

Current known item state usage in frontend:

- `BOUGHT` is treated as bought
- anything else is treated as open

Current known interaction reality:

- multiple lists exist
- lists are group-scoped
- list reorder exists
- item reorder exists for open items
- add/edit/toggle/remove exists
- quantity and unit are supported
- bought items are rendered separately from open items

Current known limitations:

- no list type field in transport
- no taxonomy/category field in transport
- no note field in transport
- no provenance/source field in transport
- no explicit order field in transport
- no current transport-level grouping model beyond client-side open/bought separation

### Future-ready frontend/domain direction

The Shopping feature should evolve toward:

- explicit list type
- explicit grouping semantics
- explicit classification semantics
- explicit provenance/source semantics
- richer domain-oriented item representation than the current transport contract

This future direction should be understood as feature/domain intent, not current API truth.

---

## Architectural Ownership

Shopping domain logic belongs in:

- `src/features/shopping`

This includes Shopping-specific:

- classification
- grouping
- list-type interpretation
- taxonomy selection
- item interaction semantics
- shopping-oriented workflow logic

`shared/` is for primitives and infrastructure, not for Shopping semantics.

That means:

- shared UI primitives may be reused by Shopping
- shared overlay infrastructure may be reused by Shopping
- shopping-specific grouping or classification logic must remain feature-owned

Shopping must preserve clean feature boundaries with:

- meals
- home
- auth/bootstrap
- other domain features

---

## Integration Direction

Meals may feed Shopping through a clean integration boundary.

That means:

- Shopping can accept externally produced items
- Shopping should not become meal-owned
- meal-derived items should remain Shopping items once added

Provenance should stay generic, for example:

- `manual`
- `meal-plan`
- `recipe`
- other future domain sources

The source model must not overfit Meals.

---

## Non-goals / Deferred Areas

The following are not core requirements for the initial future-ready Shopping model unless explicitly added later:

- retailer-specific aisle precision
- barcode scanning as a core requirement
- loyalty or deals integration
- heavy personalization from day one
- complex lifecycle states beyond open/bought
- cross-list move semantics
- shopping becoming a retailer-specific workflow instead of a general shared group purchasing feature

---

## API / Transport Reality

Current known transport contracts in frontend code expose:

### Shopping list

- `id: string`
- `name: string`
- `items: ShoppingItemResponse[]`

### Shopping item

- `id: string`
- `name: string`
- `status: string`
- `quantity: number | null`
- `unit: ShoppingUnit | null`
- `createdAt: string`
- `boughtAt: string | null`

### Current known unit values

- `PCS`
- `PACK`
- `KG`
- `HG`
- `G`
- `L`
- `DL`
- `ML`

These values reflect current canonical transport reality.

### Current known mutation shape

- create list
- list lists
- delete list
- update list name
- reorder list with `direction: UP | DOWN`
- add item with `name`, optional `quantity`, optional `unit`
- toggle item bought/open
- reorder item with `direction: UP | DOWN`
- delete item
- update item with `name`, optional `quantity`, optional `unit`

### Current known omission

Current transport does not expose:

- list type
- grouping mode
- item note
- category suggestion
- category override
- effective category
- source/provenance
- explicit item order value

That omission is architectural reality and must be acknowledged when evolving the feature.
