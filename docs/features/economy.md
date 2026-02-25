# Economy

This document defines the intended design and evolution for the Economy feature.

Economy is not yet implemented.  
This document serves as the architectural contract for its implementation.

---

## Purpose

Economy is a **household settlement engine**.

Its purpose is to calculate how shared expenses (and optionally incomes) should be balanced between household members over defined settlement periods.

Economy does not:

- manage bank accounts
- store financial balances
- execute payments
- integrate with financial institutions

It calculates settlement obligations only.

---

## Design Principles

- Settlement is period-based.
- Periods are sequential and non-overlapping.
- Exactly one active period exists at a time.
- Historical periods are immutable.
- Settlement is always derived, never stored.
- The model must support multiple fairness strategies.
- The model must remain lightweight and household-oriented.

---

## Core Model

### SettlementPeriod (Aggregate Root)

Each household operates in sequential settlement periods.

#### Data shape (conceptual)

- `id`
- `householdId`
- `startDate`
- `endDate` (nullable when OPEN)
- `status` (`OPEN` | `CLOSED`)
- `strategySnapshot`

#### Invariants

- Exactly one `OPEN` period per household.
- Periods must not overlap.
- No temporal gaps between periods.
- `previous.endDate == next.startDate`.
- `CLOSED` period is immutable.

---

## Transactions

Transactions represent shared expenses within a period.

#### Data shape (conceptual)

- `id`
- `periodId`
- `amount`
- `description`
- `paidBy` (userId)
- `createdAt`
- `deletedAt` (soft delete)
- `category` (optional)

#### Rules

- Transactions are immutable.
- Soft delete allowed only while period is `OPEN`.
- Deleted transactions are excluded from settlement.
- No transaction mutation allowed in `CLOSED` periods.

---

## Incomes

Incomes may optionally be registered per user per period.

#### Data shape (conceptual)

- `periodId`
- `userId`
- `amount`

Incomes are only relevant for strategies that require them.

---

## Settlement Calculation

Settlement is always calculated dynamically from:

- active transactions
- incomes (if applicable)
- strategy snapshot

For each period:

- Settlement per member must sum to zero.
- Positive → member owes money.
- Negative → member is owed money.

Settlement is never persisted.

---

## Strategies

Each period uses exactly one strategy.

### Equal Cost

Shared expenses are divided equally.

---

### Percentage Cost

Shared expenses are divided according to fixed percentages per member.

Percentages must sum to 100.

---

### Income-Derived Percentage (Planned)

Percentages are derived from registered income.

Expenses are divided proportionally to income.

---

### Equal Remaining (Planned)

After shared expenses are subtracted from total income,  
remaining disposable income is equalized between members.

---

## Period Lifecycle

- A new household starts with one `OPEN` period.
- Closing a period:
  - sets `endDate`
  - sets `status = CLOSED`
  - creates a new `OPEN` period starting at the same timestamp.
- Closed periods are read-only.

---

## Scope (V0)

Initial implementation includes:

- Period lifecycle (OPEN → CLOSED → new OPEN)
- Equal Cost strategy
- Percentage Cost strategy
- Soft delete of transactions (OPEN only)
- Dynamic settlement calculation

Not included in V0:

- Income registration
- Income-based strategies
- Recurring transactions
- Reporting and analytics
- Cross-period debt carry-over

---

## Planned Evolution

### V1 – Income Support

- Per-period income registration
- Income-Derived Percentage strategy

### V2 – Equal Remaining Strategy

- Equalization of disposable income
- Full support for income-based balancing

### V3 – Recurring Transactions

- Recurring expense templates
- Automatic transaction materialization on period creation

### V4 – Reporting & Insights

- Category breakdown
- Cross-period analytics
- Historical visualization

---

## Architectural Intent

Economy is designed as:

A period-based ledger  
+ a pluggable settlement strategy  
+ deterministic calculation  

The architecture must allow strategy evolution without altering core period or transaction structure.
