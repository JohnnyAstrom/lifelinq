package app.lifelinq.features.shopping.infrastructure;

import app.lifelinq.features.meals.contract.MealsShoppingPort;
import app.lifelinq.features.meals.contract.MealsShoppingAccessDeniedException;
import app.lifelinq.features.meals.contract.MealsShoppingDuplicateItemException;
import app.lifelinq.features.meals.contract.MealsShoppingListNotFoundException;
import app.lifelinq.features.shopping.application.AccessDeniedException;
import app.lifelinq.features.shopping.application.ShoppingApplicationService;
import app.lifelinq.features.shopping.domain.DuplicateShoppingItemNameException;
import app.lifelinq.features.shopping.domain.ShoppingListNotFoundException;
import app.lifelinq.features.shopping.domain.ShoppingUnit;
import java.math.BigDecimal;
import java.util.UUID;

public final class MealsShoppingPortAdapter implements MealsShoppingPort {
    private final ShoppingApplicationService shoppingApplicationService;

    public MealsShoppingPortAdapter(ShoppingApplicationService shoppingApplicationService) {
        this.shoppingApplicationService = shoppingApplicationService;
    }

    @Override
    public void addShoppingItem(
            UUID groupId,
            UUID actorUserId,
            UUID listId,
            String itemName,
            BigDecimal quantity,
            String unitName
    ) {
        try {
            shoppingApplicationService.addShoppingItem(
                    groupId,
                    actorUserId,
                    listId,
                    itemName,
                    quantity,
                    toShoppingUnit(unitName)
            );
        } catch (AccessDeniedException ex) {
            throw new MealsShoppingAccessDeniedException(ex.getMessage());
        } catch (ShoppingListNotFoundException ex) {
            throw new MealsShoppingListNotFoundException(ex.getMessage());
        } catch (DuplicateShoppingItemNameException ex) {
            throw new MealsShoppingDuplicateItemException(ex.getMessage());
        }
    }

    private ShoppingUnit toShoppingUnit(String unitName) {
        if (unitName == null) {
            return null;
        }
        return ShoppingUnit.valueOf(unitName);
    }
}
