package app.lifelinq.features.shopping.domain;

import java.util.Locale;

public enum ShoppingCategory {
    PRODUCE("produce"),
    DAIRY("dairy"),
    BAKERY("bakery"),
    MEAT_SEAFOOD("meat-seafood"),
    PANTRY("pantry"),
    FROZEN("frozen"),
    SNACKS_DRINKS("snacks-drinks"),
    HOUSEHOLD("household"),
    HEALTH_BEAUTY("health-beauty"),
    OTHER("other");

    private final String key;

    ShoppingCategory(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

    public static ShoppingCategory fromKey(String rawKey) {
        if (rawKey == null || rawKey.isBlank()) {
            throw new IllegalArgumentException("preferredCategory must not be blank");
        }
        String normalized = rawKey.trim().toLowerCase(Locale.ROOT);
        for (ShoppingCategory category : values()) {
            if (category.key.equals(normalized)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown shopping category: " + rawKey);
    }
}
