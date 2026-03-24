package app.lifelinq.features.meals.domain;

public record RecipeProvenance(RecipeOriginKind originKind, String referenceUrl) {
    public RecipeProvenance {
        originKind = originKind == null ? RecipeOriginKind.MANUAL : originKind;
        referenceUrl = normalize(referenceUrl);
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
