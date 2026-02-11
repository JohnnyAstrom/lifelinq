package app.lifelinq.features.todo.infrastructure;

import app.lifelinq.features.todo.domain.TodoStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    protected TodoEntity() {
    }

    public TodoEntity(UUID id, UUID householdId, String text, TodoStatus status) {
        this.id = id;
        this.householdId = householdId;
        this.text = text;
        this.status = status;
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
}
