package app.lifelinq.features.shopping.application;

import app.lifelinq.features.household.contract.EnsureHouseholdMemberUseCase;
import app.lifelinq.features.shopping.contract.AddShoppingItemOutput;
import app.lifelinq.features.shopping.contract.CreateShoppingListOutput;
import app.lifelinq.features.shopping.contract.ShoppingItemStatusView;
import app.lifelinq.features.shopping.contract.ShoppingItemView;
import app.lifelinq.features.shopping.contract.ShoppingListView;
import app.lifelinq.features.shopping.contract.ShoppingUnitView;
import app.lifelinq.features.shopping.contract.ToggleShoppingItemOutput;
import app.lifelinq.features.shopping.domain.ShoppingItem;
import app.lifelinq.features.shopping.domain.ShoppingItemStatus;
import app.lifelinq.features.shopping.domain.ShoppingList;
import app.lifelinq.features.shopping.domain.ShoppingListNotFoundException;
import app.lifelinq.features.shopping.domain.ShoppingListRepository;
import app.lifelinq.features.shopping.domain.ShoppingUnit;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class ShoppingApplicationService {
    private final ShoppingListRepository shoppingListRepository;
    private final EnsureHouseholdMemberUseCase ensureHouseholdMemberUseCase;
    private final Clock clock;

    public ShoppingApplicationService(
            ShoppingListRepository shoppingListRepository,
            EnsureHouseholdMemberUseCase ensureHouseholdMemberUseCase,
            Clock clock
    ) {
        if (shoppingListRepository == null) {
            throw new IllegalArgumentException("shoppingListRepository must not be null");
        }
        if (ensureHouseholdMemberUseCase == null) {
            throw new IllegalArgumentException("ensureHouseholdMemberUseCase must not be null");
        }
        if (clock == null) {
            throw new IllegalArgumentException("clock must not be null");
        }
        this.shoppingListRepository = shoppingListRepository;
        this.ensureHouseholdMemberUseCase = ensureHouseholdMemberUseCase;
        this.clock = clock;
    }

    @Transactional
    public CreateShoppingListOutput createShoppingList(
            UUID householdId,
            UUID actorUserId,
            String name
    ) {
        ensureHouseholdMemberUseCase.execute(householdId, actorUserId);
        String normalizedName = normalizeListName(name);
        UUID listId = UUID.randomUUID();
        ShoppingList list = new ShoppingList(listId, householdId, normalizedName, clock.instant());
        shoppingListRepository.save(list);
        return new CreateShoppingListOutput(listId, normalizedName);
    }

    @Transactional
    public AddShoppingItemOutput addShoppingItem(
            UUID householdId,
            UUID actorUserId,
            UUID listId,
            String itemName
    ) {
        return addShoppingItem(householdId, actorUserId, listId, itemName, null, null);
    }

    @Transactional
    public AddShoppingItemOutput addShoppingItem(
            UUID householdId,
            UUID actorUserId,
            UUID listId,
            String itemName,
            BigDecimal quantity,
            ShoppingUnit unit
    ) {
        ensureHouseholdMemberUseCase.execute(householdId, actorUserId);
        ShoppingList list = getListForHousehold(householdId, listId);
        String normalizedName = normalizeItemName(itemName);
        UUID itemId = UUID.randomUUID();
        Instant now = clock.instant();
        list.addItem(itemId, normalizedName, quantity, unit, now);
        shoppingListRepository.save(list);
        ShoppingItem item = list.getItemOrThrow(itemId);
        return new AddShoppingItemOutput(
                list.getId(),
                item.getId(),
                item.getName(),
                toViewStatus(item.getStatus()),
                item.getQuantity(),
                toViewUnit(item.getUnit()),
                item.getCreatedAt(),
                item.getBoughtAt()
        );
    }

    @Transactional
    public ToggleShoppingItemOutput toggleShoppingItem(
            UUID householdId,
            UUID actorUserId,
            UUID listId,
            UUID itemId
    ) {
        ensureHouseholdMemberUseCase.execute(householdId, actorUserId);
        ShoppingList list = getListForHousehold(householdId, listId);
        list.toggleItem(itemId, clock.instant());
        shoppingListRepository.save(list);
        ShoppingItem item = list.getItemOrThrow(itemId);
        return new ToggleShoppingItemOutput(
                list.getId(),
                item.getId(),
                toViewStatus(item.getStatus()),
                item.getBoughtAt()
        );
    }

    @Transactional
    public void removeShoppingItem(
            UUID householdId,
            UUID actorUserId,
            UUID listId,
            UUID itemId
    ) {
        ensureHouseholdMemberUseCase.execute(householdId, actorUserId);
        ShoppingList list = getListForHousehold(householdId, listId);
        list.removeItem(itemId);
        shoppingListRepository.save(list);
    }

    @Transactional
    public void removeShoppingList(
            UUID householdId,
            UUID actorUserId,
            UUID listId
    ) {
        ensureHouseholdMemberUseCase.execute(householdId, actorUserId);
        getListForHousehold(householdId, listId);
        shoppingListRepository.deleteById(listId);
    }

    @Transactional
    public ShoppingItemView updateShoppingItem(
            UUID householdId,
            UUID actorUserId,
            UUID listId,
            UUID itemId,
            String name,
            BigDecimal quantity,
            ShoppingUnit unit
    ) {
        ensureHouseholdMemberUseCase.execute(householdId, actorUserId);
        ShoppingList list = getListForHousehold(householdId, listId);
        String normalizedName = normalizeItemName(name);
        list.updateItem(itemId, normalizedName, quantity, unit);
        shoppingListRepository.save(list);
        return toView(list.getItemOrThrow(itemId));
    }

    @Transactional(readOnly = true)
    public List<ShoppingListView> listShoppingLists(UUID householdId, UUID actorUserId) {
        ensureHouseholdMemberUseCase.execute(householdId, actorUserId);
        List<ShoppingList> lists = shoppingListRepository.findByHouseholdId(householdId);
        List<ShoppingListView> result = new ArrayList<>();
        for (ShoppingList list : lists) {
            result.add(toView(list));
        }
        return result;
    }

    @Transactional
    public ShoppingListView updateShoppingListName(
            UUID householdId,
            UUID actorUserId,
            UUID listId,
            String name
    ) {
        ensureHouseholdMemberUseCase.execute(householdId, actorUserId);
        ShoppingList list = getListForHousehold(householdId, listId);
        String normalizedName = normalizeListName(name);
        list.rename(normalizedName);
        shoppingListRepository.save(list);
        return toView(list);
    }

    private ShoppingList getListForHousehold(UUID householdId, UUID listId) {
        if (listId == null) {
            throw new IllegalArgumentException("listId must not be null");
        }
        ShoppingList list = shoppingListRepository.findById(listId)
                .orElseThrow(() -> new ShoppingListNotFoundException(listId));
        if (!list.getHouseholdId().equals(householdId)) {
            throw new AccessDeniedException("List does not belong to household");
        }
        return list;
    }

    private ShoppingListView toView(ShoppingList list) {
        List<ShoppingItemView> items = new ArrayList<>();
        for (ShoppingItem item : list.getItems()) {
            items.add(toView(item));
        }
        return new ShoppingListView(list.getId(), list.getName(), items);
    }

    private ShoppingItemView toView(ShoppingItem item) {
        return new ShoppingItemView(
                item.getId(),
                item.getName(),
                toViewStatus(item.getStatus()),
                item.getQuantity(),
                toViewUnit(item.getUnit()),
                item.getCreatedAt(),
                item.getBoughtAt()
        );
    }

    private ShoppingItemStatusView toViewStatus(ShoppingItemStatus status) {
        if (status == ShoppingItemStatus.BOUGHT) {
            return ShoppingItemStatusView.BOUGHT;
        }
        return ShoppingItemStatusView.TO_BUY;
    }

    private ShoppingUnitView toViewUnit(ShoppingUnit unit) {
        if (unit == null) {
            return null;
        }
        return ShoppingUnitView.valueOf(unit.name());
    }

    private String normalizeItemName(String name) {
        if (name == null) {
            return null;
        }
        return name.trim().toLowerCase();
    }

    private String normalizeListName(String name) {
        if (name == null) {
            return null;
        }
        return name.trim();
    }
}
