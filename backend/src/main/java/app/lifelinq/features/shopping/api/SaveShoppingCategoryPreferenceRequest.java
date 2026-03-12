package app.lifelinq.features.shopping.api;

public final class SaveShoppingCategoryPreferenceRequest {
    private String normalizedTitle;
    private String preferredCategory;

    public String getNormalizedTitle() {
        return normalizedTitle;
    }

    public String getPreferredCategory() {
        return preferredCategory;
    }
}
