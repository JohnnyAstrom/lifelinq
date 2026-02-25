package app.lifelinq.features.meals.infrastructure;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeekPlanJpaRepository extends JpaRepository<WeekPlanEntity, UUID> {

    @EntityGraph(attributePaths = "meals")
    Optional<WeekPlanEntity> findByGroupIdAndYearAndIsoWeek(UUID groupId, int year, int isoWeek);

    @Override
    @EntityGraph(attributePaths = "meals")
    Optional<WeekPlanEntity> findById(UUID id);
}
