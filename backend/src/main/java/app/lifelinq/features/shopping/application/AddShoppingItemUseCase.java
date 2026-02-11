package app.lifelinq.features.shopping.application;

import app.lifelinq.features.shopping.domain.ShoppingItem;
import app.lifelinq.features.shopping.domain.ShoppingItemRepository;
import java.time.Instant;
import java.util.UUID;

final class AddShoppingItemUseCase {
    private final ShoppingItemRepository repository;

    AddShoppingItemUseCase(ShoppingItemRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("repository must not be null");
        }
        this.repository = repository;
    }

    public AddShoppingItemResult execute(AddShoppingItemCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        if (command.getHouseholdId() == null) {
            throw new IllegalArgumentException("householdId must not be null");
        }
        if (command.getName() == null || command.getName().isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }

        ShoppingItem item = new ShoppingItem(
                UUID.randomUUID(),
                command.getHouseholdId(),
                command.getName(),
                Instant.now()
        );
        repository.save(item);
        return new AddShoppingItemResult(item.getId());
    }
}
