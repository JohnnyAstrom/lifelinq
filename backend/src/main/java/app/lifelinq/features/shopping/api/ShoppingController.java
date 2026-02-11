package app.lifelinq.features.shopping.api;

import app.lifelinq.config.RequestContext;
import app.lifelinq.config.RequestContextHolder;
import app.lifelinq.features.shopping.application.ShoppingApplicationService;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> create(@RequestBody CreateShoppingItemRequest request) {
        RequestContext context = RequestContextHolder.getCurrent();
        if (context == null || context.getHouseholdId() == null) {
            return ResponseEntity.badRequest().body("Missing household context");
        }
        UUID itemId = shoppingApplicationService.addItem(context.getHouseholdId(), request.getName());
        return ResponseEntity.ok(new CreateShoppingItemResponse(itemId));
    }
}
