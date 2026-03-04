# ADR 0008: PostgreSQL Persistence Baseline

## Status

Accepted (2026-03-04)

## Context

Phase 6 introduced the persistence transition from in-memory-first runtime behavior to PostgreSQL-backed runtime behavior.

The system already selected PostgreSQL at stack level, but persistence execution details were not yet locked as architecture decisions.

During Phase 6, schema validation failures surfaced from naming drift and mixed schema ownership assumptions.

A clear persistence baseline is required to keep runtime behavior deterministic across local and production-like environments.

## Decision

- PostgreSQL is the runtime persistence database.
- Flyway is the authoritative schema source of truth.
- Hibernate does not generate schema; `ddl-auto=validate` is enforced for runtime validation only.
- Database object naming follows `snake_case` convention.
- Local development uses Docker-based PostgreSQL.
- Persistence naming is aligned to domain terminology by using `groups` table naming in place of `households`.

## Consequences

- Schema evolution is explicit, versioned, and reviewable through migrations.
- Startup fails fast when entity mappings and schema diverge.
- Local environments reproduce production-like persistence behavior more reliably.
- Naming consistency improves mapping stability and reduces validation drift.
- Existing and future persistence changes must be introduced via Flyway migrations.

## Alternatives Considered

### 1) Hibernate schema generation (`update` / `create`)

Rejected.

This weakens migration control, makes schema evolution less deterministic, and increases drift risk across environments.

### 2) Keep mixed camelCase and snake_case database naming

Rejected.

This creates repeated mapping ambiguity and validation issues with the current naming strategy.

### 3) Keep `households` persistence naming while domain uses `group`

Rejected.

This preserves terminology drift in the persistence layer and increases cognitive overhead in maintenance.

### 4) Local Postgres installed manually instead of Docker baseline

Rejected.

This increases environment variance and onboarding friction.
