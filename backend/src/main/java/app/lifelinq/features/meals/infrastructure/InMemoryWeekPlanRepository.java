package app.lifelinq.features.meals.infrastructure;

import app.lifelinq.features.meals.domain.WeekPlan;
import app.lifelinq.features.meals.domain.WeekPlanRepository;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryWeekPlanRepository implements WeekPlanRepository {
    private final ConcurrentMap<UUID, WeekPlan> byId = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, UUID> byGroupWeek = new ConcurrentHashMap<>();

    @Override
    public WeekPlan save(WeekPlan weekPlan) {
        if (weekPlan == null) {
            throw new IllegalArgumentException("weekPlan must not be null");
        }
        byId.put(weekPlan.getId(), weekPlan);
        byGroupWeek.put(key(weekPlan.getGroupId(), weekPlan.getYear(), weekPlan.getIsoWeek()), weekPlan.getId());
        return weekPlan;
    }

    @Override
    public Optional<WeekPlan> findByGroupAndWeek(UUID groupId, int year, int isoWeek) {
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        UUID id = byGroupWeek.get(key(groupId, year, isoWeek));
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

    private String key(UUID groupId, int year, int isoWeek) {
        return groupId + ":" + year + ":" + isoWeek;
    }
}
