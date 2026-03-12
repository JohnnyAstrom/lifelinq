package app.lifelinq.features.shopping.infrastructure;

import app.lifelinq.features.shopping.domain.ShoppingCategoryPreference;
import app.lifelinq.features.shopping.domain.ShoppingCategoryPreferenceRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class InMemoryShoppingCategoryPreferenceRepository implements ShoppingCategoryPreferenceRepository {
    private final Map<String, ShoppingCategoryPreference> preferences = new HashMap<>();

    @Override
    public ShoppingCategoryPreference save(ShoppingCategoryPreference preference) {
        if (preference == null) {
            throw new IllegalArgumentException("preference must not be null");
        }
        preferences.put(key(preference.groupId(), preference.normalizedTitle()), preference);
        return preference;
    }

    @Override
    public List<ShoppingCategoryPreference> findByGroupId(UUID groupId) {
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        List<ShoppingCategoryPreference> result = new ArrayList<>();
        for (ShoppingCategoryPreference preference : preferences.values()) {
            if (groupId.equals(preference.groupId())) {
                result.add(preference);
            }
        }
        return result;
    }

    @Override
    public Optional<ShoppingCategoryPreference> findByGroupIdAndNormalizedTitle(UUID groupId, String normalizedTitle) {
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        if (normalizedTitle == null || normalizedTitle.isBlank()) {
            throw new IllegalArgumentException("normalizedTitle must not be blank");
        }
        return Optional.ofNullable(preferences.get(key(groupId, normalizedTitle)));
    }

    private String key(UUID groupId, String normalizedTitle) {
        return groupId + "::" + normalizedTitle;
    }
}
