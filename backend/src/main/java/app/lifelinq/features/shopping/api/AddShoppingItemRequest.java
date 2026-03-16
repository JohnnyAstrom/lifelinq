package app.lifelinq.features.shopping.api;

public final class AddShoppingItemRequest {
    private String name;
    private java.math.BigDecimal quantity;
    private String unit;
    private Boolean addAsNew;

    public String getName() {
        return name;
    }

    public java.math.BigDecimal getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public Boolean getAddAsNew() {
        return addAsNew;
    }
}
