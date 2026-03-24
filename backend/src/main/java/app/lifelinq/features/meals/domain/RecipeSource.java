package app.lifelinq.features.meals.domain;

public record RecipeSource(String sourceName, String sourceUrl) {
    public RecipeSource {
        sourceName = normalize(sourceName);
        sourceUrl = normalize(sourceUrl);
    }

    public boolean isEmpty() {
        return sourceName == null && sourceUrl == null;
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
