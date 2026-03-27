package app.lifelinq.features.meals.contract;

public record RecipeAssetIntakeReference(
        RecipeAssetIntakeKind kind,
        String referenceId,
        String sourceLabel,
        String originalFilename,
        String mimeType
) {
    public RecipeAssetIntakeReference {
        if (kind == null) {
            throw new IllegalArgumentException("kind must not be null");
        }
        referenceId = normalizeRequired(referenceId, "referenceId");
        sourceLabel = normalizeOptional(sourceLabel);
        originalFilename = normalizeOptional(originalFilename);
        mimeType = normalizeOptional(mimeType);
    }

    public String effectiveSourceLabel() {
        return sourceLabel != null ? sourceLabel : originalFilename;
    }

    private static String normalizeRequired(String value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }

    private static String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
