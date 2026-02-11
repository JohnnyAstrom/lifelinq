package app.lifelinq.features.shopping.application;

import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class ShoppingApplicationService {
    private final AddShoppingItemUseCase addShoppingItemUseCase;

    public ShoppingApplicationService() {
        this.addShoppingItemUseCase = new AddShoppingItemUseCase();
    }

    @Transactional
    public AddShoppingItemResult addItem(UUID householdId, String name) {
        AddShoppingItemCommand command = new AddShoppingItemCommand(householdId, name);
        return addShoppingItemUseCase.execute(command);
    }
}
