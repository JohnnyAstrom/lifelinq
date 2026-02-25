package app.lifelinq.features.shopping.infrastructure;

import app.lifelinq.features.shopping.domain.ShoppingItem;
import app.lifelinq.features.shopping.domain.ShoppingItemStatus;
import app.lifelinq.features.shopping.domain.ShoppingList;
import app.lifelinq.features.shopping.domain.ShoppingUnit;
import java.util.ArrayList;
import java.util.List;

final class ShoppingListMapper {

    ShoppingListEntity toEntity(ShoppingList list) {
        ShoppingListEntity entity = new ShoppingListEntity(
                list.getId(),
                list.getGroupId(),
                list.getName(),
                list.getOrderIndex(),
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
                entity.getGroupId(),
                entity.getName(),
                entity.getOrderIndex(),
                entity.getCreatedAt(),
                items
        );
    }

    private ShoppingItemEntity toEntity(ShoppingItem item, ShoppingListEntity list) {
        return new ShoppingItemEntity(
                item.getId(),
                list,
                item.getName(),
                item.getOrderIndex(),
                toEntityStatus(item.getStatus()),
                item.getQuantity(),
                toEntityUnit(item.getUnit()),
                item.getCreatedAt(),
                item.getBoughtAt()
        );
    }

    private ShoppingItem toDomain(ShoppingItemEntity entity) {
        return ShoppingItem.rehydrate(
                entity.getId(),
                entity.getName(),
                entity.getOrderIndex() != null ? entity.getOrderIndex() : 0,
                entity.getCreatedAt(),
                toDomainStatus(entity.getStatus()),
                entity.getBoughtAt(),
                entity.getQuantity(),
                toDomainUnit(entity.getUnit())
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

    private ShoppingUnitEntity toEntityUnit(ShoppingUnit unit) {
        if (unit == null) {
            return null;
        }
        return ShoppingUnitEntity.valueOf(unit.name());
    }

    private ShoppingUnit toDomainUnit(ShoppingUnitEntity unit) {
        if (unit == null) {
            return null;
        }
        return ShoppingUnit.valueOf(unit.name());
    }
}
