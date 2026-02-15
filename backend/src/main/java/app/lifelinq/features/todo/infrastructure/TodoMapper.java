package app.lifelinq.features.todo.infrastructure;

import app.lifelinq.features.todo.domain.Todo;

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
                todo.getDueTime(),
                todo.getDeletedAt()
        );
    }

    public Todo toDomain(TodoEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity must not be null");
        }
        return Todo.rehydrate(
                entity.getId(),
                entity.getHouseholdId(),
                entity.getText(),
                entity.getStatus(),
                entity.getDueDate(),
                entity.getDueTime(),
                entity.getDeletedAt()
        );
    }
}
