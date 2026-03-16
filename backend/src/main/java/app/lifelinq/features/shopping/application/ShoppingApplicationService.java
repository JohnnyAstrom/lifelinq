package app.lifelinq.features.shopping.application;

import app.lifelinq.features.group.contract.EnsureGroupMemberUseCase;
import app.lifelinq.features.shopping.contract.AddShoppingItemOutput;
import app.lifelinq.features.shopping.contract.CreateShoppingListOutput;
import app.lifelinq.features.shopping.contract.ShoppingCategoryPreferenceView;
import app.lifelinq.features.shopping.contract.ShoppingItemStatusView;
import app.lifelinq.features.shopping.contract.ShoppingItemView;
import app.lifelinq.features.shopping.contract.ShoppingListView;
import app.lifelinq.features.shopping.contract.ShoppingUnitView;
import app.lifelinq.features.shopping.contract.ToggleShoppingItemOutput;
import app.lifelinq.features.shopping.domain.ShoppingCategory;
import app.lifelinq.features.shopping.domain.ShoppingCategoryPreference;
import app.lifelinq.features.shopping.domain.ShoppingCategoryPreferenceRepository;
import app.lifelinq.features.shopping.domain.ShoppingItem;
import app.lifelinq.features.shopping.domain.ShoppingAddItemResult;
import app.lifelinq.features.shopping.domain.ShoppingItemStatus;
import app.lifelinq.features.shopping.domain.ShoppingItemSourceKind;
import app.lifelinq.features.shopping.domain.ShoppingList;
import app.lifelinq.features.shopping.domain.ShoppingListNotFoundException;
import app.lifelinq.features.shopping.domain.ShoppingListRepository;
import app.lifelinq.features.shopping.domain.ShoppingListType;
import app.lifelinq.features.shopping.domain.ShoppingUnit;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class ShoppingApplicationService {
    private final ShoppingListRepository shoppingListRepository;
    private final ShoppingCategoryPreferenceRepository shoppingCategoryPreferenceRepository;
    private final EnsureGroupMemberUseCase ensureGroupMemberUseCase;
    private final Clock clock;

    public ShoppingApplicationService(
            ShoppingListRepository shoppingListRepository,
            ShoppingCategoryPreferenceRepository shoppingCategoryPreferenceRepository,
            EnsureGroupMemberUseCase ensureGroupMemberUseCase,
            Clock clock
    ) {
        if (shoppingListRepository == null) {
            throw new IllegalArgumentException("shoppingListRepository must not be null");
        }
        if (shoppingCategoryPreferenceRepository == null) {
            throw new IllegalArgumentException("shoppingCategoryPreferenceRepository must not be null");
        }
        if (ensureGroupMemberUseCase == null) {
            throw new IllegalArgumentException("ensureGroupMemberUseCase must not be null");
        }
        if (clock == null) {
            throw new IllegalArgumentException("clock must not be null");
        }
        this.shoppingListRepository = shoppingListRepository;
        this.shoppingCategoryPreferenceRepository = shoppingCategoryPreferenceRepository;
        this.ensureGroupMemberUseCase = ensureGroupMemberUseCase;
        this.clock = clock;
    }

    @Transactional
    public CreateShoppingListOutput createShoppingList(
            UUID groupId,
            UUID actorUserId,
            String name
    ) {
        return createShoppingList(groupId, actorUserId, name, ShoppingListType.MIXED);
    }

    @Transactional
    public CreateShoppingListOutput createShoppingList(
            UUID groupId,
            UUID actorUserId,
            String name,
            ShoppingListType type
    ) {
        ensureGroupMemberUseCase.execute(groupId, actorUserId);
        String normalizedName = normalizeListName(name);
        UUID listId = UUID.randomUUID();
        List<ShoppingList> existingLists = loadOrderedLists(groupId);
        normalizeOrderAndPersist(existingLists);
        int nextOrderIndex = existingLists.size();
        ShoppingList list = new ShoppingList(listId, groupId, normalizedName, type, nextOrderIndex, clock.instant());
        shoppingListRepository.save(list);
        return new CreateShoppingListOutput(listId, normalizedName, type.key());
    }

    @Transactional
    public AddShoppingItemOutput addShoppingItem(
            UUID groupId,
            UUID actorUserId,
            UUID listId,
            String itemName
    ) {
        return addShoppingItem(groupId, actorUserId, listId, itemName, null, null);
    }

    @Transactional
    public AddShoppingItemOutput addShoppingItem(
            UUID groupId,
            UUID actorUserId,
            UUID listId,
            String itemName,
            BigDecimal quantity,
            ShoppingUnit unit
    ) {
        return addShoppingItem(groupId, actorUserId, listId, itemName, quantity, unit, null, null, false);
    }

    @Transactional
    public AddShoppingItemOutput addShoppingItem(
            UUID groupId,
            UUID actorUserId,
            UUID listId,
            String itemName,
            BigDecimal quantity,
            ShoppingUnit unit,
            boolean addAsNew
    ) {
        return addShoppingItem(groupId, actorUserId, listId, itemName, quantity, unit, null, null, addAsNew);
    }

    @Transactional
    public AddShoppingItemOutput addShoppingItem(
            UUID groupId,
            UUID actorUserId,
            UUID listId,
            String itemName,
            BigDecimal quantity,
            ShoppingUnit unit,
            ShoppingItemSourceKind sourceKind,
            String sourceLabel
    ) {
        return addShoppingItem(groupId, actorUserId, listId, itemName, quantity, unit, sourceKind, sourceLabel, false);
    }

    @Transactional
    public AddShoppingItemOutput addShoppingItem(
            UUID groupId,
            UUID actorUserId,
            UUID listId,
            String itemName,
            BigDecimal quantity,
            ShoppingUnit unit,
            ShoppingItemSourceKind sourceKind,
            String sourceLabel,
            boolean addAsNew
    ) {
        ensureGroupMemberUseCase.execute(groupId, actorUserId);
        ShoppingList list = getListForGroup(groupId, listId);
        String normalizedName = normalizeItemName(itemName);
        UUID itemId = UUID.randomUUID();
        Instant now = clock.instant();
        ShoppingAddItemResult addResult = list.addItem(itemId, normalizedName, quantity, unit, sourceKind, sourceLabel, addAsNew, now);
        shoppingListRepository.save(list);
        ShoppingItem item = list.getItemOrThrow(addResult.itemId());
        return new AddShoppingItemOutput(
                list.getId(),
                item.getId(),
                item.getName(),
                addResult.outcome().name(),
                toViewStatus(item.getStatus()),
                item.getQuantity(),
                toViewUnit(item.getUnit()),
                toViewSourceKind(item.getSourceKind()),
                item.getSourceLabel(),
                item.getCreatedAt(),
                item.getBoughtAt()
        );
    }

    @Transactional
    public ToggleShoppingItemOutput toggleShoppingItem(
            UUID groupId,
            UUID actorUserId,
            UUID listId,
            UUID itemId
    ) {
        ensureGroupMemberUseCase.execute(groupId, actorUserId);
        ShoppingList list = getListForGroup(groupId, listId);
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
            UUID groupId,
            UUID actorUserId,
            UUID listId,
            UUID itemId
    ) {
        ensureGroupMemberUseCase.execute(groupId, actorUserId);
        ShoppingList list = getListForGroup(groupId, listId);
        list.removeItem(itemId);
        shoppingListRepository.save(list);
    }

    @Transactional
    public void reorderShoppingItem(
            UUID groupId,
            UUID actorUserId,
            UUID listId,
            UUID itemId,
            String direction
    ) {
        ensureGroupMemberUseCase.execute(groupId, actorUserId);
        ShoppingList list = getListForGroup(groupId, listId);
        list.reorderOpenItem(itemId, direction);
        shoppingListRepository.save(list);
    }

    @Transactional
    public void removeShoppingList(
            UUID groupId,
            UUID actorUserId,
            UUID listId
    ) {
        ensureGroupMemberUseCase.execute(groupId, actorUserId);
        getListForGroup(groupId, listId);
        shoppingListRepository.deleteById(listId);
        normalizeOrderAndPersist(loadOrderedLists(groupId));
    }

    @Transactional
    public ShoppingItemView updateShoppingItem(
            UUID groupId,
            UUID actorUserId,
            UUID listId,
            UUID itemId,
            String name,
            BigDecimal quantity,
            ShoppingUnit unit
    ) {
        ensureGroupMemberUseCase.execute(groupId, actorUserId);
        ShoppingList list = getListForGroup(groupId, listId);
        String normalizedName = normalizeItemName(name);
        list.updateItem(itemId, normalizedName, quantity, unit);
        shoppingListRepository.save(list);
        return toView(list.getItemOrThrow(itemId));
    }

    @Transactional(readOnly = true)
    public List<ShoppingCategoryPreferenceView> listShoppingCategoryPreferences(UUID groupId, UUID actorUserId) {
        ensureGroupMemberUseCase.execute(groupId, actorUserId);
        List<ShoppingCategoryPreferenceView> result = new ArrayList<>();
        for (ShoppingCategoryPreference preference : shoppingCategoryPreferenceRepository.findByGroupId(groupId)) {
            result.add(toView(preference));
        }
        return result;
    }

    @Transactional
    public ShoppingCategoryPreferenceView saveShoppingCategoryPreference(
            UUID groupId,
            UUID actorUserId,
            ShoppingListType listType,
            String normalizedTitle,
            ShoppingCategory preferredCategory
    ) {
        ensureGroupMemberUseCase.execute(groupId, actorUserId);
        ShoppingCategoryPreference saved = shoppingCategoryPreferenceRepository.save(
                new ShoppingCategoryPreference(
                        groupId,
                        listType,
                        normalizeCategoryPreferenceTitle(normalizedTitle),
                        preferredCategory,
                        clock.instant()
                )
        );
        return toView(saved);
    }

    @Transactional
    public void clearShoppingCategoryPreference(
            UUID groupId,
            UUID actorUserId,
            ShoppingListType listType,
            String normalizedTitle
    ) {
        ensureGroupMemberUseCase.execute(groupId, actorUserId);
        shoppingCategoryPreferenceRepository.deleteByGroupIdAndListTypeAndNormalizedTitle(
                groupId,
                listType,
                normalizeCategoryPreferenceTitle(normalizedTitle)
        );
    }

    @Transactional(readOnly = true)
    public List<ShoppingListView> listShoppingLists(UUID groupId, UUID actorUserId) {
        ensureGroupMemberUseCase.execute(groupId, actorUserId);
        List<ShoppingList> lists = loadOrderedLists(groupId);
        List<ShoppingListView> result = new ArrayList<>();
        for (ShoppingList list : lists) {
            result.add(toView(list));
        }
        return result;
    }

    @Transactional
    public void reorderShoppingList(
            UUID groupId,
            UUID actorUserId,
            UUID listId,
            String direction
    ) {
        ensureGroupMemberUseCase.execute(groupId, actorUserId);
        List<ShoppingList> lists = loadOrderedLists(groupId);
        normalizeOrderAndPersist(lists);
        int currentIndex = findIndexById(lists, listId);
        int targetIndex;
        String normalizedDirection = normalizeDirection(direction);
        if ("UP".equals(normalizedDirection)) {
            targetIndex = currentIndex - 1;
        } else {
            targetIndex = currentIndex + 1;
        }
        if (targetIndex < 0 || targetIndex >= lists.size()) {
            return;
        }
        ShoppingList current = lists.get(currentIndex);
        ShoppingList target = lists.get(targetIndex);
        int currentOrder = current.getOrderIndex();
        current.setOrderIndex(target.getOrderIndex());
        target.setOrderIndex(currentOrder);
        shoppingListRepository.save(current);
        shoppingListRepository.save(target);
    }

    @Transactional
    public ShoppingListView updateShoppingListIdentity(
            UUID groupId,
            UUID actorUserId,
            UUID listId,
            String name,
            ShoppingListType type
    ) {
        ensureGroupMemberUseCase.execute(groupId, actorUserId);
        ShoppingList list = getListForGroup(groupId, listId);
        String normalizedName = normalizeListName(name);
        list.updateIdentity(normalizedName, type);
        shoppingListRepository.save(list);
        return toView(list);
    }

    private ShoppingList getListForGroup(UUID groupId, UUID listId) {
        if (listId == null) {
            throw new IllegalArgumentException("listId must not be null");
        }
        ShoppingList list = shoppingListRepository.findById(listId)
                .orElseThrow(() -> new ShoppingListNotFoundException(listId));
        if (!list.getGroupId().equals(groupId)) {
            throw new AccessDeniedException("List does not belong to group");
        }
        return list;
    }

    private List<ShoppingList> loadOrderedLists(UUID groupId) {
        List<ShoppingList> lists = new ArrayList<>(shoppingListRepository.findByGroupId(groupId));
        lists.sort(Comparator
                .comparingInt(ShoppingList::getOrderIndex)
                .thenComparing(ShoppingList::getCreatedAt)
                .thenComparing(ShoppingList::getId));
        return lists;
    }

    private void normalizeOrderAndPersist(List<ShoppingList> lists) {
        for (int index = 0; index < lists.size(); index++) {
            ShoppingList list = lists.get(index);
            if (list.getOrderIndex() != index) {
                list.setOrderIndex(index);
                shoppingListRepository.save(list);
            }
        }
    }

    private int findIndexById(List<ShoppingList> lists, UUID listId) {
        if (listId == null) {
            throw new IllegalArgumentException("listId must not be null");
        }
        for (int i = 0; i < lists.size(); i++) {
            if (lists.get(i).getId().equals(listId)) {
                return i;
            }
        }
        throw new ShoppingListNotFoundException(listId);
    }

    private String normalizeDirection(String direction) {
        if (direction == null || direction.isBlank()) {
            throw new IllegalArgumentException("direction must not be blank");
        }
        String normalized = direction.trim().toUpperCase();
        if (!"UP".equals(normalized) && !"DOWN".equals(normalized)) {
            throw new IllegalArgumentException("direction must be UP or DOWN");
        }
        return normalized;
    }

    private ShoppingListView toView(ShoppingList list) {
        List<ShoppingItemView> items = new ArrayList<>();
        for (ShoppingItem item : list.getItems()) {
            items.add(toView(item));
        }
        return new ShoppingListView(list.getId(), list.getName(), list.getType().key(), items);
    }

    private ShoppingItemView toView(ShoppingItem item) {
        return new ShoppingItemView(
                item.getId(),
                item.getName(),
                toViewStatus(item.getStatus()),
                item.getQuantity(),
                toViewUnit(item.getUnit()),
                toViewSourceKind(item.getSourceKind()),
                item.getSourceLabel(),
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

    private String toViewSourceKind(ShoppingItemSourceKind sourceKind) {
        if (sourceKind == null) {
            return null;
        }
        return sourceKind.key();
    }

    private ShoppingCategoryPreferenceView toView(ShoppingCategoryPreference preference) {
        return new ShoppingCategoryPreferenceView(
                preference.listType().key(),
                preference.normalizedTitle(),
                preference.preferredCategory().key()
        );
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

    private String normalizeCategoryPreferenceTitle(String title) {
        if (title == null) {
            throw new IllegalArgumentException("normalizedTitle must not be null");
        }
        String normalized = Normalizer.normalize(title.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFKD)
                .replaceAll("\\p{M}+", "")
                .replaceAll("[^a-z0-9]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("normalizedTitle must not be blank");
        }
        return normalized;
    }
}
