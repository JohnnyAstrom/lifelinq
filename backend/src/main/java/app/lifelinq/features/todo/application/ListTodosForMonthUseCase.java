package app.lifelinq.features.todo.application;

import app.lifelinq.features.todo.domain.Todo;
import app.lifelinq.features.todo.domain.TodoRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

final class ListTodosForMonthUseCase {
    private final TodoRepository todoRepository;

    ListTodosForMonthUseCase(TodoRepository todoRepository) {
        if (todoRepository == null) {
            throw new IllegalArgumentException("todoRepository must not be null");
        }
        this.todoRepository = todoRepository;
    }

    ListTodosResult execute(TodoMonthQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("query must not be null");
        }
        if (query.getHouseholdId() == null) {
            throw new IllegalArgumentException("householdId must not be null");
        }
        YearMonth yearMonth = YearMonth.of(query.getYear(), query.getMonth());
        LocalDate startDate = toUtcDate(yearMonth.atDay(1));
        LocalDate endDate = toUtcDate(yearMonth.atEndOfMonth());
        List<Todo> todos = todoRepository.findByHouseholdIdAndDueDateBetween(
                query.getHouseholdId(),
                startDate,
                endDate
        );
        return new ListTodosResult(todos);
    }

    private LocalDate toUtcDate(LocalDate date) {
        ZonedDateTime utc = date.atStartOfDay(ZoneOffset.UTC);
        return utc.toLocalDate();
    }
}
