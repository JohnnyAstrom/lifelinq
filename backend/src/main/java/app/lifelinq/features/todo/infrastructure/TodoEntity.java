package app.lifelinq.features.todo.infrastructure;

import app.lifelinq.features.todo.domain.TodoScope;
import app.lifelinq.features.todo.domain.TodoStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "todos",
        indexes = {
                @Index(name = "idx_todos_household", columnList = "householdId"),
                @Index(name = "idx_todos_household_scope", columnList = "householdId,scope"),
                @Index(name = "idx_todos_household_duedate", columnList = "householdId,dueDate"),
                @Index(name = "idx_todos_deletedat", columnList = "deletedAt")
        }
)
public class TodoEntity {
    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID householdId;

    @Column(nullable = false)
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TodoStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TodoScope scope;

    private LocalDate dueDate;

    private LocalTime dueTime;

    private Integer scopeYear;

    private Integer scopeWeek;

    private Integer scopeMonth;

    private Instant completedAt;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant deletedAt;

    protected TodoEntity() {
    }

    public TodoEntity(
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
        this.id = id;
        this.householdId = householdId;
        this.text = text;
        this.status = status;
        this.scope = scope;
        this.dueDate = dueDate;
        this.dueTime = dueTime;
        this.scopeYear = scopeYear;
        this.scopeWeek = scopeWeek;
        this.scopeMonth = scopeMonth;
        this.completedAt = completedAt;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
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

    public TodoStatus getStatus() {
        return status;
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

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }
}
