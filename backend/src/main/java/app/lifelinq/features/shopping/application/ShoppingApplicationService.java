package app.lifelinq.features.shopping.application;

import app.lifelinq.features.shopping.domain.ShoppingItemRepository;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class ShoppingApplicationService {
    private final AddShoppingItemUseCase addShoppingItemUseCase;

    public ShoppingApplicationService(ShoppingItemRepository repository) {
        this.addShoppingItemUseCase = new AddShoppingItemUseCase(repository);
    }

    @Transactional
    public AddShoppingItemResult addItem(UUID householdId, String name) {
        AddShoppingItemCommand command = new AddShoppingItemCommand(householdId, name);
        return addShoppingItemUseCase.execute(command);
    }
}
