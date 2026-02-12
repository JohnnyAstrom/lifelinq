package app.lifelinq.features.meals.infrastructure;

import app.lifelinq.features.meals.domain.PlannedMeal;
import app.lifelinq.features.meals.domain.WeekPlan;
import app.lifelinq.features.meals.domain.WeekPlanRepository;
import java.util.Optional;
import java.util.UUID;

public final class JpaWeekPlanRepositoryAdapter implements WeekPlanRepository {
    private final WeekPlanJpaRepository repository;
    private final WeekPlanMapper mapper;

    public JpaWeekPlanRepositoryAdapter(
            WeekPlanJpaRepository repository,
            WeekPlanMapper mapper
    ) {
        if (repository == null) {
            throw new IllegalArgumentException("repository must not be null");
        }
        if (mapper == null) {
            throw new IllegalArgumentException("mapper must not be null");
        }
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public WeekPlan save(WeekPlan weekPlan) {
        if (weekPlan == null) {
            throw new IllegalArgumentException("weekPlan must not be null");
        }
        WeekPlanEntity entity = repository.findById(weekPlan.getId())
                .map(existing -> updateEntity(existing, weekPlan))
                .orElseGet(() -> mapper.toEntity(weekPlan));
        WeekPlanEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<WeekPlan> findByHouseholdAndWeek(UUID householdId, int year, int isoWeek) {
        if (householdId == null) {
            throw new IllegalArgumentException("householdId must not be null");
        }
        return repository.findByHouseholdIdAndYearAndIsoWeek(householdId, year, isoWeek)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<WeekPlan> findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        return repository.findById(id).map(mapper::toDomain);
    }

    private WeekPlanEntity updateEntity(WeekPlanEntity entity, WeekPlan weekPlan) {
        if (!entity.getHouseholdId().equals(weekPlan.getHouseholdId())) {
            throw new IllegalArgumentException("householdId cannot be changed");
        }
        if (entity.getYear() != weekPlan.getYear()) {
            throw new IllegalArgumentException("year cannot be changed");
        }
        if (entity.getIsoWeek() != weekPlan.getIsoWeek()) {
            throw new IllegalArgumentException("isoWeek cannot be changed");
        }
        if (!entity.getCreatedAt().equals(weekPlan.getCreatedAt())) {
            throw new IllegalArgumentException("createdAt cannot be changed");
        }
        entity.getMeals().clear();
        for (PlannedMeal meal : weekPlan.getMeals()) {
            entity.getMeals().add(mapper.toEntity(meal, entity));
        }
        return entity;
    }
}
