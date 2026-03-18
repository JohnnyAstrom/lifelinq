package app.lifelinq.features.meals.domain;

import java.math.BigDecimal;
import java.util.UUID;

public final class Ingredient {
    private final UUID id;
    private final String name;
    private final String rawText;
    private final BigDecimal quantity;
    private final IngredientUnit unit;
    private final int position;

    public Ingredient(UUID id, String name, BigDecimal quantity, IngredientUnit unit, int position) {
        this(id, name, null, quantity, unit, position);
    }

    public Ingredient(UUID id, String name, String rawText, BigDecimal quantity, IngredientUnit unit, int position) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (quantity == null && unit != null) {
            throw new IllegalArgumentException("unit requires quantity");
        }
        if (quantity != null && quantity.signum() <= 0) {
            throw new IllegalArgumentException("quantity must be greater than zero");
        }
        if (position < 1) {
            throw new IllegalArgumentException("position must be greater than or equal to 1");
        }
        this.id = id;
        this.name = normalizeRequiredName(name);
        this.rawText = normalizeOptionalText(rawText);
        this.quantity = quantity;
        this.unit = unit;
        this.position = position;
    }

    private static String normalizeRequiredName(String value) {
        String normalized = normalizeOptionalText(value);
        if (normalized == null) {
            throw new IllegalArgumentException("name must not be blank");
        }
        return normalized;
    }

    private static String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().replaceAll("\\s+", " ");
        return normalized.isEmpty() ? null : normalized;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRawText() {
        return rawText;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public IngredientUnit getUnit() {
        return unit;
    }

    public int getPosition() {
        return position;
    }
}
