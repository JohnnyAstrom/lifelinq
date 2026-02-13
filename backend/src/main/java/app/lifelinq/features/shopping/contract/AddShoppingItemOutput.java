package app.lifelinq.features.shopping.contract;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AddShoppingItemOutput(
        UUID listId,
        UUID itemId,
        String name,
        ShoppingItemStatusView status,
        BigDecimal quantity,
        ShoppingUnitView unit,
        Instant createdAt,
        Instant boughtAt
) {}
