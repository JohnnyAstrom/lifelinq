package app.lifelinq.features.shopping.infrastructure;

import app.lifelinq.features.shopping.domain.ShoppingItem;
import app.lifelinq.features.shopping.domain.ShoppingItemRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class InMemoryShoppingItemRepository implements ShoppingItemRepository {
    private final Map<UUID, ShoppingItem> items = new HashMap<>();

    @Override
    public void save(ShoppingItem item) {
        if (item == null) {
            throw new IllegalArgumentException("item must not be null");
        }
        items.put(item.getId(), item);
    }

    int size() {
        return items.size();
    }
}
