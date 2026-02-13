package app.lifelinq.features.todo.infrastructure;

import app.lifelinq.features.todo.domain.Todo;
import app.lifelinq.features.todo.domain.TodoStatus;
import java.time.Instant;

public final class TodoMapper {

    public TodoEntity toEntity(Todo todo) {
        if (todo == null) {
            throw new IllegalArgumentException("todo must not be null");
        }
        return new TodoEntity(
                todo.getId(),
                todo.getHouseholdId(),
                todo.getText(),
                todo.getStatus(),
                todo.getDueDate(),
                todo.getDueTime()
        );
    }

    public Todo toDomain(TodoEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity must not be null");
        }
        Todo todo = new Todo(
                entity.getId(),
                entity.getHouseholdId(),
                entity.getText(),
                entity.getDueDate(),
                entity.getDueTime()
        );
        if (entity.getStatus() == TodoStatus.COMPLETED) {
            todo.toggle(Instant.EPOCH);
        }
        return todo;
    }
}
