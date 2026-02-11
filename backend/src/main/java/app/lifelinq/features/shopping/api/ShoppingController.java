package app.lifelinq.features.shopping.api;

import app.lifelinq.features.shopping.application.ShoppingApplicationService;
import java.util.UUID;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ShoppingController {
    private final ShoppingApplicationService shoppingApplicationService;

    public ShoppingController(ShoppingApplicationService shoppingApplicationService) {
        this.shoppingApplicationService = shoppingApplicationService;
    }

    @PostMapping("/shopping-items")
    public CreateShoppingItemResponse create(@RequestBody CreateShoppingItemRequest request) {
        // TODO: Remove householdId from request once auth derives household context from token.
        UUID householdId = request.getHouseholdId();
        UUID itemId = shoppingApplicationService.addItem(householdId, request.getName());
        return new CreateShoppingItemResponse(itemId);
    }
}
