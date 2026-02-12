package app.lifelinq.features.shopping.application;

import app.lifelinq.features.shopping.domain.ShoppingItemRepository;
import app.lifelinq.features.user.application.EnsureUserExistsUseCase;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class ShoppingApplicationService {
    private final AddShoppingItemUseCase addShoppingItemUseCase;
    private final EnsureUserExistsUseCase ensureUserExistsUseCase;

    public ShoppingApplicationService(
            ShoppingItemRepository repository,
            EnsureUserExistsUseCase ensureUserExistsUseCase
    ) {
        this.addShoppingItemUseCase = new AddShoppingItemUseCase(repository);
        this.ensureUserExistsUseCase = ensureUserExistsUseCase;
    }

    @Transactional
    public UUID addItem(UUID householdId, UUID actorUserId, String name) {
        ensureUserExistsUseCase.execute(actorUserId);
        AddShoppingItemCommand command = new AddShoppingItemCommand(householdId, name);
        return addShoppingItemUseCase.execute(command).getItemId();
    }
}
