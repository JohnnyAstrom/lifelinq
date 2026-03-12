package app.lifelinq.features.shopping.api;

public record ShoppingCategoryPreferenceResponse(
        String normalizedTitle,
        String preferredCategory
) {}
