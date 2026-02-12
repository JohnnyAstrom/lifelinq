package app.lifelinq.features.shopping.domain;

public final class DuplicateShoppingItemNameException extends RuntimeException {
    public DuplicateShoppingItemNameException(String normalizedName) {
        super("item name must be unique within list: " + normalizedName);
    }
}
