package app.lifelinq.features.shopping.api;

public final class UpdateShoppingItemRequest {
    private String name;
    private java.math.BigDecimal quantity;
    private String unit;

    public String getName() {
        return name;
    }

    public java.math.BigDecimal getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }
}
