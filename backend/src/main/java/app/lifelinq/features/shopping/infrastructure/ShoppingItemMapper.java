package app.lifelinq.features.shopping.infrastructure;

import app.lifelinq.features.shopping.domain.ShoppingItem;

final class ShoppingItemMapper {

    ShoppingItemEntity toEntity(ShoppingItem item) {
        return new ShoppingItemEntity(
                item.getId(),
                item.getHouseholdId(),
                item.getName(),
                item.getCreatedAt()
        );
    }

    ShoppingItem toDomain(ShoppingItemEntity entity) {
        return new ShoppingItem(
                entity.getId(),
                entity.getHouseholdId(),
                entity.getName(),
                entity.getCreatedAt()
        );
    }
}
