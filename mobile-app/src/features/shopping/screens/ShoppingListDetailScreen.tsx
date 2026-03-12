import { useEffect, useMemo, useRef, useState } from 'react';
import { Ionicons } from '@expo/vector-icons';
import {
  Alert,
  GestureResponderEvent,
  Keyboard,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import { Swipeable } from 'react-native-gesture-handler';
import { AddDetailsSheetContent } from '../components/AddDetailsSheetContent';
import { EditItemSheetContent } from '../components/EditItemSheetContent';
import { ShoppingItemRow } from '../components/ShoppingItemRow';
import { ShoppingItemSectionCard } from '../components/ShoppingItemSectionCard';
import { useShoppingCategoryPreferences } from '../hooks/useShoppingCategoryPreferences';
import { useShoppingListDetailProjection } from '../hooks/useShoppingListDetailProjection';
import { useShoppingListDetailWorkflow } from '../hooks/useShoppingListDetailWorkflow';
import { useShoppingLists } from '../hooks/useShoppingLists';
import { useAppBackHandler } from '../../../shared/hooks/useAppBackHandler';
import { type ShoppingUnit } from '../api/shoppingApi';
import { getShoppingCategoryDefinitions, type ShoppingCategoryKey } from '../utils/shoppingCategories';
import { AppScreen, BackIconButton, Subtle, TopBar } from '../../../shared/ui/components';
import { OverlaySheet } from '../../../shared/ui/OverlaySheet';
import { textStyles, theme } from '../../../shared/ui/theme';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

type Props = {
  token: string;
  listId: string;
  onBack: () => void;
};

export function ShoppingListDetailScreen({ token, listId, onBack }: Props) {
  const insets = useSafeAreaInsets();
  const shopping = useShoppingLists(token);
  const categoryPreferences = useShoppingCategoryPreferences();
  const workflow = useShoppingListDetailWorkflow({ shopping, listId });
  const { state: workflowState, actions: workflowActions } = workflow;
  const [showMoreEditUnits, setShowMoreEditUnits] = useState(false);
  const [showMoreAddUnits, setShowMoreAddUnits] = useState(false);
  const [showAddFormDetails, setShowAddFormDetails] = useState(false);
  const [isBoughtExpanded, setIsBoughtExpanded] = useState(false);
  const orderedOpenItemIdsRef = useRef<string[]>([]);
  const draggingOpenItemIdRef = useRef<string | null>(null);
  const dragStartIndexRef = useRef<number | null>(null);
  const dragMovedRef = useRef(false);
  const dragStartPageYRef = useRef<number | null>(null);
  const rowHeightRef = useRef(92);
  const finishingDragRef = useRef(false);
  const ignoreNextOpenPressRef = useRef(false);
  const pendingOpenReorderSyncRef = useRef(false);
  const addDetailsInputRef = useRef<TextInput | null>(null);
  const quantityRef = useRef<TextInput | null>(null);
  const projection = useShoppingListDetailProjection({
    lists: shopping.lists,
    listId,
    orderedOpenItemIds: workflowState.orderedOpenItemIds,
    categoryContext: categoryPreferences,
    categoryPreferencesVersion: categoryPreferences.version,
  });
  const selectedList = projection.list;
  const openItems = projection.openItems;
  const openSections = projection.openSections;
  const boughtSection = projection.boughtSection;
  const boughtItems = boughtSection?.items ?? [];
  const isBoughtCollapsed = boughtItems.length > 0 && !isBoughtExpanded;
  const canReorderOpenItems = openSections.length <= 1;
  const showOpenReorderHint = canReorderOpenItems && openItems.length > 0;

  const strings = {
    titleFallback: 'Shopping list',
    back: 'Back',
    openLabel: 'Open',
    boughtLabel: 'Bought',
    openCountSuffix: 'open',
    boughtCountSuffix: 'bought',
    showBought: 'Show bought',
    hideBought: 'Hide bought',
    details: 'Open item details',
    swipeBought: 'Bought',
    swipeOpen: 'Open',
    noOpenItems: 'No open items.',
    noBoughtItems: 'No bought items yet.',
    addPlaceholder: 'Add item…',
    addPlaceholderExtended: 'Add item…',
    addAction: 'Add item',
    addDetailsToggle: '+ Add amount',
    addDetailsHide: '- Hide amount',
    loadingItems: 'Loading items...',
    clearBought: 'Clear bought',
    clearBoughtTitle: 'Clear bought items?',
    clearBoughtBody: 'This will remove all bought items from the list.',
    addQuantityPlaceholder: 'Amount',
    addErrorQuantity: 'Quantity must be a positive number.',
    addErrorQuantityUnit: 'Quantity and unit must be set together.',
    addDetailsTitle: 'Add details',
    addDetailsAddedPrefix: 'Added',
    addItemTitle: 'Add item',
    editTitle: 'Edit item',
    editNamePlaceholder: 'Item name',
    editQuantityPlaceholder: 'Quantity (optional)',
    editCategoryLabel: 'Category',
    autoCategoryLabel: 'Auto',
    saveChanges: 'Save changes',
    removeItem: 'Remove item',
    close: 'Close',
    nameRequired: 'Name is required.',
    quantityInvalid: 'Quantity must be a positive number.',
    quantityUnitMismatch: 'Quantity and unit must be set together.',
    unitNone: 'None',
    unitMore: 'Show more',
    unitLess: 'Show less',
    reorderHint: 'Hold and drag to reorder open items',
  };

  useEffect(() => {
    if (workflowState.draggingOpenItemId || pendingOpenReorderSyncRef.current) {
      return;
    }
    const next = openItems.map((item) => item.id);
    const current = orderedOpenItemIdsRef.current;
    const same = current.length === next.length && current.every((value, index) => value === next[index]);
    if (same) {
      return;
    }
    orderedOpenItemIdsRef.current = next;
    workflowActions.setOrderedOpenItemIds(next);
  }, [openItems, workflowActions, workflowState.draggingOpenItemId]);

  useEffect(() => {
    if (!workflowState.showAddDetails || !showAddFormDetails) {
      return;
    }
    const focusTimeout = setTimeout(() => {
      quantityRef.current?.focus();
    }, 40);
    return () => clearTimeout(focusTimeout);
  }, [showAddFormDetails, workflowState.showAddDetails]);

  function closeAddDetails() {
    workflowActions.closeAddDetails();
    setShowMoreAddUnits(false);
    setShowAddFormDetails(false);
    Keyboard.dismiss();
  }

  function openEdit(item: {
    id: string;
    title: string;
    quantity: number | null;
    unit: ShoppingUnit | null;
    categoryOverrideKey: ShoppingCategoryKey | null;
  }) {
    workflowActions.openEdit(item);
    setShowMoreEditUnits(false);
  }

  function closeEdit() {
    workflowActions.closeEdit();
    setShowMoreEditUnits(false);
    Keyboard.dismiss();
  }

  async function handleSaveEdit() {
    await workflowActions.handleSaveEdit(
      {
        nameRequired: strings.nameRequired,
        quantityInvalid: strings.quantityInvalid,
        quantityUnitMismatch: strings.quantityUnitMismatch,
      },
      { onClose: closeEdit }
    );
  }

  async function handleRemoveEdit() {
    await workflowActions.handleRemoveEdit({ onClose: closeEdit });
  }

  async function handleAddItem() {
    await workflowActions.handleAddItem(
      {
        addErrorQuantity: strings.addErrorQuantity,
        addErrorQuantityUnit: strings.addErrorQuantityUnit,
        addDetailsAddedPrefix: strings.addDetailsAddedPrefix,
      },
      { onRefocus: () => addDetailsInputRef.current?.focus() }
    );
  }

  async function handleClearBought() {
    if (!selectedList || boughtItems.length === 0) {
      return;
    }
    for (const item of boughtItems) {
      await shopping.removeItem(selectedList.id, item.id);
    }
  }

  function requestClearBought() {
    if (!selectedList || boughtItems.length === 0) {
      return;
    }
    Alert.alert(strings.clearBoughtTitle, strings.clearBoughtBody, [
      { text: strings.close, style: 'cancel' },
      {
        text: strings.clearBought,
        style: 'destructive',
        onPress: () => {
          void handleClearBought();
        },
      },
    ]);
  }

  function moveIds(ids: string[], from: number, to: number) {
    if (from === to) {
      return ids;
    }
    const next = [...ids];
    const [moved] = next.splice(from, 1);
    next.splice(to, 0, moved);
    return next;
  }

  function startOpenDrag(itemId: string, pageY: number) {
    if (!canReorderOpenItems) {
      return;
    }
    const currentIds = orderedOpenItemIdsRef.current.length > 0
      ? orderedOpenItemIdsRef.current
      : openItems.map((item) => item.id);
    const index = currentIds.indexOf(itemId);
    if (index < 0) {
      return;
    }
    orderedOpenItemIdsRef.current = currentIds;
    workflowActions.setOrderedOpenItemIds(currentIds);
    workflowActions.setDraggingOpenItemId(itemId);
    draggingOpenItemIdRef.current = itemId;
    dragStartIndexRef.current = index;
    dragMovedRef.current = false;
    dragStartPageYRef.current = pageY;
    ignoreNextOpenPressRef.current = true;
  }

  function handleOpenTouchMove(event: GestureResponderEvent) {
    if (!draggingOpenItemIdRef.current) {
      return;
    }
    const pageY = event.nativeEvent.pageY;
    const startY = dragStartPageYRef.current;
    if (startY === null) {
      return;
    }
    if (Math.abs(pageY - startY) < 12) {
      return;
    }
    const currentIds = orderedOpenItemIdsRef.current;
    if (currentIds.length === 0) {
      return;
    }
    const startIndex = dragStartIndexRef.current;
    if (startIndex === null) {
      return;
    }
    const deltaY = pageY - startY;
    const offsetRows = Math.round(deltaY / rowHeightRef.current);
    const targetIndex = Math.max(0, Math.min(currentIds.length - 1, startIndex + offsetRows));
    const currentIndex = currentIds.indexOf(draggingOpenItemIdRef.current);
    if (currentIndex < 0 || targetIndex === currentIndex) {
      return;
    }
    dragMovedRef.current = true;
    const next = moveIds(currentIds, currentIndex, targetIndex);
    orderedOpenItemIdsRef.current = next;
    workflowActions.setOrderedOpenItemIds(next);
  }

  async function finishOpenDrag() {
    if (finishingDragRef.current) {
      return;
    }
    finishingDragRef.current = true;
    const draggedId = draggingOpenItemIdRef.current;
    const startIndex = dragStartIndexRef.current;
    if (!draggedId || startIndex === null) {
      finishingDragRef.current = false;
      return;
    }
    const moved = dragMovedRef.current;
    const finalIndex = orderedOpenItemIdsRef.current.indexOf(draggedId);
    workflowActions.setDraggingOpenItemId(null);
    draggingOpenItemIdRef.current = null;
    dragStartIndexRef.current = null;
    dragMovedRef.current = false;
    dragStartPageYRef.current = null;
    setTimeout(() => {
      ignoreNextOpenPressRef.current = false;
    }, 0);
    if (!moved || finalIndex < 0 || finalIndex === startIndex) {
      finishingDragRef.current = false;
      return;
    }
    try {
      pendingOpenReorderSyncRef.current = true;
      await workflowActions.finishOpenDrag({
        draggedId,
        startIndex,
        finalIds: orderedOpenItemIdsRef.current,
      });
    } finally {
      pendingOpenReorderSyncRef.current = false;
      finishingDragRef.current = false;
    }
  }

  useAppBackHandler({
    canGoBack: true,
    onGoBack: onBack,
    isOverlayOpen: !!workflowState.editItemId || workflowState.showAddDetails,
    onCloseOverlay: () => {
      if (workflowState.editItemId) {
        closeEdit();
        return;
      }
      if (workflowState.showAddDetails) {
        closeAddDetails();
      }
    },
  });

  return (
    <AppScreen
      scroll={false}
      contentStyle={styles.screenContent}
      header={(
        <TopBar
          title={selectedList ? selectedList.name : strings.titleFallback}
          icon={<Ionicons name="cart-outline" />}
          accentKey="shopping"
          right={<BackIconButton onPress={onBack} />}
        />
      )}
    >

      <View style={styles.contentOffset}>
        <View style={styles.mainLayout}>
          <ScrollView
            style={styles.listScroll}
            contentContainerStyle={styles.scrollContent}
            scrollEnabled={!workflowState.draggingOpenItemId}
            keyboardShouldPersistTaps="handled"
          >
          {shopping.loading ? <Subtle>{strings.loadingItems}</Subtle> : null}
          {shopping.error ? <Text style={styles.error}>{shopping.error}</Text> : null}

          {openSections.length === 0 ? (
            <ShoppingItemSectionCard
              title={strings.openLabel}
              countLabel={`0 ${strings.openCountSuffix}`}
              emptyState={strings.noOpenItems}
            />
          ) : openSections.map((section) => (
            <ShoppingItemSectionCard
              key={section.id}
              title={section.title}
              countLabel={`${section.itemCount} ${strings.openCountSuffix}`}
              hint={showOpenReorderHint ? strings.reorderHint : null}
            >
              <View style={styles.sectionItems}>
                {section.items.map((item) => (
                  <Swipeable
                    key={item.id}
                    enabled={!workflowState.draggingOpenItemId && canReorderOpenItems}
                    renderRightActions={() => (
                      <View style={[styles.swipeAction, styles.swipeActionBought]}>
                        <Text style={styles.swipeActionText}>{strings.swipeBought}</Text>
                      </View>
                    )}
                    onSwipeableOpen={() => selectedList && shopping.toggleItem(selectedList.id, item.id)}
                  >
                    <ShoppingItemRow
                      title={item.title}
                      meta={item.displayMeta}
                      checked={false}
                      dragging={workflowState.draggingOpenItemId === item.id}
                      secondaryActionLabel={strings.details}
                      onToggle={() => {
                        if (ignoreNextOpenPressRef.current) {
                          ignoreNextOpenPressRef.current = false;
                          return;
                        }
                        if (selectedList && !draggingOpenItemIdRef.current) {
                          shopping.toggleItem(selectedList.id, item.id);
                        }
                      }}
                      onOpenDetails={() => {
                        if (!draggingOpenItemIdRef.current) {
                          openEdit({
                            id: item.id,
                            title: item.title,
                            quantity: item.quantity,
                            unit: item.unit,
                            categoryOverrideKey: item.categoryOverride?.key ?? null,
                          });
                        }
                      }}
                      onMeasuredHeight={(height) => {
                        rowHeightRef.current = height + theme.spacing.xs;
                      }}
                      onToggleLongPress={canReorderOpenItems
                        ? (event) => startOpenDrag(item.id, event.nativeEvent.pageY)
                        : undefined}
                      onToggleTouchMove={canReorderOpenItems ? handleOpenTouchMove : undefined}
                      onToggleTouchEnd={canReorderOpenItems
                        ? () => {
                          void finishOpenDrag();
                        }
                        : undefined}
                      onToggleTouchCancel={canReorderOpenItems
                        ? () => {
                          void finishOpenDrag();
                        }
                        : undefined}
                    />
                  </Swipeable>
                ))}
              </View>
            </ShoppingItemSectionCard>
          ))}

          <ShoppingItemSectionCard
            title={strings.boughtLabel}
            countLabel={`${boughtSection?.itemCount ?? 0} ${strings.boughtCountSuffix}`}
            variant="bought"
            collapsedSummary={isBoughtCollapsed ? `${boughtItems.length} bought item${boughtItems.length === 1 ? '' : 's'} hidden` : null}
            emptyState={boughtItems.length === 0 ? strings.noBoughtItems : null}
            actionLabel={(boughtSection?.itemCount ?? 0) > 0
              ? (isBoughtCollapsed ? strings.showBought : strings.hideBought)
              : undefined}
            onActionPress={(boughtSection?.itemCount ?? 0) > 0
              ? () => setIsBoughtExpanded((prev) => !prev)
              : undefined}
          >
            {boughtItems.length > 0 && !isBoughtCollapsed ? (
              <View style={styles.boughtContent}>
                <View style={styles.boughtActions}>
                  <Pressable onPress={requestClearBought} style={({ pressed }) => [styles.boughtActionButton, pressed ? styles.boughtActionButtonPressed : null]}>
                    <Text style={styles.boughtActionLabel}>{strings.clearBought}</Text>
                  </Pressable>
                </View>
                <View style={styles.sectionItems}>
                {boughtItems.map((item) => (
                  <Swipeable
                    key={item.id}
                    renderRightActions={() => (
                      <View style={[styles.swipeAction, styles.swipeActionOpen]}>
                        <Text style={styles.swipeActionText}>{strings.swipeOpen}</Text>
                      </View>
                    )}
                    onSwipeableOpen={() => selectedList && shopping.toggleItem(selectedList.id, item.id)}
                  >
                    <ShoppingItemRow
                      title={item.title}
                      meta={item.displayMeta}
                      checked
                      secondaryActionLabel={strings.details}
                      onToggle={() => {
                        if (selectedList) {
                          shopping.toggleItem(selectedList.id, item.id);
                        }
                      }}
                      onOpenDetails={() => openEdit({
                        id: item.id,
                        title: item.title,
                        quantity: item.quantity,
                        unit: item.unit,
                        categoryOverrideKey: item.categoryOverride?.key ?? null,
                      })}
                    />
                  </Swipeable>
                ))}
                </View>
              </View>
            ) : null}
          </ShoppingItemSectionCard>
          </ScrollView>
        </View>
      </View>

      <Pressable
        style={[styles.fab, { bottom: Math.max(insets.bottom + 8, 12) }]}
        onPress={() => {
          setShowAddFormDetails(false);
          setShowMoreAddUnits(false);
          workflowActions.setShowAddDetails(true);
        }}
      >
        <Text style={styles.fabText}>+</Text>
      </Pressable>

      {workflowState.editItemId ? (
        <OverlaySheet onClose={closeEdit} sheetStyle={styles.quickAddSheet}>
          <EditItemSheetContent
            styles={styles}
            title={strings.editTitle}
            editNamePlaceholder={strings.editNamePlaceholder}
            editQuantityPlaceholder={strings.editQuantityPlaceholder}
            editCategoryLabel={strings.editCategoryLabel}
            autoCategoryLabel={strings.autoCategoryLabel}
            saveChangesLabel={strings.saveChanges}
            removeItemLabel={strings.removeItem}
            closeLabel={strings.close}
            unitNoneLabel={strings.unitNone}
            unitToggleMoreLabel={strings.unitMore}
            unitToggleLessLabel={strings.unitLess}
            nameValue={workflowState.editName}
            quantityValue={workflowState.editQuantity}
            editUnit={workflowState.editUnit}
            editCategoryOverride={workflowState.editCategoryOverride}
            editError={workflowState.editError}
            showMoreEditUnits={showMoreEditUnits}
            primaryUnitOptions={PRIMARY_UNIT_OPTIONS}
            moreUnitOptions={MORE_UNIT_OPTIONS}
            categoryOptions={CATEGORY_OPTIONS}
            onChangeName={workflowActions.setEditName}
            onChangeQuantity={workflowActions.setEditQuantity}
            onSelectUnit={(value) => {
              workflowActions.setEditUnit(value as ShoppingUnit | null);
              if (!value) {
                workflowActions.setEditQuantity('');
              }
            }}
            onSelectCategoryOverride={(value) => workflowActions.setEditCategoryOverride(value)}
            onToggleMoreUnits={() => setShowMoreEditUnits((prev) => !prev)}
            onSave={handleSaveEdit}
            onRemove={handleRemoveEdit}
            onClose={closeEdit}
          />
        </OverlaySheet>
      ) : null}

      {workflowState.showAddDetails ? (
        <OverlaySheet
          onClose={closeAddDetails}
          sheetStyle={styles.quickAddSheet}
        >
          <AddDetailsSheetContent
            styles={styles}
            title={strings.addAction}
            addPlaceholderExtended={strings.addPlaceholderExtended}
            detailsToggleLabel={strings.addDetailsToggle}
            detailsHideLabel={strings.addDetailsHide}
            addQuantityPlaceholder={strings.addQuantityPlaceholder}
            addItemTitle={strings.addItemTitle}
            successFeedback={workflowState.addDetailsFeedback}
            unitNoneLabel={strings.unitNone}
            unitToggleMoreLabel={strings.unitMore}
            unitToggleLessLabel={strings.unitLess}
            nameValue={workflowState.newItemName}
            quantityValue={workflowState.addQuantity}
            addUnit={workflowState.addUnit}
            addError={workflowState.addError}
            detailsExpanded={showAddFormDetails}
            showMoreAddUnits={showMoreAddUnits}
            inputRef={addDetailsInputRef}
            quantityRef={quantityRef}
            primaryUnitOptions={PRIMARY_UNIT_OPTIONS}
            moreUnitOptions={MORE_UNIT_OPTIONS}
            onChangeName={workflowActions.setNewItemName}
            onSubmitName={async () => {
              if (workflowState.newItemName.trim()) {
                await handleAddItem();
                return;
              }
              setShowAddFormDetails(true);
            }}
            onToggleDetails={() => setShowAddFormDetails((prev) => !prev)}
            onChangeQuantity={(value) => {
              workflowActions.setAddQuantity(value);
              if (value.trim().length > 0 && workflowState.addUnit == null) {
                workflowActions.setAddUnit('PCS');
              }
              if (value.trim().length === 0) {
                workflowActions.setAddUnit(null);
              }
              workflowActions.setAddError(null);
            }}
            onSelectUnit={(value) => {
              workflowActions.setAddUnit(value as ShoppingUnit | null);
              if (value === null) {
                workflowActions.setAddQuantity('');
              }
              workflowActions.setAddError(null);
            }}
            onToggleMoreUnits={() => setShowMoreAddUnits((prev) => !prev)}
            onAddItem={async () => {
              await handleAddItem();
            }}
          />
        </OverlaySheet>
      ) : null}
    </AppScreen>
  );
}

const PRIMARY_UNIT_OPTIONS: { label: string; value: ShoppingUnit }[] = [
  { label: 'pcs', value: 'PCS' },
  { label: 'pack', value: 'PACK' },
  { label: 'kg', value: 'KG' },
];

const MORE_UNIT_OPTIONS: { label: string; value: ShoppingUnit }[] = [
  { label: 'hg', value: 'HG' },
  { label: 'g', value: 'G' },
  { label: 'l', value: 'L' },
  { label: 'dl', value: 'DL' },
  { label: 'ml', value: 'ML' },
];

const CATEGORY_OPTIONS = getShoppingCategoryDefinitions().map((definition) => ({
  label: definition.label,
  value: definition.key,
}));

const styles = StyleSheet.create({
  screenContent: {
    flex: 1,
  },
  contentOffset: {
    flex: 1,
    paddingTop: theme.layout.topBarOffset + theme.spacing.md,
  },
  mainLayout: {
    flex: 1,
  },
  listScroll: {
    flex: 1,
  },
  scrollContent: {
    paddingBottom: 96,
    gap: theme.spacing.sm,
  },
  swipeAction: {
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: theme.spacing.lg,
    marginVertical: theme.spacing.xs,
    borderRadius: theme.radius.md,
  },
  swipeActionBought: {
    backgroundColor: theme.colors.success,
  },
  swipeActionOpen: {
    backgroundColor: theme.colors.feature.shopping,
  },
  swipeActionText: {
    color: '#ffffff',
    fontWeight: '700',
    fontFamily: theme.typography.heading,
  },
  sectionItems: {
    gap: theme.spacing.xs,
  },
  boughtContent: {
    gap: theme.spacing.xs,
  },
  boughtActions: {
    flexDirection: 'row',
    justifyContent: 'flex-start',
  },
  boughtActionButton: {
    minHeight: 28,
    justifyContent: 'center',
  },
  boughtActionButtonPressed: {
    opacity: 0.65,
  },
  boughtActionLabel: {
    ...textStyles.subtle,
    fontWeight: '600',
    color: theme.colors.textSecondary,
  },
  quickEditRow: {
    marginTop: theme.spacing.xs,
    marginLeft: 36,
    padding: theme.spacing.sm,
    borderRadius: theme.radius.md,
    backgroundColor: theme.colors.surface,
    borderWidth: 1,
    borderColor: theme.colors.border,
    gap: theme.spacing.sm,
  },
  quickEditTitle: {
    ...textStyles.subtle,
    textTransform: 'uppercase',
    letterSpacing: 0.8,
  },
  quickEditInputs: {
    gap: theme.spacing.xs,
  },
  quickUnitRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: theme.spacing.xs,
  },
  quickActions: {
    flexDirection: 'row',
    gap: theme.spacing.xs,
  },
  quickInput: {
    maxWidth: 140,
  },
  addDetailsBar: {
    paddingHorizontal: theme.spacing.md,
    paddingTop: theme.spacing.xs,
    paddingBottom: theme.spacing.xs,
    borderTopWidth: 1,
    borderTopColor: theme.colors.border,
    gap: theme.spacing.xs,
  },
  addQuantityInput: {
    maxWidth: 160,
  },
  addUnitRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: theme.spacing.xs,
  },
  sheetActions: {
    gap: theme.spacing.xs,
  },
  quickAddSheet: {
    backgroundColor: theme.colors.surface,
    borderTopLeftRadius: theme.radius.xl,
    borderTopRightRadius: theme.radius.xl,
    borderTopWidth: 1,
    borderTopColor: theme.colors.border,
    maxWidth: theme.layout.sheetMaxWidth,
    alignSelf: 'center',
    width: '100%',
    padding: theme.layout.sheetPadding,
    gap: theme.spacing.xs,
  },
  quickAddHeader: {
    justifyContent: 'center',
  },
  error: {
    color: theme.colors.danger,
    fontFamily: theme.typography.body,
  },
  sheet: {
    backgroundColor: theme.colors.surface,
    borderTopLeftRadius: theme.radius.xl,
    borderTopRightRadius: theme.radius.xl,
    maxWidth: theme.layout.sheetMaxWidth,
    alignSelf: 'center',
    width: '100%',
    padding: theme.layout.sheetPadding,
    gap: theme.spacing.xs,
    borderWidth: 1,
    borderColor: theme.colors.border,
  },
  unitRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: theme.spacing.xs,
  },
  editorActions: {
    gap: theme.spacing.xs,
  },
  editorScroll: {
    maxHeight: '100%',
  },
  editorScrollContent: {
    gap: theme.spacing.xs,
  },
  fab: {
    position: 'absolute',
    right: theme.spacing.lg,
    bottom: theme.spacing.lg,
    width: 56,
    height: 56,
    borderRadius: 28,
    backgroundColor: theme.colors.feature.shopping,
    alignItems: 'center',
    justifyContent: 'center',
    ...theme.elevation.floating,
  },
  fabText: {
    color: '#ffffff',
    fontSize: 28,
    lineHeight: 28,
    fontFamily: theme.typography.heading,
  },
});

