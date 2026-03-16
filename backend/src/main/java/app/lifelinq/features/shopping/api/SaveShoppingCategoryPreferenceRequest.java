package app.lifelinq.features.shopping.api;

public final class SaveShoppingCategoryPreferenceRequest {
    private String listType;
    private String normalizedTitle;
    private String preferredCategory;

    public String getListType() {
        return listType;
    }

    public String getNormalizedTitle() {
        return normalizedTitle;
    }

    public String getPreferredCategory() {
        return preferredCategory;
    }
}
