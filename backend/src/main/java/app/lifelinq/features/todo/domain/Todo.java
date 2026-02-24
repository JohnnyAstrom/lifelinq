package app.lifelinq.features.todo.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public final class Todo {
    private final UUID id;
    private final UUID householdId;
    private final String text;
    private TodoStatus status;
    private final TodoScope scope;
    private final LocalDate dueDate;
    private final LocalTime dueTime;
    private final Integer scopeYear;
    private final Integer scopeWeek;
    private final Integer scopeMonth;
    private Instant completedAt;
    private final Instant createdAt;
    private Instant deletedAt;

    public Todo(UUID id, UUID householdId, String text) {
        this(id, householdId, text, TodoScope.LATER, null, null, null, null, null, Instant.now());
    }

    public Todo(UUID id, UUID householdId, String text, LocalDate dueDate, LocalTime dueTime) {
        this(
                id,
                householdId,
                text,
                dueDate != null ? TodoScope.DAY : TodoScope.LATER,
                dueDate,
                dueTime,
                null,
                null,
                null,
                Instant.now()
        );
    }

    public Todo(
            UUID id,
            UUID householdId,
            String text,
            TodoScope scope,
            LocalDate dueDate,
            LocalTime dueTime,
            Integer scopeYear,
            Integer scopeWeek,
            Integer scopeMonth,
            Instant createdAt
    ) {
        validateBase(id, householdId, text, createdAt);
        validateScopeFields(scope, dueDate, dueTime, scopeYear, scopeWeek, scopeMonth);
        this.id = id;
        this.householdId = householdId;
        this.text = text;
        this.scope = scope;
        this.dueDate = dueDate;
        this.dueTime = dueTime;
        this.scopeYear = scopeYear;
        this.scopeWeek = scopeWeek;
        this.scopeMonth = scopeMonth;
        this.status = TodoStatus.OPEN;
        this.completedAt = null;
        this.createdAt = createdAt;
        this.deletedAt = null;
    }

    public static Todo rehydrate(
            UUID id,
            UUID householdId,
            String text,
            TodoStatus status,
            TodoScope scope,
            LocalDate dueDate,
            LocalTime dueTime,
            Integer scopeYear,
            Integer scopeWeek,
            Integer scopeMonth,
            Instant completedAt,
            Instant createdAt,
            Instant deletedAt
    ) {
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        TodoScope effectiveScope = scope;
        if (effectiveScope == null) {
            effectiveScope = dueDate != null ? TodoScope.DAY : TodoScope.LATER;
        }
        Instant effectiveCreatedAt = createdAt != null ? createdAt : Instant.EPOCH;
        Todo todo = new Todo(
                id,
                householdId,
                text,
                effectiveScope,
                dueDate,
                dueTime,
                scopeYear,
                scopeWeek,
                scopeMonth,
                effectiveCreatedAt
        );
        todo.status = status;
        todo.completedAt = completedAt;
        todo.deletedAt = deletedAt;
        return todo;
    }

    public static Todo rehydrate(
            UUID id,
            UUID householdId,
            String text,
            TodoStatus status,
            LocalDate dueDate,
            LocalTime dueTime,
            Instant deletedAt
    ) {
        return rehydrate(
                id,
                householdId,
                text,
                status,
                null,
                dueDate,
                dueTime,
                null,
                null,
                null,
                null,
                null,
                deletedAt
        );
    }

    private static void validateBase(UUID id, UUID householdId, String text, Instant createdAt) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        if (householdId == null) {
            throw new IllegalArgumentException("householdId must not be null");
        }
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text must not be blank");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt must not be null");
        }
    }

    private static void validateScopeFields(
            TodoScope scope,
            LocalDate dueDate,
            LocalTime dueTime,
            Integer scopeYear,
            Integer scopeWeek,
            Integer scopeMonth
    ) {
        if (scope == null) {
            throw new IllegalArgumentException("scope must not be null");
        }
        if (dueTime != null && dueDate == null) {
            throw new IllegalArgumentException("dueDate must not be null when dueTime is set");
        }
        switch (scope) {
            case DAY -> {
                if (dueDate == null) {
                    throw new IllegalArgumentException("dueDate must not be null for DAY scope");
                }
                if (scopeYear != null || scopeWeek != null || scopeMonth != null) {
                    throw new IllegalArgumentException("scopeYear/scopeWeek/scopeMonth must be null for DAY scope");
                }
            }
            case WEEK -> {
                if (scopeYear == null || scopeWeek == null) {
                    throw new IllegalArgumentException("scopeYear and scopeWeek must not be null for WEEK scope");
                }
                if (scopeWeek < 1 || scopeWeek > 53) {
                    throw new IllegalArgumentException("scopeWeek must be between 1 and 53");
                }
                if (dueDate != null || dueTime != null || scopeMonth != null) {
                    throw new IllegalArgumentException("dueDate/dueTime/scopeMonth must be null for WEEK scope");
                }
            }
            case MONTH -> {
                if (scopeYear == null || scopeMonth == null) {
                    throw new IllegalArgumentException("scopeYear and scopeMonth must not be null for MONTH scope");
                }
                if (scopeMonth < 1 || scopeMonth > 12) {
                    throw new IllegalArgumentException("scopeMonth must be between 1 and 12");
                }
                if (dueDate != null || dueTime != null || scopeWeek != null) {
                    throw new IllegalArgumentException("dueDate/dueTime/scopeWeek must be null for MONTH scope");
                }
            }
            case LATER -> {
                if (dueDate != null || dueTime != null || scopeYear != null || scopeWeek != null || scopeMonth != null) {
                    throw new IllegalArgumentException("all scheduling fields must be null for LATER scope");
                }
            }
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getHouseholdId() {
        return householdId;
    }

    public String getText() {
        return text;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public TodoScope getScope() {
        return scope;
    }

    public LocalTime getDueTime() {
        return dueTime;
    }

    public Integer getScopeYear() {
        return scopeYear;
    }

    public Integer getScopeWeek() {
        return scopeWeek;
    }

    public Integer getScopeMonth() {
        return scopeMonth;
    }

    public TodoStatus getStatus() {
        return status;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void toggle(Instant now) {
        if (now == null) {
            throw new IllegalArgumentException("now must not be null");
        }
        if (deletedAt != null) {
            throw new IllegalStateException("todo is deleted");
        }
        if (status == TodoStatus.OPEN) {
            status = TodoStatus.COMPLETED;
            completedAt = now;
        } else {
            status = TodoStatus.OPEN;
            completedAt = null;
        }
    }

    public void delete(Instant now) {
        if (now == null) {
            throw new IllegalArgumentException("now must not be null");
        }
        if (deletedAt == null) {
            deletedAt = now;
        }
    }
}
