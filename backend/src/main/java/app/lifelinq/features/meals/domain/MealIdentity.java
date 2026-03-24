package app.lifelinq.features.meals.domain;

import java.util.Locale;
import java.util.UUID;

public record MealIdentity(
        String key,
        MealIdentityKind kind,
        String title,
        UUID recipeId
) {
    public MealIdentity {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key must not be blank");
        }
        if (kind == null) {
            throw new IllegalArgumentException("kind must not be null");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (kind == MealIdentityKind.RECIPE && recipeId == null) {
            throw new IllegalArgumentException("recipeId must not be null for recipe identities");
        }
        if (kind == MealIdentityKind.TITLE_ONLY && recipeId != null) {
            throw new IllegalArgumentException("recipeId must be null for title-only identities");
        }
        key = key.trim();
        title = title.trim();
    }

    public static MealIdentity forRecipe(UUID recipeId, String title) {
        if (recipeId == null) {
            throw new IllegalArgumentException("recipeId must not be null");
        }
        return new MealIdentity(recipeKey(recipeId), MealIdentityKind.RECIPE, requireTitle(title), recipeId);
    }

    public static MealIdentity forTitle(String title) {
        String normalizedTitle = requireTitle(title);
        return new MealIdentity(titleKey(normalizedTitle), MealIdentityKind.TITLE_ONLY, normalizedTitle, null);
    }

    public static String recipeKey(UUID recipeId) {
        if (recipeId == null) {
            throw new IllegalArgumentException("recipeId must not be null");
        }
        return "recipe:" + recipeId;
    }

    public static String titleKey(String title) {
        return "title:" + normalizeComparableTitle(requireTitle(title));
    }

    public static String normalizeComparableTitle(String title) {
        if (title == null) {
            throw new IllegalArgumentException("title must not be null");
        }
        String normalized = title.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        return normalized;
    }

    private static String requireTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        return title.trim();
    }
}
