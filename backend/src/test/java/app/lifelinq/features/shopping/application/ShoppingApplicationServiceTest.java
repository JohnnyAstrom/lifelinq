package app.lifelinq.features.shopping.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import app.lifelinq.features.group.contract.EnsureGroupMemberUseCase;
import app.lifelinq.features.shopping.contract.AddShoppingItemOutput;
import app.lifelinq.features.shopping.contract.ShoppingUnitView;
import app.lifelinq.features.shopping.domain.ShoppingItemSourceKind;
import app.lifelinq.features.shopping.domain.ShoppingList;
import app.lifelinq.features.shopping.domain.ShoppingListType;
import app.lifelinq.features.shopping.domain.ShoppingUnit;
import app.lifelinq.features.shopping.contract.ShoppingListView;
import app.lifelinq.features.shopping.infrastructure.InMemoryShoppingCategoryPreferenceRepository;
import app.lifelinq.features.shopping.infrastructure.InMemoryShoppingListRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ShoppingApplicationServiceTest {

    @Test
    void updateShoppingListIdentityUpdatesNameAndType() {
        InMemoryShoppingListRepository listRepository = new InMemoryShoppingListRepository();
        ShoppingApplicationService service = new ShoppingApplicationService(
                listRepository,
                new InMemoryShoppingCategoryPreferenceRepository(),
                allowAllMembership(),
                Clock.fixed(Instant.parse("2026-03-13T09:00:00Z"), ZoneOffset.UTC)
        );
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID listId = UUID.randomUUID();

        listRepository.save(new ShoppingList(
                listId,
                groupId,
                "Groceries",
                ShoppingListType.GROCERY,
                Instant.parse("2026-03-13T08:00:00Z")
        ));

        ShoppingListView updated = service.updateShoppingListIdentity(
                groupId,
                userId,
                listId,
                "Cabin supplies",
                ShoppingListType.SUPPLIES
        );

        assertEquals("Cabin supplies", updated.name());
        assertEquals("supplies", updated.type());
        ShoppingList saved = listRepository.findById(listId).orElseThrow();
        assertEquals("Cabin supplies", saved.getName());
        assertEquals(ShoppingListType.SUPPLIES, saved.getType());
    }

    @Test
    void addShoppingItemReturnsMergedItemWhenMealPlanIntakeIsAbsorbed() {
        InMemoryShoppingListRepository listRepository = new InMemoryShoppingListRepository();
        ShoppingApplicationService service = new ShoppingApplicationService(
                listRepository,
                new InMemoryShoppingCategoryPreferenceRepository(),
                allowAllMembership(),
                Clock.fixed(Instant.parse("2026-03-13T09:00:00Z"), ZoneOffset.UTC)
        );
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID listId = UUID.randomUUID();

        listRepository.save(new ShoppingList(
                listId,
                groupId,
                "Groceries",
                ShoppingListType.GROCERY,
                Instant.parse("2026-03-13T08:00:00Z")
        ));

        AddShoppingItemOutput first = service.addShoppingItem(
                groupId,
                userId,
                listId,
                "tomato",
                new BigDecimal("2"),
                ShoppingUnit.PCS,
                ShoppingItemSourceKind.MEAL_PLAN,
                "Pasta"
        );

        AddShoppingItemOutput second = service.addShoppingItem(
                groupId,
                userId,
                listId,
                "tomato",
                new BigDecimal("3"),
                ShoppingUnit.PCS,
                ShoppingItemSourceKind.MEAL_PLAN,
                "Soup"
        );

        assertEquals(first.itemId(), second.itemId());
        assertEquals("INCREASED_EXISTING", second.outcome());
        assertEquals(new BigDecimal("5"), second.quantity());
        assertEquals(1, listRepository.findById(listId).orElseThrow().getItems().size());
        assertNull(second.sourceKind());
        assertNull(second.sourceLabel());
    }

    @Test
    void addShoppingItemUpdatesSingleOpenItemWithoutQuantityWhenMealPlanIntakeProvidesIt() {
        InMemoryShoppingListRepository listRepository = new InMemoryShoppingListRepository();
        ShoppingApplicationService service = new ShoppingApplicationService(
                listRepository,
                new InMemoryShoppingCategoryPreferenceRepository(),
                allowAllMembership(),
                Clock.fixed(Instant.parse("2026-03-13T09:00:00Z"), ZoneOffset.UTC)
        );
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID listId = UUID.randomUUID();

        listRepository.save(new ShoppingList(
                listId,
                groupId,
                "Groceries",
                ShoppingListType.GROCERY,
                Instant.parse("2026-03-13T08:00:00Z")
        ));

        AddShoppingItemOutput first = service.addShoppingItem(
                groupId,
                userId,
                listId,
                "tomato",
                null,
                null,
                ShoppingItemSourceKind.MEAL_PLAN,
                "Salad"
        );

        AddShoppingItemOutput second = service.addShoppingItem(
                groupId,
                userId,
                listId,
                "tomato",
                new BigDecimal("2"),
                ShoppingUnit.KG,
                ShoppingItemSourceKind.MEAL_PLAN,
                "Soup"
        );

        assertEquals(first.itemId(), second.itemId());
        assertEquals("UPDATED_EXISTING", second.outcome());
        assertEquals(new BigDecimal("2"), second.quantity());
        assertEquals(ShoppingUnitView.KG, second.unit());
        assertEquals(1, listRepository.findById(listId).orElseThrow().getItems().size());
        assertNull(second.sourceKind());
        assertNull(second.sourceLabel());
    }

    @Test
    void addShoppingItemReusesSingleOpenQuantifiedItemWhenMealPlanIntakeHasNoQuantity() {
        InMemoryShoppingListRepository listRepository = new InMemoryShoppingListRepository();
        ShoppingApplicationService service = new ShoppingApplicationService(
                listRepository,
                new InMemoryShoppingCategoryPreferenceRepository(),
                allowAllMembership(),
                Clock.fixed(Instant.parse("2026-03-13T09:00:00Z"), ZoneOffset.UTC)
        );
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID listId = UUID.randomUUID();

        listRepository.save(new ShoppingList(
                listId,
                groupId,
                "Groceries",
                ShoppingListType.GROCERY,
                Instant.parse("2026-03-13T08:00:00Z")
        ));

        AddShoppingItemOutput first = service.addShoppingItem(
                groupId,
                userId,
                listId,
                "banana",
                new BigDecimal("3"),
                ShoppingUnit.PCS,
                ShoppingItemSourceKind.MEAL_PLAN,
                "Smoothie"
        );

        AddShoppingItemOutput second = service.addShoppingItem(
                groupId,
                userId,
                listId,
                "banana",
                null,
                null,
                ShoppingItemSourceKind.MEAL_PLAN,
                "Pancakes"
        );

        assertEquals(first.itemId(), second.itemId());
        assertEquals("REUSED_EXISTING", second.outcome());
        assertEquals(new BigDecimal("3"), second.quantity());
        assertEquals(ShoppingUnitView.PCS, second.unit());
        assertEquals(1, listRepository.findById(listId).orElseThrow().getItems().size());
        assertNull(second.sourceKind());
        assertNull(second.sourceLabel());
    }

    @Test
    void manualAddDefaultsToReusingSingleMatchingOpenItemUnlessAddAsNewIsRequested() {
        InMemoryShoppingListRepository listRepository = new InMemoryShoppingListRepository();
        ShoppingApplicationService service = new ShoppingApplicationService(
                listRepository,
                new InMemoryShoppingCategoryPreferenceRepository(),
                allowAllMembership(),
                Clock.fixed(Instant.parse("2026-03-13T09:00:00Z"), ZoneOffset.UTC)
        );
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID listId = UUID.randomUUID();

        listRepository.save(new ShoppingList(
                listId,
                groupId,
                "Groceries",
                ShoppingListType.GROCERY,
                Instant.parse("2026-03-13T08:00:00Z")
        ));

        AddShoppingItemOutput first = service.addShoppingItem(groupId, userId, listId, "banana");
        AddShoppingItemOutput second = service.addShoppingItem(groupId, userId, listId, "banana");
        AddShoppingItemOutput third = service.addShoppingItem(groupId, userId, listId, "banana", null, null, true);

        assertEquals(first.itemId(), second.itemId());
        assertEquals("REUSED_EXISTING", second.outcome());
        assertEquals(2, listRepository.findById(listId).orElseThrow().getItems().size());
        assertEquals("CREATED", third.outcome());
    }

    private EnsureGroupMemberUseCase allowAllMembership() {
        return (groupId, actorUserId) -> {};
    }
}
