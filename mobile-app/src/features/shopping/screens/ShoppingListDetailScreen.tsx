import { useEffect, useMemo, useRef, useState } from 'react';
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
    <AppScreen scroll={false} contentStyle={styles.screenContent}>
      <TopBar
        title={selected ? selected.name : strings.titleFallback}
        right={<BackIconButton onPress={onBack} />}
      />

      <View style={styles.mainLayout}>
        <ScrollView
          style={styles.listScroll}
          contentContainerStyle={styles.scrollContent}
          scrollEnabled={!workflowState.draggingOpenItemId}
          keyboardShouldPersistTaps="handled"
        >
          {shopping.loading ? <Subtle>{strings.loadingItems}</Subtle> : null}
          {shopping.error ? <Text style={styles.error}>{shopping.error}</Text> : null}

          <AppCard>
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

          <AppCard>
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
    padding: 0,
    flex: 1,
  },
  mainLayout: {
    flex: 1,
  },
  listScroll: {
    flex: 1,
  },
  scrollContent: {
    padding: theme.spacing.lg,
    paddingTop: 90,
    paddingBottom: theme.spacing.md,
    gap: theme.spacing.md,
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
    marginVertical: 2,
    borderRadius: theme.radius.md,
  },
  swipeActionBought: {
    backgroundColor: theme.colors.success,
  },
  swipeActionOpen: {
    backgroundColor: theme.colors.primary,
  },
  swipeActionText: {
    color: '#ffffff',
    fontWeight: '700',
    fontFamily: theme.typography.heading,
  },
  items: {
    gap: theme.spacing.sm,
    marginTop: theme.spacing.sm,
  },
  itemRow: {
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    padding: theme.spacing.md,
    backgroundColor: theme.colors.surfaceAlt,
    flexDirection: 'row',
    alignItems: 'center',
    gap: theme.spacing.sm,
  },
  checkboxPressable: {
    padding: 2,
  },
  checkbox: {
    width: 22,
    height: 22,
    borderRadius: 6,
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
    gap: 2,
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
    gap: 4,
    paddingLeft: theme.spacing.sm,
    borderLeftWidth: 1,
    borderLeftColor: theme.colors.border,
  },
  quickEditRow: {
    marginTop: theme.spacing.sm,
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
    gap: theme.spacing.sm,
  },
  quickUnitRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: theme.spacing.sm,
  },
  quickActions: {
    flexDirection: 'row',
    gap: theme.spacing.sm,
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
    paddingTop: theme.spacing.sm,
    paddingBottom: theme.spacing.sm,
    paddingHorizontal: theme.spacing.lg,
    flexDirection: 'row',
    gap: theme.spacing.sm,
    alignItems: 'center',
  },
  addDetailsBar: {
    paddingHorizontal: theme.spacing.lg,
    paddingTop: theme.spacing.sm,
    paddingBottom: theme.spacing.sm,
    borderTopWidth: 1,
    borderTopColor: theme.colors.border,
    gap: theme.spacing.sm,
  },
  addQuantityInput: {
    maxWidth: 160,
  },
  addUnitRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: theme.spacing.sm,
  },
  sheetActions: {
    gap: theme.spacing.sm,
  },
  quickAddSheet: {
    backgroundColor: theme.colors.surface,
    borderTopLeftRadius: 0,
    borderTopRightRadius: 0,
    borderTopWidth: 1,
    borderTopColor: theme.colors.border,
    paddingHorizontal: theme.spacing.lg,
    paddingTop: theme.spacing.md,
    paddingBottom: theme.spacing.md,
    gap: theme.spacing.sm,
  },
  bottomInput: {
    flex: 1,
  },
  itemRowDragging: {
    borderColor: theme.colors.primary,
    opacity: 0.85,
  },
  bottomInputPressable: {
    flex: 1,
    borderWidth: 1,
    borderColor: theme.colors.borderStrong,
    borderRadius: theme.radius.md,
    paddingVertical: 10,
    paddingHorizontal: 12,
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
    paddingVertical: theme.spacing.sm,
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
    padding: theme.spacing.lg,
    gap: theme.spacing.sm,
    borderWidth: 1,
    borderColor: theme.colors.border,
  },
  sheetHandle: {
    alignSelf: 'center',
    width: 48,
    height: 5,
    borderRadius: 999,
    backgroundColor: theme.colors.borderStrong,
    marginBottom: theme.spacing.sm,
  },
  unitRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: theme.spacing.sm,
  },
  editorActions: {
    gap: theme.spacing.sm,
  },
  editorScroll: {
    maxHeight: '100%',
  },
  editorScrollContent: {
    gap: theme.spacing.sm,
    paddingBottom: theme.spacing.md,
  },
});
