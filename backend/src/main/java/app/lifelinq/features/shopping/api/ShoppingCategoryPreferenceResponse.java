package app.lifelinq.features.shopping.api;

public record ShoppingCategoryPreferenceResponse(
        String listType,
        String normalizedTitle,
        String preferredCategory
) {}
