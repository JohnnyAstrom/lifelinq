package app.lifelinq.features.todo.infrastructure;

import app.lifelinq.features.todo.domain.Todo;
import app.lifelinq.features.todo.domain.TodoRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class JpaTodoRepositoryAdapter implements TodoRepository {
    private final TodoJpaRepository todoJpaRepository;
    private final TodoMapper mapper;

    public JpaTodoRepositoryAdapter(TodoJpaRepository todoJpaRepository, TodoMapper mapper) {
        if (todoJpaRepository == null) {
            throw new IllegalArgumentException("todoJpaRepository must not be null");
        }
        if (mapper == null) {
            throw new IllegalArgumentException("mapper must not be null");
        }
        this.todoJpaRepository = todoJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Todo> findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        return todoJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public void save(Todo todo) {
        TodoEntity entity = mapper.toEntity(todo);
        todoJpaRepository.save(entity);
    }

    @Override
    public List<Todo> findAll() {
        List<Todo> result = new ArrayList<>();
        for (TodoEntity entity : todoJpaRepository.findAll()) {
            result.add(mapper.toDomain(entity));
        }
        return result;
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
        for (TodoEntity entity : todoJpaRepository
                .findByHouseholdIdAndDeletedAtIsNullAndDueDateBetweenOrderByDueDateAscIdAsc(householdId, startDate, endDate)) {
            result.add(mapper.toDomain(entity));
        }
        return result;
    }
}
