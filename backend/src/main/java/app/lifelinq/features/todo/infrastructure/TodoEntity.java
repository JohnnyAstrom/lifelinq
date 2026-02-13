package app.lifelinq.features.todo.infrastructure;

import app.lifelinq.features.todo.domain.TodoStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "todos")
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

    private LocalDate dueDate;

    private LocalTime dueTime;

    protected TodoEntity() {
    }

    public TodoEntity(
            UUID id,
            UUID householdId,
            String text,
            TodoStatus status,
            LocalDate dueDate,
            LocalTime dueTime
    ) {
        this.id = id;
        this.householdId = householdId;
        this.text = text;
        this.status = status;
        this.dueDate = dueDate;
        this.dueTime = dueTime;
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

    public LocalTime getDueTime() {
        return dueTime;
    }
}
