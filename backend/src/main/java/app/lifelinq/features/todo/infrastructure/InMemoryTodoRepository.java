package app.lifelinq.features.todo.infrastructure;

import app.lifelinq.features.todo.domain.Todo;
import app.lifelinq.features.todo.domain.TodoRepository;
import app.lifelinq.features.todo.domain.TodoScope;
import app.lifelinq.features.todo.domain.TodoStatus;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryTodoRepository implements TodoRepository {
    private final ConcurrentMap<UUID, Todo> byId = new ConcurrentHashMap<>();

    @Override
    public Optional<Todo> findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public void save(Todo todo) {
        if (todo == null) {
            throw new IllegalArgumentException("todo must not be null");
        }
        byId.put(todo.getId(), todo);
    }

    @Override
    public List<Todo> listByGroup(UUID groupId, TodoStatus statusFilter) {
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        if (statusFilter == null) {
            throw new IllegalArgumentException("statusFilter must not be null");
        }
        List<Todo> result = new ArrayList<>();
        for (Todo todo : byId.values()) {
            if (!groupId.equals(todo.getGroupId())) {
                continue;
            }
            if (todo.isDeleted()) {
                continue;
            }
            if (statusFilter != TodoStatus.ALL && todo.getStatus() != statusFilter) {
                continue;
            }
            result.add(todo);
        }
        result.sort(defaultComparator());
        return result;
    }

    @Override
    public List<Todo> listForMonth(UUID groupId, int year, int month, LocalDate startDate, LocalDate endDate) {
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("startDate must not be null");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("endDate must not be null");
        }
        List<Todo> result = new ArrayList<>();
        for (Todo todo : byId.values()) {
            if (!groupId.equals(todo.getGroupId())) {
                continue;
            }
            if (todo.isDeleted()) {
                continue;
            }
            if (todo.getScope() == TodoScope.DAY && todo.getDueDate() != null) {
                LocalDate dueDate = todo.getDueDate();
                if ((dueDate.isEqual(startDate) || dueDate.isAfter(startDate))
                        && (dueDate.isEqual(endDate) || dueDate.isBefore(endDate))) {
                    result.add(todo);
                }
                continue;
            }
            if (todo.getScope() == TodoScope.MONTH
                    && Integer.valueOf(year).equals(todo.getScopeYear())
                    && Integer.valueOf(month).equals(todo.getScopeMonth())) {
                result.add(todo);
            }
        }
        result.sort(defaultComparator());
        return result;
    }

    private Comparator<Todo> defaultComparator() {
        return Comparator
                .comparingInt((Todo todo) -> switch (todo.getScope()) {
                    case DAY -> 0;
                    case WEEK -> 1;
                    case MONTH -> 2;
                    case LATER -> 3;
                })
                .thenComparing(Todo::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(todo -> todo.getDueTime() == null ? 1 : 0)
                .thenComparing(Todo::getDueTime, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Todo::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Todo::getId);
    }
}
