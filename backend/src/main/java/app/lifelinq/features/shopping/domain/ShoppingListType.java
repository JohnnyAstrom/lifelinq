package app.lifelinq.features.shopping.domain;

import java.util.Locale;

public enum ShoppingListType {
    GROCERY("grocery"),
    CONSUMABLES("consumables"),
    SUPPLIES("supplies"),
    MIXED("mixed");

    private final String key;

    ShoppingListType(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

    public static ShoppingListType fromKey(String rawKey) {
        if (rawKey == null || rawKey.isBlank()) {
            return MIXED;
        }
        String normalized = rawKey.trim().toLowerCase(Locale.ROOT);
        for (ShoppingListType value : values()) {
            if (value.key.equals(normalized)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown shopping list type: " + rawKey);
    }
}
