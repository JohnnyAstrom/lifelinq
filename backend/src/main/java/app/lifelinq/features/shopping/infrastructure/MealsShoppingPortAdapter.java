package app.lifelinq.features.shopping.infrastructure;

import app.lifelinq.features.meals.application.MealsShoppingPort;
import app.lifelinq.features.meals.application.MealsShoppingAccessDeniedException;
import app.lifelinq.features.meals.application.MealsShoppingDuplicateItemException;
import app.lifelinq.features.meals.application.MealsShoppingListNotFoundException;
import app.lifelinq.features.meals.domain.IngredientUnit;
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
            UUID householdId,
            UUID actorUserId,
            UUID listId,
            String itemName,
            BigDecimal quantity,
            IngredientUnit unit
    ) {
        try {
            shoppingApplicationService.addShoppingItem(
                    householdId,
                    actorUserId,
                    listId,
                    itemName,
                    quantity,
                    toShoppingUnit(unit)
            );
        } catch (AccessDeniedException ex) {
            throw new MealsShoppingAccessDeniedException(ex.getMessage());
        } catch (ShoppingListNotFoundException ex) {
            throw new MealsShoppingListNotFoundException(ex.getMessage());
        } catch (DuplicateShoppingItemNameException ex) {
            throw new MealsShoppingDuplicateItemException(ex.getMessage());
        }
    }

    private ShoppingUnit toShoppingUnit(IngredientUnit unit) {
        if (unit == null) {
            return null;
        }
        return ShoppingUnit.valueOf(unit.name());
    }
}
