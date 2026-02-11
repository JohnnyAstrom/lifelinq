package app.lifelinq.features.shopping.domain;

import java.util.Optional;
import java.util.UUID;

public interface ShoppingItemRepository {
    void save(ShoppingItem item);

    Optional<ShoppingItem> findById(UUID id);
}
