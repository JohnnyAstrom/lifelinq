package app.lifelinq.features.shopping.contract;

public record ShoppingCategoryPreferenceView(
        String listType,
        String normalizedTitle,
        String preferredCategory
) {}
