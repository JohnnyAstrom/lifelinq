UX Principles
Scope & Authority

This document governs user experience decisions only.

It does not alter:

domain logic

API contracts

data models

transaction boundaries

feature ownership

Architecture remains authoritative over system behavior.

UX principles guide presentation, labeling, visibility, and interaction — not capability.

This document is normative for all UI-related decisions.

1. Product Identity

LifeLinq is collaboration-native with frictionless solo entry.

The system is built for shared coordination, but the experience must always begin personal and simple.

Architecture: multi-context

Experience: single-space-first

Users should feel like they are entering their place — not entering a system.

2. Core Mental Model

Internal representation: group

Architectural concept: context

UX model: place (implicit)

“Space” may be used when labeling is necessary, but it is not the primary identity.

Principle:

This is my place.

It can become ours.

The system supports structure — it does not own it.

The product must never feel:

multi-tenant

administrative

enterprise-configured

organization-driven

3. Solo-First Principle

If a user has one space:

No switching UI

No role exposure

No system labeling

No administrative terminology

Multi-space is exposed only when the user actually has more than one.

Features become visible only when they require action.

4. Feature Visibility Rule

A capability should only be visible when the user must make a decision that depends on it.

System capacity must never be exposed simply because it exists.

Complexity should remain latent until relevant.

5. Role Philosophy

Roles are governance mechanisms, not identity markers.

Roles influence available actions, not status.

Roles should not be displayed unless they affect user decisions.

Roles protect the system. They do not define the person.

6. Terminology

Backend terminology must never surface directly in the UI.

Avoid:

Group

Membership

Active group

Tenant

Manage group

Prefer:

Members

Invite someone

Switch space

“Space” should be used sparingly.
Implicit context is better than explicit labeling.
Defaults do not require naming.

7. Human Identity

Users are represented as people.

UUIDs are never displayed.

Names take priority over identifiers.

The system shows people, not accounts.

The product is person-centered.

8. Progressive Disclosure

Structural complexity is revealed gradually.

Single space → no visible structure

Multiple spaces → subtle switching

Admin capabilities → only when relevant

Users should never be confronted with the full system model unnecessarily.

9. Continuity Principle

Context changes, but the user’s orientation must remain intact.

Navigation structure remains stable.

Layout remains consistent.

The product feels continuous.

Context changes. The product does not.

10. Interaction & Feedback Philosophy

Feedback defines how the system responds to user action.

Feedback must be:

brief

calm

human

non-technical

The system assists — it does not instruct.

Success states should feel lightweight.
Confirmation should often be implicit through UI change rather than verbose messaging.

11. Information Hierarchy & Action Priority

UI prioritizes action over structure.

Show what the user can do.

Hide system status unless it affects action.

Structural information is secondary.

The product is a tool for action, not a dashboard.

12. Empty State Philosophy

Empty states should feel:

natural

expected

non-alarming

Empty states are not an error.
They are the starting point.

Avoid enterprise-style phrasing such as:
“You have no items configured.”

Empty states should signal possibility, not absence.

13. Error Handling

Failure states must be communicated in a way that is:

brief

calm

human

non-technical

Errors should:

explain what happened

suggest next action when possible

never feel accusatory

Examples:

“Something went wrong. Please try again.”

“You need to log in to continue.”

“You don’t have permission to do that.”

Errors should reduce anxiety, not increase it.

14. Visual & Interaction Character

The product should feel:

minimal

calm

responsive

lightweight

Interfaces prioritize clarity over decoration.

The system should feel fast, steady, and emotionally neutral.

15. Accessibility Commitment

Accessibility is not optional.

Interfaces must:

support keyboard navigation

support screen readers

maintain sufficient contrast

avoid exclusionary interaction patterns

Clarity and accessibility are aligned goals.
