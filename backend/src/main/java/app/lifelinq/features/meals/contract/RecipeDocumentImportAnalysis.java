package app.lifelinq.features.meals.contract;

public record RecipeDocumentImportAnalysis(
        RecipeDocumentImportStrategy strategy,
        String extractedText
) {
    public RecipeDocumentImportAnalysis {
        if (strategy == null) {
            throw new IllegalArgumentException("strategy must not be null");
        }
        extractedText = normalizeOptional(extractedText);
    }

    private static String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
