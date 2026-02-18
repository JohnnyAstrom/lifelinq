import { useEffect, useMemo, useRef, useState } from 'react';
import {
  Alert,
  GestureResponderEvent,
  Keyboard,
  Modal,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import { Swipeable } from 'react-native-gesture-handler';
import { useShoppingLists } from '../features/shopping/hooks/useShoppingLists';
import { type ShoppingUnit } from '../shared/api/shopping';
import { AppButton, AppCard, AppChip, AppInput, AppScreen, SectionTitle, Subtle, TopBar } from '../shared/ui/components';
import { OverlaySheet } from '../shared/ui/OverlaySheet';
import { textStyles, theme } from '../shared/ui/theme';

type Props = {
  token: string;
  listId: string;
  onBack: () => void;
};

export function ShoppingListDetailScreen({ token, listId, onBack }: Props) {
  const shopping = useShoppingLists(token);
  const [newItemName, setNewItemName] = useState('');
  const [quickAddName, setQuickAddName] = useState('');
  const [quickAddFeedback, setQuickAddFeedback] = useState<string | null>(null);
  const [showQuickAdd, setShowQuickAdd] = useState(false);
  const [editItemId, setEditItemId] = useState<string | null>(null);
  const [editName, setEditName] = useState('');
  const [editQuantity, setEditQuantity] = useState('');
  const [editUnit, setEditUnit] = useState<ShoppingUnit | null>('ST');
  const [editError, setEditError] = useState<string | null>(null);
  const [showMoreEditUnits, setShowMoreEditUnits] = useState(false);
  const [showAddDetails, setShowAddDetails] = useState(false);
  const [addQuantity, setAddQuantity] = useState('');
  const [addUnit, setAddUnit] = useState<ShoppingUnit | null>('ST');
  const [addError, setAddError] = useState<string | null>(null);
  const [addDetailsFeedback, setAddDetailsFeedback] = useState<string | null>(null);
  const [showMoreAddUnits, setShowMoreAddUnits] = useState(false);
  const [orderedOpenItemIds, setOrderedOpenItemIds] = useState<string[]>([]);
  const [draggingOpenItemId, setDraggingOpenItemId] = useState<string | null>(null);
  const orderedOpenItemIdsRef = useRef<string[]>([]);
  const draggingOpenItemIdRef = useRef<string | null>(null);
  const dragStartIndexRef = useRef<number | null>(null);
  const dragMovedRef = useRef(false);
  const dragStartPageYRef = useRef<number | null>(null);
  const rowHeightRef = useRef(92);
  const finishingDragRef = useRef(false);
  const ignoreNextOpenPressRef = useRef(false);
  const pendingOpenReorderSyncRef = useRef(false);
  const quickAddFeedbackTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const addDetailsFeedbackTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const quickAddInputRef = useRef<TextInput | null>(null);
  const addDetailsInputRef = useRef<TextInput | null>(null);

  const selected = useMemo(() => {
    return shopping.lists.find((list) => list.id === listId) ?? null;
  }, [shopping.lists, listId]);

  const items = useMemo(() => selected?.items ?? [], [selected]);
  const openItemsBase = useMemo(() => items.filter((item) => item.status !== 'BOUGHT'), [items]);
  const openItems = useMemo(() => {
    if (orderedOpenItemIds.length === 0) {
      return openItemsBase;
    }
    const byId = new Map(openItemsBase.map((item) => [item.id, item]));
    const ordered = orderedOpenItemIds
      .map((id) => byId.get(id))
      .filter((item): item is NonNullable<typeof item> => !!item);
    return ordered.length === openItemsBase.length ? ordered : openItemsBase;
  }, [openItemsBase, orderedOpenItemIds]);
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
    if (draggingOpenItemId || pendingOpenReorderSyncRef.current) {
      return;
    }
    const next = openItemsBase.map((item) => item.id);
    const current = orderedOpenItemIdsRef.current;
    const same = current.length === next.length && current.every((value, index) => value === next[index]);
    if (same) {
      return;
    }
    orderedOpenItemIdsRef.current = next;
    setOrderedOpenItemIds(next);
  }, [draggingOpenItemId, openItemsBase]);

  useEffect(() => {
    return () => {
      if (quickAddFeedbackTimerRef.current) {
        clearTimeout(quickAddFeedbackTimerRef.current);
        quickAddFeedbackTimerRef.current = null;
      }
      if (addDetailsFeedbackTimerRef.current) {
        clearTimeout(addDetailsFeedbackTimerRef.current);
        addDetailsFeedbackTimerRef.current = null;
      }
    };
  }, []);

  async function handleAddItem() {
    if (!selected || !newItemName.trim()) {
      return;
    }
    const addedName = newItemName.trim();
    const parsedQuantity = parseQuantity(addQuantity);
    if (Number.isNaN(parsedQuantity)) {
      setAddError(strings.addErrorQuantity);
      return;
    }
    if (parsedQuantity !== null && !addUnit) {
      setAddError(strings.addErrorQuantityUnit);
      return;
    }
    const effectiveUnit = parsedQuantity === null ? null : addUnit;
    await shopping.addItem(selected.id, addedName, parsedQuantity, effectiveUnit);
    setNewItemName('');
    setAddQuantity('');
    setAddUnit('ST');
    setAddError(null);
    const quantityPrefix =
      parsedQuantity !== null && effectiveUnit
        ? `${formatQuantityForFeedback(parsedQuantity)} ${formatUnitForFeedback(effectiveUnit)} - `
        : '';
    setAddDetailsFeedback(`${quantityPrefix}${addedName} ${strings.addDetailsAddedSuffix}`);
    if (addDetailsFeedbackTimerRef.current) {
      clearTimeout(addDetailsFeedbackTimerRef.current);
    }
    addDetailsFeedbackTimerRef.current = setTimeout(() => {
      setAddDetailsFeedback(null);
      addDetailsFeedbackTimerRef.current = null;
    }, 3200);
    requestAnimationFrame(() => {
      addDetailsInputRef.current?.focus();
    });
  }

  function formatQuantityForFeedback(value: number): string {
    return Number.isInteger(value) ? String(value) : String(value);
  }

  function formatUnitForFeedback(unit: ShoppingUnit): string {
    switch (unit) {
      case 'ST':
        return 'pcs';
      case 'FORP':
        return 'pack';
      case 'KG':
        return 'kg';
      case 'HG':
        return 'hg';
      case 'G':
        return 'g';
      case 'L':
        return 'l';
      case 'DL':
        return 'dl';
      case 'ML':
        return 'ml';
      default:
        return '';
    }
  }

  async function handleQuickAdd() {
    if (!selected || !quickAddName.trim()) {
      return;
    }
    const addedName = quickAddName.trim();
    await shopping.addItem(selected.id, addedName, null, null);
    setQuickAddName('');
    setQuickAddFeedback(`${addedName} ${strings.quickAddAddedSuffix}`);
    if (quickAddFeedbackTimerRef.current) {
      clearTimeout(quickAddFeedbackTimerRef.current);
    }
    quickAddFeedbackTimerRef.current = setTimeout(() => {
      setQuickAddFeedback(null);
      quickAddFeedbackTimerRef.current = null;
    }, 3200);
    requestAnimationFrame(() => {
      quickAddInputRef.current?.focus();
    });
  }

  function closeQuickAdd() {
    setQuickAddName('');
    setQuickAddFeedback(null);
    if (quickAddFeedbackTimerRef.current) {
      clearTimeout(quickAddFeedbackTimerRef.current);
      quickAddFeedbackTimerRef.current = null;
    }
    setShowQuickAdd(false);
    Keyboard.dismiss();
  }

  function closeAddDetails() {
    setShowAddDetails(false);
    setShowMoreAddUnits(false);
    setAddDetailsFeedback(null);
    if (addDetailsFeedbackTimerRef.current) {
      clearTimeout(addDetailsFeedbackTimerRef.current);
      addDetailsFeedbackTimerRef.current = null;
    }
    Keyboard.dismiss();
  }

  function openEdit(item: typeof items[number]) {
    setEditItemId(item.id);
    setEditName(item.name);
    setEditQuantity(item.quantity ? String(item.quantity) : '');
    setEditUnit(item.unit ?? 'ST');
    setEditError(null);
    setShowMoreEditUnits(false);
  }

  function closeEdit() {
    setEditItemId(null);
    setEditName('');
    setEditQuantity('');
    setEditUnit('ST');
    setEditError(null);
    setShowMoreEditUnits(false);
    Keyboard.dismiss();
  }

  function parseQuantity(value: string): number | null {
    if (!value.trim()) {
      return null;
    }
    const parsed = Number(value.replace(',', '.'));
    if (!Number.isFinite(parsed) || parsed <= 0) {
      return NaN;
    }
    return parsed;
  }

  async function handleSaveEdit() {
    if (!selected || !editItemId) {
      return;
    }
    if (!editName.trim()) {
      setEditError(strings.nameRequired);
      return;
    }
    const parsedQuantity = parseQuantity(editQuantity);
    if (Number.isNaN(parsedQuantity)) {
      setEditError(strings.quantityInvalid);
      return;
    }
    if (parsedQuantity !== null && !editUnit) {
      setEditError(strings.quantityUnitMismatch);
      return;
    }
    const effectiveUnit = parsedQuantity === null ? null : editUnit;
    await shopping.updateItem(
      selected.id,
      editItemId,
      editName.trim(),
      parsedQuantity,
      effectiveUnit
    );
    closeEdit();
  }

  async function handleRemoveEdit() {
    if (!selected || !editItemId) {
      return;
    }
    await shopping.removeItem(selected.id, editItemId);
    closeEdit();
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

  function formatItemMeta(item: typeof items[number]) {
    if (item.quantity == null || !item.unit) {
      return null;
    }
    const label = UNIT_LABELS[item.unit] ?? item.unit.toLowerCase();
    return `${item.quantity} ${label}`;
  }

  function formatItemTitle(item: typeof items[number]) {
    const meta = formatItemMeta(item);
    if (meta) {
      return `${meta} - ${item.name}`;
    }
    return item.name;
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
    setOrderedOpenItemIds(currentIds);
    setDraggingOpenItemId(itemId);
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
    setOrderedOpenItemIds(next);
  }

  async function finishOpenDrag() {
    if (finishingDragRef.current) {
      return;
    }
    finishingDragRef.current = true;
    const draggedId = draggingOpenItemIdRef.current;
    const startIndex = dragStartIndexRef.current;
    if (!selected || !draggedId || startIndex === null) {
      finishingDragRef.current = false;
      return;
    }
    const moved = dragMovedRef.current;
    const finalIndex = orderedOpenItemIdsRef.current.indexOf(draggedId);
    setDraggingOpenItemId(null);
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
    const direction = finalIndex > startIndex ? 'DOWN' : 'UP';
    const steps = Math.abs(finalIndex - startIndex);
    try {
      pendingOpenReorderSyncRef.current = true;
      await shopping.reorderItem(selected.id, draggedId, direction, steps);
    } finally {
      pendingOpenReorderSyncRef.current = false;
      finishingDragRef.current = false;
    }
  }

  return (
    <AppScreen scroll={false} contentStyle={styles.screenContent}>
      <TopBar
        title={selected ? selected.name : strings.titleFallback}
        left={<AppButton title={strings.back} onPress={onBack} variant="ghost" />}
      />

      <View style={styles.mainLayout}>
        <ScrollView
          style={styles.listScroll}
          contentContainerStyle={styles.scrollContent}
          scrollEnabled={!draggingOpenItemId}
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
                    enabled={!draggingOpenItemId}
                    renderRightActions={() => (
                      <View style={[styles.swipeAction, styles.swipeActionBought]}>
                        <Text style={styles.swipeActionText}>{strings.swipeBought}</Text>
                      </View>
                    )}
                    onSwipeableOpen={() => selected && shopping.toggleItem(selected.id, item.id)}
                  >
                    <View style={[styles.itemRow, draggingOpenItemId === item.id ? styles.itemRowDragging : null]}>
                      <Pressable
                        style={styles.toggleZone}
                        onLongPress={(event) => startOpenDrag(item.id, event.nativeEvent.pageY)}
                        delayLongPress={180}
                        onTouchMove={handleOpenTouchMove}
                        onTouchEnd={() => {
                          void finishOpenDrag();
                        }}
                        onTouchCancel={() => {
                          void finishOpenDrag();
                        }}
                        onPress={() => {
                          if (ignoreNextOpenPressRef.current) {
                            ignoreNextOpenPressRef.current = false;
                            return;
                          }
                          if (selected && !draggingOpenItemIdRef.current) {
                            shopping.toggleItem(selected.id, item.id);
                          }
                        }}
                      >
                        <View style={styles.checkbox} />
                        <View style={styles.itemContent}>
                          <Text style={styles.itemText}>{formatItemTitle(item)}</Text>
                        </View>
                      </Pressable>
                      <Pressable
                        style={styles.detailZone}
                        onPress={() => {
                          if (!draggingOpenItemIdRef.current) {
                            openEdit(item);
                          }
                        }}
                      >
                        <Text style={styles.itemHintText}>{strings.details}</Text>
                        <Text style={styles.itemHintChevron}>›</Text>
                      </Pressable>
                    </View>
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
                    <View style={styles.itemRow}>
                      <Pressable
                        style={styles.toggleZone}
                        onPress={() => {
                          if (selected) {
                            shopping.toggleItem(selected.id, item.id);
                          }
                        }}
                      >
                        <View style={[styles.checkbox, styles.checkboxChecked]}>
                          <Text style={[styles.checkboxMark, styles.checkboxMarkChecked]}>✓</Text>
                        </View>
                        <View style={styles.itemContent}>
                          <Text style={[styles.itemText, styles.itemTextDone]}>{formatItemTitle(item)}</Text>
                        </View>
                      </Pressable>
                      <Pressable style={styles.detailZone} onPress={() => openEdit(item)}>
                        <Text style={[styles.itemHintText, styles.itemTextDone]}>{strings.details}</Text>
                        <Text style={[styles.itemHintChevron, styles.itemTextDone]}>›</Text>
                      </Pressable>
                    </View>
                  </Swipeable>
                ))}
              </View>
            )}
          </AppCard>
        </ScrollView>

      <View style={styles.bottomContainer}>
        <View style={styles.bottomBar}>
          <Pressable
            style={styles.bottomInputPressable}
            onPress={() => {
              setQuickAddName('');
              setShowQuickAdd(true);
            }}
          >
            <Text style={styles.bottomInputPlaceholder}>{strings.addPlaceholder}</Text>
          </Pressable>
          <AppButton
            title={strings.addAction}
            onPress={() => {
              Keyboard.dismiss();
              setShowAddDetails(true);
            }}
          />
        </View>
      </View>
      </View>


      {editItemId ? (
        <OverlaySheet onClose={closeEdit} sheetStyle={styles.quickAddSheet}>
          <View style={styles.sheetHandle} />
          <ScrollView
            style={styles.editorScroll}
            contentContainerStyle={styles.editorScrollContent}
            keyboardShouldPersistTaps="handled"
            showsVerticalScrollIndicator={false}
          >
            <View style={styles.quickAddHeader}>
              <Text style={textStyles.h3}>{strings.editTitle}</Text>
            </View>
            <AppInput
              placeholder={strings.editNamePlaceholder}
              value={editName}
              onChangeText={setEditName}
            />
            <AppInput
              placeholder={strings.editQuantityPlaceholder}
              value={editQuantity}
              onChangeText={setEditQuantity}
              keyboardType="decimal-pad"
            />
            <View style={styles.unitRow}>
              {PRIMARY_UNIT_OPTIONS.map((unit) => (
                <AppChip
                  key={unit.value}
                  label={unit.label}
                  active={editUnit === unit.value}
                  onPress={() => setEditUnit(unit.value)}
                />
              ))}
              <AppChip
                label={strings.unitNone}
                active={!editUnit}
                onPress={() => {
                  setEditUnit(null);
                  setEditQuantity('');
                }}
              />
              <AppChip
                label={showMoreEditUnits ? strings.unitLess : strings.unitMore}
                active={showMoreEditUnits}
                onPress={() => setShowMoreEditUnits((prev) => !prev)}
              />
            </View>
            {showMoreEditUnits ? (
              <View style={styles.addUnitRow}>
                {MORE_UNIT_OPTIONS.map((unit) => (
                  <AppChip
                    key={unit.value}
                    label={unit.label}
                    active={editUnit === unit.value}
                    onPress={() => setEditUnit(unit.value)}
                  />
                ))}
              </View>
            ) : null}
            {editError ? <Text style={styles.error}>{editError}</Text> : null}
            <View style={styles.editorActions}>
              <AppButton title={strings.saveChanges} onPress={handleSaveEdit} fullWidth />
              <AppButton title={strings.removeItem} onPress={handleRemoveEdit} variant="ghost" fullWidth />
              <AppButton title={strings.close} onPress={closeEdit} variant="secondary" fullWidth />
            </View>
          </ScrollView>
        </OverlaySheet>
      ) : null}

      {showQuickAdd ? (
        <OverlaySheet
          onClose={closeQuickAdd}
          sheetStyle={styles.quickAddSheet}
          aboveSheet={
            quickAddFeedback ? (
              <View style={styles.quickAddFeedback}>
                <Text style={styles.quickAddFeedbackText}>{quickAddFeedback}</Text>
              </View>
            ) : null
          }
        >
          <View style={styles.sheetHandle} />
          <View style={styles.quickAddHeader}>
            <Text style={textStyles.h3}>{strings.quickAddTitle}</Text>
          </View>
          <AppInput
            ref={quickAddInputRef}
            placeholder={strings.addPlaceholder}
            value={quickAddName}
            onChangeText={setQuickAddName}
            autoFocus
            blurOnSubmit={false}
            onSubmitEditing={async () => {
              if (quickAddName.trim()) {
                await handleQuickAdd();
                return;
              }
              closeQuickAdd();
            }}
            returnKeyType="done"
          />
        </OverlaySheet>
      ) : null}

      <Modal
        visible={showAddDetails}
        transparent
        animationType="slide"
        onRequestClose={closeAddDetails}
      >
        <Pressable style={styles.backdrop} onPress={closeAddDetails}>
          <View style={styles.modalContent}>
            {addDetailsFeedback ? (
              <View style={styles.aboveSheetFeedback}>
                <View style={styles.quickAddFeedback}>
                  <Text style={styles.quickAddFeedbackText}>{addDetailsFeedback}</Text>
                </View>
              </View>
            ) : null}
            <Pressable style={styles.quickAddSheet} onPress={() => null}>
              <View style={styles.sheetHandle} />
              <View style={styles.quickAddHeader}>
                <Text style={textStyles.h3}>{strings.addDetailsTitle}</Text>
              </View>
              <AppInput
                ref={addDetailsInputRef}
                placeholder={strings.addPlaceholderExtended}
                value={newItemName}
                onChangeText={setNewItemName}
                onSubmitEditing={async () => {
                  if (newItemName.trim()) {
                    await handleAddItem();
                  }
                }}
                returnKeyType="done"
              />
              <AppInput
                value={addQuantity}
                onChangeText={(value) => {
                  setAddQuantity(value);
                  setAddError(null);
                }}
                placeholder={strings.addQuantityPlaceholder}
                keyboardType="decimal-pad"
                style={styles.addQuantityInput}
              />
              <View style={styles.addUnitRow}>
                {PRIMARY_UNIT_OPTIONS.map((unit) => (
                  <AppChip
                    key={unit.value}
                    label={unit.label}
                    active={addUnit === unit.value}
                    onPress={() => {
                      Keyboard.dismiss();
                      setAddUnit(unit.value);
                      setAddError(null);
                    }}
                  />
                ))}
                <AppChip
                  label={strings.unitNone}
                  active={!addUnit}
                  onPress={() => {
                    Keyboard.dismiss();
                    setAddUnit(null);
                    setAddQuantity('');
                    setAddError(null);
                  }}
                />
                <AppChip
                  label={showMoreAddUnits ? strings.unitLess : strings.unitMore}
                  active={showMoreAddUnits}
                  onPress={() => setShowMoreAddUnits((prev) => !prev)}
                />
              </View>
              {showMoreAddUnits ? (
                <View style={styles.addUnitRow}>
                  {MORE_UNIT_OPTIONS.map((unit) => (
                    <AppChip
                      key={unit.value}
                      label={unit.label}
                      active={addUnit === unit.value}
                      onPress={() => {
                        Keyboard.dismiss();
                        setAddUnit(unit.value);
                        setAddError(null);
                      }}
                    />
                  ))}
                </View>
              ) : null}
              {addError ? <Text style={styles.error}>{addError}</Text> : null}
              <View style={styles.sheetActions}>
                <AppButton
                  title={strings.addItemTitle}
                  onPress={async () => {
                    await handleAddItem();
                  }}
                  disabled={newItemName.trim().length === 0}
                  fullWidth
                />
                <AppButton title={strings.close} onPress={closeAddDetails} variant="ghost" fullWidth />
              </View>
            </Pressable>
          </View>
        </Pressable>
      </Modal>
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

const UNIT_LABELS: Record<ShoppingUnit, string> = {
  ST: 'pcs',
  FORP: 'pack',
  KG: 'kg',
  HG: 'hg',
  G: 'g',
  L: 'l',
  DL: 'dl',
  ML: 'ml',
};

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
    paddingBottom: theme.spacing.lg,
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
  backdrop: {
    position: 'absolute',
    left: 0,
    right: 0,
    top: 0,
    bottom: 0,
    backgroundColor: 'rgba(0,0,0,0.4)',
    justifyContent: 'flex-end',
  },
  modalContent: {
    flex: 1,
    justifyContent: 'flex-end',
  },
  aboveSheetFeedback: {
    paddingHorizontal: theme.spacing.lg,
    paddingBottom: theme.spacing.sm,
    alignItems: 'center',
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
