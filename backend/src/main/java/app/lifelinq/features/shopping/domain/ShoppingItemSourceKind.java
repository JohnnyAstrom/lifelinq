package app.lifelinq.features.shopping.domain;

public enum ShoppingItemSourceKind {
    MEAL_PLAN;

    public String key() {
        return switch (this) {
            case MEAL_PLAN -> "meal-plan";
        };
    }

    public static ShoppingItemSourceKind fromKey(String rawKey) {
        if (rawKey == null || rawKey.isBlank()) {
            return null;
        }
        return switch (rawKey.trim().toLowerCase()) {
            case "meal-plan" -> MEAL_PLAN;
            default -> throw new IllegalArgumentException("Unsupported shopping item source kind: " + rawKey);
        };
    }
}
