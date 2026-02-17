package app.lifelinq.features.shopping.infrastructure;

import app.lifelinq.features.shopping.domain.ShoppingList;
import app.lifelinq.features.shopping.domain.ShoppingListRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class InMemoryShoppingListRepository implements ShoppingListRepository {
    private final Map<UUID, ShoppingList> lists = new HashMap<>();

    @Override
    public ShoppingList save(ShoppingList list) {
        if (list == null) {
            throw new IllegalArgumentException("list must not be null");
        }
        lists.put(list.getId(), list);
        return list;
    }

    @Override
    public Optional<ShoppingList> findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        return Optional.ofNullable(lists.get(id));
    }

    @Override
    public List<ShoppingList> findByHouseholdId(UUID householdId) {
        if (householdId == null) {
            throw new IllegalArgumentException("householdId must not be null");
        }
        List<ShoppingList> result = new ArrayList<>();
        for (ShoppingList list : lists.values()) {
            if (householdId.equals(list.getHouseholdId())) {
                result.add(list);
            }
        }
        return result;
    }

    @Override
    public void deleteById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        lists.remove(id);
    }

    int size() {
        return lists.size();
    }
}
