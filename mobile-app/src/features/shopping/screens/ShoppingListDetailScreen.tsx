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
import { QuickAddSheetContent } from '../components/QuickAddSheetContent';
import { ShoppingAddBar } from '../components/ShoppingAddBar';
import { ShoppingItemRow } from '../components/ShoppingItemRow';
import { useShoppingListDetailWorkflow } from '../hooks/useShoppingListDetailWorkflow';
import { useShoppingLists } from '../hooks/useShoppingLists';
import { formatItemMeta, formatItemTitle } from '../utils/shoppingFormatting';
import { useAppBackHandler } from '../../../shared/hooks/useAppBackHandler';
import { type ShoppingUnit } from '../api/shoppingApi';
import { AppButton, AppCard, AppChip, AppInput, AppScreen, BackIconButton, SectionTitle, Subtle, TopBar } from '../../../shared/ui/components';
import { OverlaySheet } from '../../../shared/ui/OverlaySheet';
import { textStyles, theme } from '../../../shared/ui/theme';

type Props = {
  token: string;
  listId: string;
  onBack: () => void;
};

export function ShoppingListDetailScreen({ token, listId, onBack }: Props) {
  const shopping = useShoppingLists(token);
  const workflow = useShoppingListDetailWorkflow({ shopping, listId });
  const { state: workflowState, actions: workflowActions } = workflow;
  const [showMoreEditUnits, setShowMoreEditUnits] = useState(false);
  const [showMoreAddUnits, setShowMoreAddUnits] = useState(false);
  const orderedOpenItemIdsRef = useRef<string[]>([]);
  const draggingOpenItemIdRef = useRef<string | null>(null);
  const dragStartIndexRef = useRef<number | null>(null);
  const dragMovedRef = useRef(false);
  const dragStartPageYRef = useRef<number | null>(null);
  const rowHeightRef = useRef(92);
  const finishingDragRef = useRef(false);
  const ignoreNextOpenPressRef = useRef(false);
  const pendingOpenReorderSyncRef = useRef(false);
  const quickAddInputRef = useRef<TextInput | null>(null);
  const addDetailsInputRef = useRef<TextInput | null>(null);
  const quantityRef = useRef<TextInput | null>(null);

  const selected = useMemo(() => {
    return shopping.lists.find((list) => list.id === listId) ?? null;
  }, [shopping.lists, listId]);

  const items = useMemo(() => selected?.items ?? [], [selected]);
  const openItemsBase = useMemo(() => items.filter((item) => item.status !== 'BOUGHT'), [items]);
  const openItems = useMemo(() => {
    if (workflowState.orderedOpenItemIds.length === 0) {
      return openItemsBase;
    }
    const byId = new Map(openItemsBase.map((item) => [item.id, item]));
    const ordered = workflowState.orderedOpenItemIds
      .map((id) => byId.get(id))
      .filter((item): item is NonNullable<typeof item> => !!item);
    return ordered.length === openItemsBase.length ? ordered : openItemsBase;
  }, [openItemsBase, workflowState.orderedOpenItemIds]);
  const boughtItems = items.filter((item) => item.status === 'BOUGHT');

  const strings = {
    titleFallback: 'Shopping list',
    back: 'Back',
    openLabel: 'Open',
    boughtLabel: 'Bought',
    openCountSuffix: 'open',
    boughtCountSuffix: 'bought',
    details: 'Edit',
    swipeBought: 'Bought',
    swipeOpen: 'Open',
    noOpenItems: 'No open items.',
    noBoughtItems: 'No bought items yet.',
    addPlaceholder: 'Add item…',
    addPlaceholderExtended: 'Add item…',
    addAction: 'Details',
    quickAddTitle: 'Add item',
    quickAddAddedSuffix: 'added to shopping list.',
    loadingItems: 'Loading items...',
    clearBought: 'Clear bought',
    clearBoughtTitle: 'Clear bought items?',
    clearBoughtBody: 'This will remove all bought items from the list.',
    addQuantityPlaceholder: 'Quantity (optional)',
    addErrorQuantity: 'Quantity must be a positive number.',
    addErrorQuantityUnit: 'Quantity and unit must be set together.',
    addDetailsTitle: 'Add details',
    addDetailsAddedSuffix: 'added to shopping list.',
    addItemTitle: 'Add item',
    editTitle: 'Edit item',
    editNamePlaceholder: 'Item name',
    editQuantityPlaceholder: 'Quantity (optional)',
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
    const next = openItemsBase.map((item) => item.id);
    const current = orderedOpenItemIdsRef.current;
    const same = current.length === next.length && current.every((value, index) => value === next[index]);
    if (same) {
      return;
    }
    orderedOpenItemIdsRef.current = next;
    workflowActions.setOrderedOpenItemIds(next);
  }, [workflowActions, workflowState.draggingOpenItemId, openItemsBase]);

  function closeQuickAdd() {
    workflowActions.closeQuickAdd();
    Keyboard.dismiss();
  }

  function closeAddDetails() {
    workflowActions.closeAddDetails();
    setShowMoreAddUnits(false);
    Keyboard.dismiss();
  }

  function openEdit(item: typeof items[number]) {
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

  async function handleQuickAdd() {
    await workflowActions.handleQuickAdd(
      { quickAddAddedSuffix: strings.quickAddAddedSuffix },
      { onRefocus: () => quickAddInputRef.current?.focus() }
    );
  }

  async function handleAddItem() {
    await workflowActions.handleAddItem(
      {
        addErrorQuantity: strings.addErrorQuantity,
        addErrorQuantityUnit: strings.addErrorQuantityUnit,
        addDetailsAddedSuffix: strings.addDetailsAddedSuffix,
      },
      { onRefocus: () => addDetailsInputRef.current?.focus() }
    );
  }

  async function handleClearBought() {
    if (!selected || boughtItems.length === 0) {
      return;
    }
    for (const item of boughtItems) {
      await shopping.removeItem(selected.id, item.id);
    }
  }

  function requestClearBought() {
    if (!selected || boughtItems.length === 0) {
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
    const currentIds = orderedOpenItemIdsRef.current.length > 0
      ? orderedOpenItemIdsRef.current
      : openItemsBase.map((item) => item.id);
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
    isOverlayOpen: workflowState.showQuickAdd || !!workflowState.editItemId || workflowState.showAddDetails,
    onCloseOverlay: () => {
      if (workflowState.showQuickAdd) {
        closeQuickAdd();
        return;
      }
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
          title={selected ? selected.name : strings.titleFallback}
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

          <AppCard style={styles.sectionCard}>
            <View style={styles.sectionHeader}>
              <SectionTitle>{strings.openLabel}</SectionTitle>
              <Subtle>{openItems.length} {strings.openCountSuffix}</Subtle>
            </View>
            {openItems.length === 0 ? (
              <Subtle>{strings.noOpenItems}</Subtle>
            ) : (
              <View style={styles.items}>
                <Subtle>{strings.reorderHint}</Subtle>
                {openItems.map((item) => (
                  <Swipeable
                    key={item.id}
                    enabled={!workflowState.draggingOpenItemId}
                    renderRightActions={() => (
                      <View style={[styles.swipeAction, styles.swipeActionBought]}>
                        <Text style={styles.swipeActionText}>{strings.swipeBought}</Text>
                      </View>
                    )}
                    onSwipeableOpen={() => selected && shopping.toggleItem(selected.id, item.id)}
                  >
                    <ShoppingItemRow
                      item={item}
                      title={formatItemTitle(item)}
                      checked={false}
                      detailLabel={strings.details}
                      dragging={workflowState.draggingOpenItemId === item.id}
                      styles={styles}
                      onToggle={() => {
                        if (ignoreNextOpenPressRef.current) {
                          ignoreNextOpenPressRef.current = false;
                          return;
                        }
                        if (selected && !draggingOpenItemIdRef.current) {
                          shopping.toggleItem(selected.id, item.id);
                        }
                      }}
                      onEdit={() => {
                        if (!draggingOpenItemIdRef.current) {
                          openEdit(item);
                        }
                      }}
                      onToggleLongPress={(event) => startOpenDrag(item.id, event.nativeEvent.pageY)}
                      onToggleTouchMove={handleOpenTouchMove}
                      onToggleTouchEnd={() => {
                        void finishOpenDrag();
                      }}
                      onToggleTouchCancel={() => {
                        void finishOpenDrag();
                      }}
                    />
                  </Swipeable>
                ))}
              </View>
            )}
          </AppCard>

          <AppCard style={styles.sectionCard}>
            <View style={styles.sectionHeader}>
              <SectionTitle>{strings.boughtLabel}</SectionTitle>
              <View style={styles.sectionHeaderRight}>
                <Subtle>{boughtItems.length} {strings.boughtCountSuffix}</Subtle>
                {boughtItems.length > 0 ? (
                  <AppButton
                    title={strings.clearBought}
                    onPress={requestClearBought}
                    variant="ghost"
                  />
                ) : null}
              </View>
            </View>
            {boughtItems.length === 0 ? (
              <Subtle>{strings.noBoughtItems}</Subtle>
            ) : (
              <View style={styles.items}>
                {boughtItems.map((item) => (
                  <Swipeable
                    key={item.id}
                    renderRightActions={() => (
                      <View style={[styles.swipeAction, styles.swipeActionOpen]}>
                        <Text style={styles.swipeActionText}>{strings.swipeOpen}</Text>
                      </View>
                    )}
                    onSwipeableOpen={() => selected && shopping.toggleItem(selected.id, item.id)}
                  >
                    <ShoppingItemRow
                      item={item}
                      title={formatItemTitle(item)}
                      checked
                      detailLabel={strings.details}
                      styles={styles}
                      onToggle={() => {
                        if (selected) {
                          shopping.toggleItem(selected.id, item.id);
                        }
                      }}
                      onEdit={() => openEdit(item)}
                    />
                  </Swipeable>
                ))}
              </View>
            )}
          </AppCard>
          </ScrollView>

          <ShoppingAddBar
            styles={styles}
            placeholder={strings.addPlaceholder}
            actionTitle={strings.addAction}
            onPressInput={() => {
              workflowActions.setQuickAddName('');
              workflowActions.setShowQuickAdd(true);
            }}
            onPressAction={() => {
              Keyboard.dismiss();
              workflowActions.setShowAddDetails(true);
            }}
          />
        </View>
      </View>


      {workflowState.editItemId ? (
        <OverlaySheet onClose={closeEdit} sheetStyle={styles.quickAddSheet}>
          <EditItemSheetContent
            styles={styles}
            title={strings.editTitle}
            editNamePlaceholder={strings.editNamePlaceholder}
            editQuantityPlaceholder={strings.editQuantityPlaceholder}
            saveChangesLabel={strings.saveChanges}
            removeItemLabel={strings.removeItem}
            closeLabel={strings.close}
            unitNoneLabel={strings.unitNone}
            unitToggleMoreLabel={strings.unitMore}
            unitToggleLessLabel={strings.unitLess}
            nameValue={workflowState.editName}
            quantityValue={workflowState.editQuantity}
            editUnit={workflowState.editUnit}
            editError={workflowState.editError}
            showMoreEditUnits={showMoreEditUnits}
            primaryUnitOptions={PRIMARY_UNIT_OPTIONS}
            moreUnitOptions={MORE_UNIT_OPTIONS}
            onChangeName={workflowActions.setEditName}
            onChangeQuantity={workflowActions.setEditQuantity}
            onSelectUnit={(value) => {
              workflowActions.setEditUnit(value as ShoppingUnit | null);
              if (!value) {
                workflowActions.setEditQuantity('');
              }
            }}
            onToggleMoreUnits={() => setShowMoreEditUnits((prev) => !prev)}
            onSave={handleSaveEdit}
            onRemove={handleRemoveEdit}
            onClose={closeEdit}
          />
        </OverlaySheet>
      ) : null}

      {workflowState.showQuickAdd ? (
        <OverlaySheet
          onClose={closeQuickAdd}
          sheetStyle={styles.quickAddSheet}
          aboveSheet={
            workflowState.quickAddFeedback ? (
              <View style={styles.quickAddFeedback}>
                <Text style={styles.quickAddFeedbackText}>{workflowState.quickAddFeedback}</Text>
              </View>
            ) : null
          }
        >
          <QuickAddSheetContent
            styles={styles}
            title={strings.quickAddTitle}
            placeholder={strings.addPlaceholder}
            value={workflowState.quickAddName}
            inputRef={quickAddInputRef}
            onChangeText={workflowActions.setQuickAddName}
            onSubmitEditing={async () => {
              if (workflowState.quickAddName.trim()) {
                await handleQuickAdd();
                return;
              }
              closeQuickAdd();
            }}
          />
        </OverlaySheet>
      ) : null}

      {workflowState.showAddDetails ? (
        <OverlaySheet
          onClose={closeAddDetails}
          sheetStyle={styles.quickAddSheet}
          aboveSheet={
            workflowState.addDetailsFeedback ? (
              <View style={styles.quickAddFeedback}>
                <Text style={styles.quickAddFeedbackText}>{workflowState.addDetailsFeedback}</Text>
              </View>
            ) : null
          }
        >
          <AddDetailsSheetContent
            styles={styles}
            title={strings.addDetailsTitle}
            addPlaceholderExtended={strings.addPlaceholderExtended}
            addQuantityPlaceholder={strings.addQuantityPlaceholder}
            addItemTitle={strings.addItemTitle}
            closeLabel={strings.close}
            unitNoneLabel={strings.unitNone}
            unitToggleMoreLabel={strings.unitMore}
            unitToggleLessLabel={strings.unitLess}
            nameValue={workflowState.newItemName}
            quantityValue={workflowState.addQuantity}
            addUnit={workflowState.addUnit}
            addError={workflowState.addError}
            showMoreAddUnits={showMoreAddUnits}
            inputRef={addDetailsInputRef}
            quantityRef={quantityRef}
            primaryUnitOptions={PRIMARY_UNIT_OPTIONS}
            moreUnitOptions={MORE_UNIT_OPTIONS}
            onChangeName={workflowActions.setNewItemName}
            onSubmitName={async () => {
              if (workflowState.newItemName.trim()) {
                await handleAddItem();
              }
            }}
            onChangeQuantity={(value) => {
              workflowActions.setAddQuantity(value);
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
            onClose={closeAddDetails}
          />
        </OverlaySheet>
      ) : null}
    </AppScreen>
  );
}

const PRIMARY_UNIT_OPTIONS: { label: string; value: ShoppingUnit }[] = [
  { label: 'pcs', value: 'ST' },
  { label: 'pack', value: 'FORP' },
  { label: 'kg', value: 'KG' },
];

const MORE_UNIT_OPTIONS: { label: string; value: ShoppingUnit }[] = [
  { label: 'hg', value: 'HG' },
  { label: 'g', value: 'G' },
  { label: 'l', value: 'L' },
  { label: 'dl', value: 'DL' },
  { label: 'ml', value: 'ML' },
];

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
    paddingBottom: theme.spacing.md,
    gap: theme.spacing.md,
  },
  sectionCard: {
    gap: theme.spacing.xs,
  },
  sectionHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  sectionHeaderRight: {
    flexDirection: 'row',
    alignItems: 'center',
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
  items: {
    gap: theme.spacing.xs,
  },
  itemRow: {
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    padding: theme.spacing.sm,
    backgroundColor: theme.colors.surfaceAlt,
    flexDirection: 'row',
    alignItems: 'center',
    gap: theme.spacing.sm,
  },
  checkboxPressable: {
    padding: theme.spacing.xs,
  },
  checkbox: {
    width: 22,
    height: 22,
    borderRadius: theme.radius.sm,
    borderWidth: 1,
    borderColor: theme.colors.borderStrong,
    backgroundColor: theme.colors.surfaceAlt,
    alignItems: 'center',
    justifyContent: 'center',
  },
  checkboxChecked: {
    backgroundColor: theme.colors.success,
    borderColor: theme.colors.success,
  },
  checkboxMark: {
    fontSize: 14,
    color: 'transparent',
    fontWeight: '700',
  },
  checkboxMarkChecked: {
    color: '#ffffff',
  },
  itemText: {
    ...textStyles.body,
  },
  itemHintText: {
    ...textStyles.subtle,
  },
  itemHintChevron: {
    ...textStyles.subtle,
    fontSize: 18,
    lineHeight: 18,
  },
  itemMeta: {
    ...textStyles.subtle,
  },
  itemContent: {
    flex: 1,
    gap: theme.spacing.xs,
  },
  toggleZone: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    gap: theme.spacing.sm,
  },
  detailZone: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: theme.spacing.xs,
    paddingLeft: theme.spacing.sm,
    borderLeftWidth: 1,
    borderLeftColor: theme.colors.border,
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
  itemTextDone: {
    color: theme.colors.subtle,
    textDecorationLine: 'line-through',
  },
  bottomContainer: {
    borderTopWidth: 1,
    borderTopColor: theme.colors.border,
    backgroundColor: theme.colors.surface,
  },
  bottomBar: {
    paddingTop: theme.spacing.xs,
    paddingBottom: theme.spacing.xs,
    paddingHorizontal: theme.spacing.md,
    flexDirection: 'row',
    gap: theme.spacing.xs,
    alignItems: 'center',
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
  bottomInput: {
    flex: 1,
  },
  itemRowDragging: {
    borderColor: theme.colors.feature.shopping,
    opacity: 0.85,
  },
  bottomInputPressable: {
    flex: 1,
    borderWidth: 1,
    borderColor: theme.colors.borderStrong,
    borderRadius: theme.radius.md,
    paddingVertical: theme.spacing.xs,
    paddingHorizontal: theme.spacing.sm,
    backgroundColor: theme.colors.surfaceAlt,
    justifyContent: 'center',
  },
  bottomInputPlaceholder: {
    fontFamily: theme.typography.body,
    fontSize: 15,
    color: theme.colors.subtle,
  },
  quickAddHeader: {
    justifyContent: 'center',
  },
  quickAddFeedback: {
    alignSelf: 'center',
    maxWidth: '100%',
    backgroundColor: theme.colors.success,
    borderRadius: theme.radius.md,
    paddingHorizontal: theme.spacing.md,
    paddingVertical: theme.spacing.xs,
  },
  quickAddFeedbackText: {
    color: '#ffffff',
    fontFamily: theme.typography.body,
    fontSize: 13,
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
});

