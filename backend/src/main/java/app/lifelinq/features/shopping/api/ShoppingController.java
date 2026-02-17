package app.lifelinq.features.shopping.api;

import app.lifelinq.config.RequestContext;
import app.lifelinq.features.shopping.application.ShoppingApplicationService;
import app.lifelinq.features.shopping.contract.AddShoppingItemOutput;
import app.lifelinq.features.shopping.contract.CreateShoppingListOutput;
import app.lifelinq.features.shopping.contract.ShoppingItemView;
import app.lifelinq.features.shopping.contract.ShoppingListView;
import app.lifelinq.features.shopping.contract.ToggleShoppingItemOutput;
import app.lifelinq.features.shopping.domain.ShoppingUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ShoppingController {
    private final ShoppingApplicationService shoppingApplicationService;

    public ShoppingController(ShoppingApplicationService shoppingApplicationService) {
        this.shoppingApplicationService = shoppingApplicationService;
    }

    @PostMapping("/shopping-lists")
    public ResponseEntity<?> createList(@RequestBody CreateShoppingListRequest request) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getHouseholdId() == null) {
            return ApiScoping.missingContext();
        }
        if (context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        CreateShoppingListOutput output = shoppingApplicationService.createShoppingList(
                context.getHouseholdId(),
                context.getUserId(),
                request.getName()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateShoppingListResponse(output.listId(), output.name()));
    }

    @GetMapping("/shopping-lists")
    public ResponseEntity<?> listLists() {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getHouseholdId() == null) {
            return ApiScoping.missingContext();
        }
        if (context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        List<ShoppingListView> lists = shoppingApplicationService.listShoppingLists(
                context.getHouseholdId(),
                context.getUserId()
        );
        List<ShoppingListResponse> responses = new ArrayList<>();
        for (ShoppingListView list : lists) {
            responses.add(toResponse(list));
        }
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/shopping-lists/{listId}")
    public ResponseEntity<?> updateList(
            @PathVariable UUID listId,
            @RequestBody UpdateShoppingListRequest request
    ) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getHouseholdId() == null) {
            return ApiScoping.missingContext();
        }
        if (context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        ShoppingListView list = shoppingApplicationService.updateShoppingListName(
                context.getHouseholdId(),
                context.getUserId(),
                listId,
                request.getName()
        );
        return ResponseEntity.ok(toResponse(list));
    }

    @PatchMapping("/shopping-lists/{listId}/order")
    public ResponseEntity<?> reorderList(
            @PathVariable UUID listId,
            @RequestBody ReorderShoppingListRequest request
    ) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getHouseholdId() == null) {
            return ApiScoping.missingContext();
        }
        if (context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        shoppingApplicationService.reorderShoppingList(
                context.getHouseholdId(),
                context.getUserId(),
                listId,
                request.getDirection()
        );
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/shopping-lists/{listId}/items")
    public ResponseEntity<?> addItem(
            @PathVariable UUID listId,
            @RequestBody AddShoppingItemRequest request
    ) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getHouseholdId() == null) {
            return ApiScoping.missingContext();
        }
        if (context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        AddShoppingItemOutput output = shoppingApplicationService.addShoppingItem(
                context.getHouseholdId(),
                context.getUserId(),
                listId,
                request.getName(),
                request.getQuantity(),
                parseUnit(request.getUnit())
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AddShoppingItemResponse(
                        output.itemId(),
                        output.name(),
                        output.status().name(),
                        output.quantity(),
                        output.unit() != null ? output.unit().name() : null,
                        output.createdAt(),
                        output.boughtAt()
                ));
    }

    @PatchMapping("/shopping-lists/{listId}/items/{itemId}")
    public ResponseEntity<?> updateItem(
            @PathVariable UUID listId,
            @PathVariable UUID itemId,
            @RequestBody UpdateShoppingItemRequest request
    ) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getHouseholdId() == null) {
            return ApiScoping.missingContext();
        }
        if (context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        ShoppingItemView item = shoppingApplicationService.updateShoppingItem(
                context.getHouseholdId(),
                context.getUserId(),
                listId,
                itemId,
                request.getName(),
                request.getQuantity(),
                parseUnit(request.getUnit())
        );
        return ResponseEntity.ok(new ShoppingItemResponse(
                item.id(),
                item.name(),
                item.status().name(),
                item.quantity(),
                item.unit() != null ? item.unit().name() : null,
                item.createdAt(),
                item.boughtAt()
        ));
    }

    @PatchMapping("/shopping-lists/{listId}/items/{itemId}/toggle")
    public ResponseEntity<?> toggleItem(
            @PathVariable UUID listId,
            @PathVariable UUID itemId
    ) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getHouseholdId() == null) {
            return ApiScoping.missingContext();
        }
        if (context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        ToggleShoppingItemOutput output = shoppingApplicationService.toggleShoppingItem(
                context.getHouseholdId(),
                context.getUserId(),
                listId,
                itemId
        );
        return ResponseEntity.ok(new ToggleShoppingItemResponse(
                output.itemId(),
                output.status().name(),
                output.boughtAt()
        ));
    }

    @PatchMapping("/shopping-lists/{listId}/items/{itemId}/order")
    public ResponseEntity<?> reorderItem(
            @PathVariable UUID listId,
            @PathVariable UUID itemId,
            @RequestBody ReorderShoppingItemRequest request
    ) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getHouseholdId() == null) {
            return ApiScoping.missingContext();
        }
        if (context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        shoppingApplicationService.reorderShoppingItem(
                context.getHouseholdId(),
                context.getUserId(),
                listId,
                itemId,
                request.getDirection()
        );
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/shopping-lists/{listId}/items/{itemId}")
    public ResponseEntity<?> removeItem(
            @PathVariable UUID listId,
            @PathVariable UUID itemId
    ) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getHouseholdId() == null) {
            return ApiScoping.missingContext();
        }
        if (context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        shoppingApplicationService.removeShoppingItem(
                context.getHouseholdId(),
                context.getUserId(),
                listId,
                itemId
        );
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/shopping-lists/{listId}")
    public ResponseEntity<?> removeList(@PathVariable UUID listId) {
        RequestContext context = ApiScoping.getContext();
        if (context == null || context.getHouseholdId() == null) {
            return ApiScoping.missingContext();
        }
        if (context.getUserId() == null) {
            return ApiScoping.missingContext();
        }
        shoppingApplicationService.removeShoppingList(
                context.getHouseholdId(),
                context.getUserId(),
                listId
        );
        return ResponseEntity.noContent().build();
    }

    private ShoppingListResponse toResponse(ShoppingListView list) {
        List<ShoppingItemResponse> items = new ArrayList<>();
        for (ShoppingItemView item : list.items()) {
            items.add(new ShoppingItemResponse(
                    item.id(),
                    item.name(),
                    item.status().name(),
                    item.quantity(),
                    item.unit() != null ? item.unit().name() : null,
                    item.createdAt(),
                    item.boughtAt()
            ));
        }
        return new ShoppingListResponse(list.id(), list.name(), items);
    }

    private ShoppingUnit parseUnit(String unit) {
        if (unit == null || unit.isBlank()) {
            return null;
        }
        String normalized = unit.trim().toUpperCase().replace("Ö", "O").replace("Ä", "A").replace("Å", "A");
        if ("FÖRP".equals(normalized) || "FORP".equals(normalized)) {
            return ShoppingUnit.FORP;
        }
        return ShoppingUnit.valueOf(normalized);
    }
}
