package app.lifelinq.features.meals.api;

import app.lifelinq.features.meals.domain.IngredientUnit;
import java.math.BigDecimal;

public final class IngredientRequest {
    private String name;
    private String rawText;
    private BigDecimal quantity;
    private IngredientUnit unit;
    private Integer position;

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

    public Integer getPosition() {
        return position;
    }
}
