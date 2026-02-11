package app.lifelinq.features.shopping.api;

import java.util.UUID;

public final class CreateShoppingItemRequest {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
