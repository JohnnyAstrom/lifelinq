package app.lifelinq.features.todo.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoJpaRepository extends JpaRepository<TodoEntity, UUID> {
    List<TodoEntity> findByHouseholdIdAndDeletedAtIsNullAndDueDateBetweenOrderByDueDateAscIdAsc(
            UUID householdId,
            LocalDate startDate,
            LocalDate endDate
    );
}
