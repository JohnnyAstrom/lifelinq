package app.lifelinq.features.todo.infrastructure;

import app.lifelinq.features.todo.domain.Todo;
import app.lifelinq.features.todo.domain.TodoRepository;
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
    public List<Todo> findAll() {
        return new ArrayList<>(byId.values());
    }

    @Override
    public List<Todo> findByHouseholdIdAndDueDateBetween(UUID householdId, LocalDate startDate, LocalDate endDate) {
        if (householdId == null) {
            throw new IllegalArgumentException("householdId must not be null");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("startDate must not be null");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("endDate must not be null");
        }
        List<Todo> result = new ArrayList<>();
        for (Todo todo : byId.values()) {
            LocalDate dueDate = todo.getDueDate();
            if (!householdId.equals(todo.getHouseholdId())) {
                continue;
            }
            if (todo.isDeleted() || dueDate == null) {
                continue;
            }
            if ((dueDate.isEqual(startDate) || dueDate.isAfter(startDate))
                    && (dueDate.isEqual(endDate) || dueDate.isBefore(endDate))) {
                result.add(todo);
            }
        }
        result.sort(Comparator.comparing(Todo::getDueDate).thenComparing(Todo::getId));
        return result;
    }
}
