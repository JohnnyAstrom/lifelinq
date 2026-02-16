package app.lifelinq.features.todo.domain;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface TodoRepository {
    Optional<Todo> findById(UUID id);

    void save(Todo todo);

    java.util.List<Todo> findAll();

    java.util.List<Todo> findByHouseholdIdAndDueDateBetween(UUID householdId, LocalDate startDate, LocalDate endDate);
}
