package app.lifelinq.features.todo.infrastructure;

import app.lifelinq.features.todo.domain.Todo;
import app.lifelinq.features.todo.domain.TodoScope;
import java.time.LocalDate;
import java.time.LocalTime;

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
        TodoScheduling scheduling = normalizeScheduling(entity);
        return Todo.rehydrate(
                entity.getId(),
                entity.getGroupId(),
                entity.getText(),
                entity.getStatus(),
                scheduling.scope(),
                scheduling.dueDate(),
                scheduling.dueTime(),
                scheduling.scopeYear(),
                scheduling.scopeWeek(),
                scheduling.scopeMonth(),
                entity.getCompletedAt(),
                entity.getCreatedAt(),
                entity.getDeletedAt()
        );
    }

    private TodoScheduling normalizeScheduling(TodoEntity entity) {
        TodoScope scope = entity.getScope();
        LocalDate dueDate = entity.getDueDate();
        LocalTime dueTime = entity.getDueTime();
        Integer scopeYear = entity.getScopeYear();
        Integer scopeWeek = entity.getScopeWeek();
        Integer scopeMonth = entity.getScopeMonth();

        if (scope == null) {
            return dueDate != null
                    ? new TodoScheduling(TodoScope.DAY, dueDate, dueTime, null, null, null)
                    : new TodoScheduling(TodoScope.LATER, null, null, null, null, null);
        }

        return switch (scope) {
            case DAY -> dueDate != null
                    ? new TodoScheduling(TodoScope.DAY, dueDate, dueTime, null, null, null)
                    : new TodoScheduling(TodoScope.LATER, null, null, null, null, null);
            case WEEK -> {
                if (scopeYear != null && scopeWeek != null) {
                    yield new TodoScheduling(TodoScope.WEEK, null, null, scopeYear, scopeWeek, null);
                }
                yield dueDate != null
                        ? new TodoScheduling(TodoScope.DAY, dueDate, dueTime, null, null, null)
                        : new TodoScheduling(TodoScope.LATER, null, null, null, null, null);
            }
            case MONTH -> {
                if (scopeYear != null && scopeMonth != null) {
                    yield new TodoScheduling(TodoScope.MONTH, null, null, scopeYear, null, scopeMonth);
                }
                yield dueDate != null
                        ? new TodoScheduling(TodoScope.DAY, dueDate, dueTime, null, null, null)
                        : new TodoScheduling(TodoScope.LATER, null, null, null, null, null);
            }
            case LATER -> dueDate != null
                    ? new TodoScheduling(TodoScope.DAY, dueDate, dueTime, null, null, null)
                    : new TodoScheduling(TodoScope.LATER, null, null, null, null, null);
        };
    }

    private record TodoScheduling(
            TodoScope scope,
            LocalDate dueDate,
            LocalTime dueTime,
            Integer scopeYear,
            Integer scopeWeek,
            Integer scopeMonth
    ) {
    }
}
