package app.lifelinq.features.meals.contract;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface MealsShoppingPort {
    void addShoppingItem(
            UUID groupId,
            UUID actorUserId,
            UUID listId,
            String itemName,
            BigDecimal quantity,
            String unitName,
            String sourceKind,
            String sourceLabel
    );

    Map<UUID, MealsShoppingListSnapshot> listShoppingListSnapshots(
            UUID groupId,
            UUID actorUserId,
            Set<UUID> listIds
    );
}
