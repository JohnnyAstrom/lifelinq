package app.lifelinq.features.shopping.infrastructure;

import app.lifelinq.features.shopping.domain.ShoppingItem;
import app.lifelinq.features.shopping.domain.ShoppingItemStatus;
import app.lifelinq.features.shopping.domain.ShoppingList;
import java.util.ArrayList;
import java.util.List;

final class ShoppingListMapper {

    ShoppingListEntity toEntity(ShoppingList list) {
        ShoppingListEntity entity = new ShoppingListEntity(
                list.getId(),
                list.getHouseholdId(),
                list.getName(),
                list.getCreatedAt()
        );
        for (ShoppingItem item : list.getItems()) {
            entity.getItems().add(toEntity(item, entity));
        }
        return entity;
    }

    ShoppingList toDomain(ShoppingListEntity entity) {
        List<ShoppingItem> items = new ArrayList<>();
        for (ShoppingItemEntity item : entity.getItems()) {
            items.add(toDomain(item));
        }
        return new ShoppingList(
                entity.getId(),
                entity.getHouseholdId(),
                entity.getName(),
                entity.getCreatedAt(),
                items
        );
    }

    private ShoppingItemEntity toEntity(ShoppingItem item, ShoppingListEntity list) {
        return new ShoppingItemEntity(
                item.getId(),
                list,
                item.getName(),
                toEntityStatus(item.getStatus()),
                item.getCreatedAt(),
                item.getBoughtAt()
        );
    }

    private ShoppingItem toDomain(ShoppingItemEntity entity) {
        return ShoppingItem.rehydrate(
                entity.getId(),
                entity.getName(),
                entity.getCreatedAt(),
                toDomainStatus(entity.getStatus()),
                entity.getBoughtAt()
        );
    }

    private ShoppingItemStatusEntity toEntityStatus(ShoppingItemStatus status) {
        if (status == ShoppingItemStatus.BOUGHT) {
            return ShoppingItemStatusEntity.BOUGHT;
        }
        return ShoppingItemStatusEntity.TO_BUY;
    }

    private ShoppingItemStatus toDomainStatus(ShoppingItemStatusEntity status) {
        if (status == ShoppingItemStatusEntity.BOUGHT) {
            return ShoppingItemStatus.BOUGHT;
        }
        return ShoppingItemStatus.TO_BUY;
    }
}
