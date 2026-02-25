package app.lifelinq.features.todo.infrastructure;

import app.lifelinq.features.todo.domain.Todo;

public final class TodoMapper {

    public TodoEntity toEntity(Todo todo) {
        if (todo == null) {
            throw new IllegalArgumentException("todo must not be null");
        }
        return new TodoEntity(
                todo.getId(),
                todo.getGroupId(),
                todo.getText(),
                todo.getStatus(),
                todo.getScope(),
                todo.getDueDate(),
                todo.getDueTime(),
                todo.getScopeYear(),
                todo.getScopeWeek(),
                todo.getScopeMonth(),
                todo.getCompletedAt(),
                todo.getCreatedAt(),
                todo.getDeletedAt()
        );
    }

    public Todo toDomain(TodoEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity must not be null");
        }
        return Todo.rehydrate(
                entity.getId(),
                entity.getGroupId(),
                entity.getText(),
                entity.getStatus(),
                entity.getScope(),
                entity.getDueDate(),
                entity.getDueTime(),
                entity.getScopeYear(),
                entity.getScopeWeek(),
                entity.getScopeMonth(),
                entity.getCompletedAt(),
                entity.getCreatedAt(),
                entity.getDeletedAt()
        );
    }
}
