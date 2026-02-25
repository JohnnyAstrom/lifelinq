package app.lifelinq.features.meals.infrastructure;

import app.lifelinq.features.meals.domain.PlannedMeal;
import app.lifelinq.features.meals.domain.WeekPlan;
import app.lifelinq.features.meals.domain.WeekPlanRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;

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
        try {
            WeekPlanEntity entity = repository.findById(weekPlan.getId())
                    .map(existing -> updateEntity(existing, weekPlan))
                    .orElseGet(() -> mapper.toEntity(weekPlan));
            WeekPlanEntity saved = repository.save(entity);
            return mapper.toDomain(saved);
        } catch (DataIntegrityViolationException ex) {
            return resolveUniqueConstraintConflict(weekPlan, ex);
        }
    }

    @Override
    public Optional<WeekPlan> findByGroupAndWeek(UUID groupId, int year, int isoWeek) {
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        return repository.findByGroupIdAndYearAndIsoWeek(groupId, year, isoWeek)
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
        if (!entity.getGroupId().equals(weekPlan.getGroupId())) {
            throw new IllegalArgumentException("groupId cannot be changed");
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

    private WeekPlan resolveUniqueConstraintConflict(WeekPlan weekPlan, DataIntegrityViolationException ex) {
        Optional<WeekPlanEntity> existingEntity = repository.findByGroupIdAndYearAndIsoWeek(
                weekPlan.getGroupId(),
                weekPlan.getYear(),
                weekPlan.getIsoWeek()
        );
        if (existingEntity.isEmpty()) {
            throw ex;
        }
        WeekPlan existing = mapper.toDomain(existingEntity.get());
        for (PlannedMeal meal : weekPlan.getMeals()) {
            existing.addOrReplaceMeal(meal.getDayOfWeek(), meal.getMealType(), meal.getRecipeId());
        }
        WeekPlanEntity merged = updateEntity(existingEntity.get(), existing);
        WeekPlanEntity saved = repository.save(merged);
        return mapper.toDomain(saved);
    }
}
