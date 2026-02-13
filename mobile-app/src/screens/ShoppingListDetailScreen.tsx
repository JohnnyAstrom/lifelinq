import { useMemo, useState } from 'react';
import { Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { Swipeable } from 'react-native-gesture-handler';
import { useShoppingLists } from '../features/shopping/hooks/useShoppingLists';
import { type ShoppingUnit } from '../shared/api/shopping';
import { AppButton, AppCard, AppChip, AppInput, AppScreen, SectionTitle, Subtle } from '../shared/ui/components';
import { textStyles, theme } from '../shared/ui/theme';

type Props = {
  token: string;
  listId: string;
  onBack: () => void;
};

export function ShoppingListDetailScreen({ token, listId, onBack }: Props) {
  const shopping = useShoppingLists(token);
  const [newItemName, setNewItemName] = useState('');
  const [editItemId, setEditItemId] = useState<string | null>(null);
  const [editName, setEditName] = useState('');
  const [editQuantity, setEditQuantity] = useState('');
  const [editUnit, setEditUnit] = useState<ShoppingUnit | null>('ST');
  const [editError, setEditError] = useState<string | null>(null);
  const [showAddDetails, setShowAddDetails] = useState(false);
  const [addQuantity, setAddQuantity] = useState('');
  const [addUnit, setAddUnit] = useState<ShoppingUnit | null>('ST');
  const [addError, setAddError] = useState<string | null>(null);

  const selected = useMemo(() => {
    return shopping.lists.find((list) => list.id === listId) ?? null;
  }, [shopping.lists, listId]);

  const items = selected ? selected.items : [];
  const openItems = items.filter((item) => item.status !== 'BOUGHT');
  const boughtItems = items.filter((item) => item.status === 'BOUGHT');

  const strings = {
    titleFallback: 'Shopping list',
    back: 'Back',
    openLabel: 'Open',
    boughtLabel: 'Bought',
    openCountSuffix: 'open',
    boughtCountSuffix: 'bought',
    details: 'Edit details',
    swipeBought: 'Bought',
    swipeOpen: 'Open',
    noOpenItems: 'No open items.',
    noBoughtItems: 'No bought items yet.',
    addPlaceholder: 'Add item… (long-press Add for details)',
    addAction: 'Add ⋯',
    loadingItems: 'Loading items...',
    clearBought: 'Clear bought',
    addQuantityPlaceholder: 'Quantity (optional)',
    addErrorQuantity: 'Quantity must be a positive number.',
    addErrorQuantityUnit: 'Quantity and unit must be set together.',
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
  };

  async function handleAddItem() {
    if (!selected || !newItemName.trim()) {
      return;
    }
    const parsedQuantity = parseQuantity(addQuantity);
    if (Number.isNaN(parsedQuantity)) {
      setAddError(strings.addErrorQuantity);
      return;
    }
    if (parsedQuantity !== null && !addUnit) {
      setAddError(strings.addErrorQuantityUnit);
      return;
    }
    await shopping.addItem(selected.id, newItemName.trim(), parsedQuantity, addUnit);
    setNewItemName('');
    setAddQuantity('');
    setAddUnit('ST');
    setAddError(null);
  }

  function openEdit(item: typeof items[number]) {
    setEditItemId(item.id);
    setEditName(item.name);
    setEditQuantity(item.quantity ? String(item.quantity) : '');
    setEditUnit(item.unit ?? 'ST');
    setEditError(null);
  }

  function closeEdit() {
    setEditItemId(null);
    setEditName('');
    setEditQuantity('');
    setEditUnit('ST');
    setEditError(null);
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
    await shopping.updateItem(
      selected.id,
      editItemId,
      editName.trim(),
      parsedQuantity,
      editUnit
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

  return (
    <AppScreen scroll={false} contentStyle={styles.screenContent}>
      <View style={styles.topBar}>
        <AppButton title={strings.back} onPress={onBack} variant="ghost" />
        <View style={styles.topBarText}>
          <Text style={styles.topTitle}>
            {selected ? selected.name : strings.titleFallback}
          </Text>
        </View>
      </View>

      <ScrollView contentContainerStyle={styles.scrollContent}>
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
              {openItems.map((item) => (
                <Swipeable
                  key={item.id}
                  renderRightActions={() => (
                    <View style={[styles.swipeAction, styles.swipeActionBought]}>
                      <Text style={styles.swipeActionText}>{strings.swipeBought}</Text>
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
                      <View style={styles.checkbox} />
                      <View style={styles.itemContent}>
                        <Text style={styles.itemText}>{formatItemTitle(item)}</Text>
                      </View>
                    </Pressable>
                    <Pressable style={styles.detailZone} onPress={() => openEdit(item)}>
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
                  onPress={handleClearBought}
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
          <AppInput
            placeholder={strings.addPlaceholder}
            value={newItemName}
            onChangeText={setNewItemName}
            style={styles.bottomInput}
          />
          <AppButton
            title={strings.addAction}
            onPress={handleAddItem}
            onLongPress={() => setShowAddDetails((prev) => !prev)}
          />
        </View>

        {showAddDetails ? (
          <View style={styles.addDetailsBar}>
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
              {UNIT_OPTIONS.map((unit) => (
                <AppChip
                  key={unit.value}
                  label={unit.label}
                  active={addUnit === unit.value}
                  onPress={() => {
                    setAddUnit(unit.value);
                    setAddError(null);
                  }}
                />
              ))}
              <AppChip
                label={strings.unitNone}
                active={!addUnit}
                onPress={() => {
                  setAddUnit(null);
                  setAddQuantity('');
                  setAddError(null);
                }}
              />
            </View>
            {addError ? <Text style={styles.error}>{addError}</Text> : null}
          </View>
        ) : null}
      </View>


      {editItemId ? (
        <Pressable style={styles.backdrop} onPress={closeEdit}>
          <Pressable style={styles.sheet} onPress={() => null}>
            <View style={styles.sheetHandle} />
            <Text style={textStyles.h3}>{strings.editTitle}</Text>
            <AppInput
              placeholder={strings.editNamePlaceholder}
              value={editName}
              onChangeText={setEditName}
              autoFocus
            />
            <AppInput
              placeholder={strings.editQuantityPlaceholder}
              value={editQuantity}
              onChangeText={setEditQuantity}
              keyboardType="decimal-pad"
            />
            <View style={styles.unitRow}>
              {UNIT_OPTIONS.map((unit) => (
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
            </View>
            {editError ? <Text style={styles.error}>{editError}</Text> : null}
            <View style={styles.editorActions}>
              <AppButton title={strings.saveChanges} onPress={handleSaveEdit} fullWidth />
              <AppButton title={strings.removeItem} onPress={handleRemoveEdit} variant="ghost" fullWidth />
              <AppButton title={strings.close} onPress={closeEdit} variant="secondary" fullWidth />
            </View>
          </Pressable>
        </Pressable>
      ) : null}
    </AppScreen>
  );
}

const UNIT_OPTIONS: Array<{ label: string; value: ShoppingUnit }> = [
  { label: 'pc', value: 'ST' },
  { label: 'pack', value: 'FORP' },
  { label: 'kg', value: 'KG' },
  { label: 'hg', value: 'HG' },
  { label: 'g', value: 'G' },
  { label: 'l', value: 'L' },
  { label: 'dl', value: 'DL' },
  { label: 'ml', value: 'ML' },
];

const UNIT_LABELS: Record<ShoppingUnit, string> = {
  ST: 'pc',
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
  },
  scrollContent: {
    padding: theme.spacing.lg,
    paddingTop: 110,
    paddingBottom: 220,
    gap: theme.spacing.md,
  },
  topBar: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    paddingTop: theme.spacing.lg,
    paddingBottom: theme.spacing.md,
    paddingHorizontal: theme.spacing.lg,
    backgroundColor: theme.colors.surface,
    borderBottomWidth: 1,
    borderBottomColor: theme.colors.border,
    flexDirection: 'row',
    alignItems: 'center',
    gap: theme.spacing.sm,
    zIndex: 5,
  },
  topBarText: {
    flex: 1,
  },
  topTitle: {
    ...textStyles.h2,
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
    position: 'absolute',
    left: 0,
    right: 0,
    bottom: 0,
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
  bottomInput: {
    flex: 1,
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
});
