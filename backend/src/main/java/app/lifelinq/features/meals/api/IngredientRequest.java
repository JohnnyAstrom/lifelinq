package app.lifelinq.features.meals.api;

import app.lifelinq.features.shopping.domain.ShoppingUnit;
import java.math.BigDecimal;

public final class IngredientRequest {
    private String name;
    private BigDecimal quantity;
    private ShoppingUnit unit;
    private Integer position;

    public String getName() {
        return name;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public ShoppingUnit getUnit() {
        return unit;
    }

    public Integer getPosition() {
        return position;
    }
}
