package app.lifelinq.features.todo.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TodoRepository {
    Optional<Todo> findById(UUID id);

    void save(Todo todo);

    List<Todo> listByGroup(UUID groupId, TodoStatus statusFilter);

    List<Todo> listForMonth(UUID groupId, int year, int month, LocalDate startDate, LocalDate endDate);
}
