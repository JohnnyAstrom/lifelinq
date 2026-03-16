import { useEffect, useRef, useState } from 'react';
import type { ShoppingListType, ShoppingUnit } from '../api/shoppingApi';
import type { ShoppingCategoryKey } from '../utils/shoppingCategories';
import { normalizeShoppingItemTitle } from '../utils/shoppingCategoryInference';
import { resolveShoppingCategory, type ShoppingCategoryOrigin } from '../utils/shoppingDomain';
import { formatQuantityForFeedback, formatUnitForFeedback, parseQuantity } from '../utils/shoppingQuantity';
import { useShoppingLists } from './useShoppingLists';
import { useShoppingCategoryPreferences } from './useShoppingCategoryPreferences';

type ShoppingListsHook = ReturnType<typeof useShoppingLists>;
type EditableShoppingItem = {
  id: string;
  title: string;
  quantity: number | null;
  unit: ShoppingUnit | null;
  categoryOverrideKey: ShoppingCategoryKey | null;
  sourceKind: 'unknown' | 'meal-plan';
  sourceLabel: string | null;
};

type AddLikeStrings = {
  addErrorQuantity: string;
  addErrorQuantityUnit: string;
  addDetailsAddedPrefix: string;
  addAlreadyOnListPrefix: string;
  addIncreasedExistingPrefix: string;
  addUpdatedExistingPrefix: string;
};

type EditStrings = {
  nameRequired: string;
  quantityInvalid: string;
  quantityUnitMismatch: string;
};

type UseShoppingListDetailWorkflowArgs = {
  shopping: ShoppingListsHook;
  listId: string;
  listType: ShoppingListType;
};

type FinishOpenDragArgs = {
  draggedId: string | null;
  startIndex: number | null;
  finalIds: string[];
};

type LikelyAddDuplicate = {
  itemId: string;
  title: string;
  quantity: number | null;
  unit: ShoppingUnit | null;
  action: 'keep-existing' | 'update-existing' | 'increase-existing';
};

export function useShoppingListDetailWorkflow({ shopping, listId, listType }: UseShoppingListDetailWorkflowArgs) {
  const categoryPreferences = useShoppingCategoryPreferences();
  const [newItemName, setNewItemName] = useState('');
  const [editItemId, setEditItemId] = useState<string | null>(null);
  const [editName, setEditName] = useState('');
  const [editQuantity, setEditQuantity] = useState('');
  const [editUnit, setEditUnit] = useState<ShoppingUnit | null>('PCS');
  const [editCategoryOverride, setEditCategoryOverride] = useState<ShoppingCategoryKey | null>(null);
  const [editSourceKind, setEditSourceKind] = useState<'unknown' | 'meal-plan'>('unknown');
  const [editSourceLabel, setEditSourceLabel] = useState<string | null>(null);
  const [editShouldRememberCategory, setEditShouldRememberCategory] = useState(false);
  const [editError, setEditError] = useState<string | null>(null);
  const [showAddDetails, setShowAddDetails] = useState(false);
  const [addQuantity, setAddQuantity] = useState('');
  const [addUnit, setAddUnit] = useState<ShoppingUnit | null>(null);
  const [addError, setAddError] = useState<string | null>(null);
  const [addDetailsFeedback, setAddDetailsFeedback] = useState<string | null>(null);
  const [orderedOpenItemIds, setOrderedOpenItemIds] = useState<string[]>([]);
  const [draggingOpenItemId, setDraggingOpenItemId] = useState<string | null>(null);
  const currentList = shopping.lists.find((list) => list.id === listId) ?? null;
  const normalizedNewTitle = normalizeShoppingItemTitle(newItemName.trim());
  const parsedAddQuantity = parseQuantity(addQuantity);
  const normalizedEditTitle = normalizeShoppingItemTitle(editName.trim());
  const editCategoryResolution = normalizedEditTitle.length > 0
    ? resolveShoppingCategory({
      itemId: editItemId,
      explicitOverrideKey: editCategoryOverride,
      listType,
      normalizedTitle: normalizedEditTitle,
      categoryContext: categoryPreferences,
    })
    : null;
  const editCategoryOrigin: ShoppingCategoryOrigin | null = editCategoryResolution?.effectiveCategory.origin ?? null;
  const editHasLearnedCategory = normalizedEditTitle.length > 0
    && categoryPreferences.getMemoryForTitle(listType, normalizedEditTitle) !== null;
  const addLikelyDuplicate = resolveLikelyAddDuplicate({
    list: currentList,
    normalizedTitle: normalizedNewTitle,
    parsedQuantity: Number.isNaN(parsedAddQuantity) ? null : parsedAddQuantity,
    unit: addUnit,
    quantityIsValid: !Number.isNaN(parsedAddQuantity),
  });

  const addDetailsFeedbackTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    return () => {
      if (addDetailsFeedbackTimerRef.current) {
        clearTimeout(addDetailsFeedbackTimerRef.current);
        addDetailsFeedbackTimerRef.current = null;
      }
    };
  }, []);

  function openEdit(item: EditableShoppingItem) {
    setEditItemId(item.id);
    setEditName(item.title);
    setEditQuantity(item.quantity ? String(item.quantity) : '');
    setEditUnit(item.unit ?? 'PCS');
    setEditCategoryOverride(item.categoryOverrideKey);
    setEditSourceKind(item.sourceKind);
    setEditSourceLabel(item.sourceLabel);
    setEditShouldRememberCategory(false);
    setEditError(null);
  }

  function closeEdit() {
    setEditItemId(null);
    setEditName('');
    setEditQuantity('');
    setEditUnit('PCS');
    setEditCategoryOverride(null);
    setEditSourceKind('unknown');
    setEditSourceLabel(null);
    setEditShouldRememberCategory(false);
    setEditError(null);
  }

  function selectEditCategoryOverride(value: ShoppingCategoryKey | null) {
    setEditCategoryOverride(value);
    setEditShouldRememberCategory(value !== null);
  }

  async function handleResetLearnedCategory() {
    const normalizedTitle = normalizeShoppingItemTitle(editName.trim());
    if (!normalizedTitle) {
      return;
    }
    await categoryPreferences.forgetCategory(listType, normalizedTitle);
    setEditShouldRememberCategory(false);
  }

  function closeAddDetails() {
    setShowAddDetails(false);
    setNewItemName('');
    setAddQuantity('');
    setAddUnit(null);
    setAddError(null);
    setAddDetailsFeedback(null);
    if (addDetailsFeedbackTimerRef.current) {
      clearTimeout(addDetailsFeedbackTimerRef.current);
      addDetailsFeedbackTimerRef.current = null;
    }
  }

  function setTimedAddFeedback(value: string) {
    setAddDetailsFeedback(value);
    if (addDetailsFeedbackTimerRef.current) {
      clearTimeout(addDetailsFeedbackTimerRef.current);
    }
    addDetailsFeedbackTimerRef.current = setTimeout(() => {
      setAddDetailsFeedback(null);
      addDetailsFeedbackTimerRef.current = null;
    }, 3200);
  }

  function resetAddDraft() {
    setNewItemName('');
    setAddQuantity('');
    setAddUnit(null);
    setAddError(null);
  }

  async function handleAddItem(
    strings: AddLikeStrings,
    options?: { onRefocus?: () => void; addAsNew?: boolean }
  ) {
    if (!newItemName.trim()) {
      return;
    }
    const addedName = newItemName.trim();
    const parsedQuantity = parsedAddQuantity;
    if (Number.isNaN(parsedQuantity)) {
      setAddError(strings.addErrorQuantity);
      return;
    }
    if (parsedQuantity !== null && !addUnit) {
      setAddError(strings.addErrorQuantityUnit);
      return;
    }
    const effectiveUnit = parsedQuantity === null ? null : addUnit;
    const response = await shopping.addItem(listId, addedName, parsedQuantity, effectiveUnit, options?.addAsNew);
    resetAddDraft();
    const feedbackName = response?.name ?? addedName;
    switch (response?.outcome) {
      case 'REUSED_EXISTING':
        setTimedAddFeedback(`${strings.addAlreadyOnListPrefix} ${feedbackName}`);
        break;
      case 'UPDATED_EXISTING':
        if (response.quantity != null && response.unit) {
          setTimedAddFeedback(
            `${strings.addUpdatedExistingPrefix} ${formatQuantityForFeedback(response.quantity)} ${formatUnitForFeedback(response.unit)} ${feedbackName}`
          );
        } else {
          setTimedAddFeedback(`${strings.addUpdatedExistingPrefix} ${feedbackName}`);
        }
        break;
      case 'INCREASED_EXISTING':
        if (response.quantity != null && response.unit) {
          setTimedAddFeedback(
            `${strings.addIncreasedExistingPrefix} ${formatQuantityForFeedback(response.quantity)} ${formatUnitForFeedback(response.unit)} ${feedbackName}`
          );
        } else {
          setTimedAddFeedback(`${strings.addIncreasedExistingPrefix} ${feedbackName}`);
        }
        break;
      default: {
        const quantityText =
          parsedQuantity !== null && effectiveUnit
            ? `${formatQuantityForFeedback(parsedQuantity)} ${formatUnitForFeedback(effectiveUnit)} `
            : '';
        setTimedAddFeedback(`${strings.addDetailsAddedPrefix} ${quantityText}${addedName}`);
      }
    }
    if (options?.onRefocus) {
      requestAnimationFrame(() => {
        options.onRefocus?.();
      });
    }
  }

  async function handleAddDuplicatePrimaryAction(
    strings: AddLikeStrings,
    options?: { onRefocus?: () => void }
  ) {
    await handleAddItem(strings, options);
  }

  async function handleSaveEdit(strings: EditStrings, options?: { onClose?: () => void }) {
    if (!editItemId) {
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
    const updated = await shopping.updateItem(listId, editItemId, editName.trim(), parsedQuantity, effectiveUnit);
    if (!updated) {
      return;
    }
    const normalizedTitle = normalizeShoppingItemTitle(editName.trim());
    if (editCategoryOverride) {
      if (editShouldRememberCategory) {
        await categoryPreferences.rememberCategory(listType, normalizedTitle, editCategoryOverride);
      }
      categoryPreferences.setCategoryOverride(editItemId, listType, normalizedTitle, editCategoryOverride);
    } else {
      categoryPreferences.clearCategoryOverride(editItemId);
    }
    if (options?.onClose) {
      options.onClose();
      return;
    }
    closeEdit();
  }

  async function handleRemoveEdit(options?: { onClose?: () => void }) {
    if (!editItemId) {
      return;
    }
    const removed = await shopping.removeItem(listId, editItemId);
    if (!removed) {
      return;
    }
    if (options?.onClose) {
      options.onClose();
      return;
    }
    closeEdit();
  }

  async function finishOpenDrag(args: FinishOpenDragArgs) {
    const { draggedId, startIndex, finalIds } = args;
    if (!draggedId || startIndex === null) {
      return;
    }
    const finalIndex = finalIds.indexOf(draggedId);
    if (finalIndex < 0 || finalIndex === startIndex) {
      return;
    }
    const direction = finalIndex > startIndex ? 'DOWN' : 'UP';
    const steps = Math.abs(finalIndex - startIndex);
    await shopping.reorderItem(listId, draggedId, direction, steps);
  }

  return {
    state: {
      newItemName,
      editItemId,
      editName,
      editQuantity,
      editUnit,
      editCategoryOverride,
      editSourceKind,
      editSourceLabel,
      editShouldRememberCategory,
      editEffectiveCategoryKey: editCategoryResolution?.effectiveCategory.key ?? null,
      editCategoryOrigin,
      editHasLearnedCategory,
      editError,
      showAddDetails,
      addQuantity,
      addUnit,
      addError,
      addDetailsFeedback,
      addLikelyDuplicate,
      orderedOpenItemIds,
      draggingOpenItemId,
    },
    actions: {
      setNewItemName,
      setEditName,
      setEditQuantity,
      setEditUnit,
      setEditCategoryOverride: selectEditCategoryOverride,
      setEditError,
      setShowAddDetails,
      setAddQuantity,
      setAddUnit,
      setAddError,
      setOrderedOpenItemIds,
      setDraggingOpenItemId,
      setAddDetailsFeedback,
      openEdit,
      closeEdit,
      closeAddDetails,
      handleResetLearnedCategory,
      handleAddItem,
      handleAddDuplicatePrimaryAction,
      handleSaveEdit,
      handleRemoveEdit,
      finishOpenDrag,
    },
  };
}

function resolveLikelyAddDuplicate(args: {
  list: ShoppingListsHook['lists'][number] | null;
  normalizedTitle: string;
  parsedQuantity: number | null;
  unit: ShoppingUnit | null;
  quantityIsValid: boolean;
}): LikelyAddDuplicate | null {
  const { list, normalizedTitle, parsedQuantity, unit, quantityIsValid } = args;
  if (!list || !normalizedTitle || !quantityIsValid) {
    return null;
  }
  const matchingOpenItems = list.items.filter((item) => (
    item.status !== 'BOUGHT'
    && normalizeShoppingItemTitle(item.name) === normalizedTitle
  ));
  if (matchingOpenItems.length !== 1) {
    return null;
  }

  const candidate = matchingOpenItems[0];
  if (parsedQuantity === null && unit === null) {
    return {
      itemId: candidate.id,
      title: candidate.name,
      quantity: candidate.quantity,
      unit: candidate.unit,
      action: 'keep-existing',
    };
  }

  if (candidate.quantity == null && candidate.unit == null) {
    return {
      itemId: candidate.id,
      title: candidate.name,
      quantity: candidate.quantity,
      unit: candidate.unit,
      action: 'update-existing',
    };
  }

  if (
    parsedQuantity !== null
    && unit
    && candidate.quantity != null
    && candidate.unit != null
    && candidate.unit === unit
  ) {
    return {
      itemId: candidate.id,
      title: candidate.name,
      quantity: candidate.quantity,
      unit: candidate.unit,
      action: 'increase-existing',
    };
  }

  return null;
}
