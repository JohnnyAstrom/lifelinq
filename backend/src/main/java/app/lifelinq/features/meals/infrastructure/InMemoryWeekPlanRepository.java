package app.lifelinq.features.meals.infrastructure;

import app.lifelinq.features.meals.domain.WeekPlan;
import app.lifelinq.features.meals.domain.WeekPlanRepository;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryWeekPlanRepository implements WeekPlanRepository {
    private final ConcurrentMap<UUID, WeekPlan> byId = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, UUID> byHouseholdWeek = new ConcurrentHashMap<>();

    @Override
    public WeekPlan save(WeekPlan weekPlan) {
        if (weekPlan == null) {
            throw new IllegalArgumentException("weekPlan must not be null");
        }
        byId.put(weekPlan.getId(), weekPlan);
        byHouseholdWeek.put(key(weekPlan.getHouseholdId(), weekPlan.getYear(), weekPlan.getIsoWeek()), weekPlan.getId());
        return weekPlan;
    }

    @Override
    public Optional<WeekPlan> findByHouseholdAndWeek(UUID householdId, int year, int isoWeek) {
        if (householdId == null) {
            throw new IllegalArgumentException("householdId must not be null");
        }
        UUID id = byHouseholdWeek.get(key(householdId, year, isoWeek));
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public Optional<WeekPlan> findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        return Optional.ofNullable(byId.get(id));
    }

    private String key(UUID householdId, int year, int isoWeek) {
        return householdId + ":" + year + ":" + isoWeek;
    }
}
