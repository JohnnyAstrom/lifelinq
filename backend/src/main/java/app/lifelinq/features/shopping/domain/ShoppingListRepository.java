package app.lifelinq.features.shopping.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShoppingListRepository {
    ShoppingList save(ShoppingList list);

    Optional<ShoppingList> findById(UUID id);

    List<ShoppingList> findByGroupId(UUID groupId);

    void deleteById(UUID id);
}
