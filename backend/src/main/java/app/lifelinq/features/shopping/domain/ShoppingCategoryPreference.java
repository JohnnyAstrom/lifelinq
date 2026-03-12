package app.lifelinq.features.shopping.domain;

import java.time.Instant;
import java.util.UUID;

public record ShoppingCategoryPreference(
        UUID groupId,
        ShoppingListType listType,
        String normalizedTitle,
        ShoppingCategory preferredCategory,
        Instant updatedAt
) {
    public ShoppingCategoryPreference {
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        if (normalizedTitle == null || normalizedTitle.isBlank()) {
            throw new IllegalArgumentException("normalizedTitle must not be blank");
        }
        if (listType == null) {
            throw new IllegalArgumentException("listType must not be null");
        }
        if (preferredCategory == null) {
            throw new IllegalArgumentException("preferredCategory must not be null");
        }
        if (updatedAt == null) {
            throw new IllegalArgumentException("updatedAt must not be null");
        }
    }
}
