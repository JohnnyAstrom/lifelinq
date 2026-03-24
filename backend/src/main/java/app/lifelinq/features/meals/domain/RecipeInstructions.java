package app.lifelinq.features.meals.domain;

public record RecipeInstructions(String body) {
    public RecipeInstructions {
        body = normalize(body);
    }

    public boolean isEmpty() {
        return body == null;
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().replace("\r\n", "\n").replace("\r", "\n");
        return normalized.isEmpty() ? null : normalized;
    }
}
