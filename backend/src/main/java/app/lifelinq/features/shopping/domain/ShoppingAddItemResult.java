package app.lifelinq.features.shopping.domain;

import java.util.UUID;

public record ShoppingAddItemResult(
        UUID itemId,
        ShoppingAddItemOutcome outcome
) {}
