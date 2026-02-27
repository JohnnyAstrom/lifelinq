package app.lifelinq.features.meals.contract;

import java.math.BigDecimal;
import java.util.UUID;

public interface MealsShoppingPort {
    void addShoppingItem(
            UUID groupId,
            UUID actorUserId,
            UUID listId,
            String itemName,
            BigDecimal quantity,
            String unitName
    );
}
