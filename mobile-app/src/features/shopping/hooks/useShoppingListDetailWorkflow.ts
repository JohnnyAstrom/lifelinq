import { useEffect, useRef, useState } from 'react';
import type { ShoppingListType, ShoppingUnit } from '../api/shoppingApi';
import type { ShoppingCategoryKey } from '../utils/shoppingCategories';
import { normalizeShoppingItemTitle } from '../utils/shoppingCategoryInference';
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
};

type AddLikeStrings = {
  addErrorQuantity: string;
  addErrorQuantityUnit: string;
  addDetailsAddedPrefix: string;
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

export function useShoppingListDetailWorkflow({ shopping, listId, listType }: UseShoppingListDetailWorkflowArgs) {
  const categoryPreferences = useShoppingCategoryPreferences();
  const [newItemName, setNewItemName] = useState('');
  const [editItemId, setEditItemId] = useState<string | null>(null);
  const [editName, setEditName] = useState('');
  const [editQuantity, setEditQuantity] = useState('');
  const [editUnit, setEditUnit] = useState<ShoppingUnit | null>('PCS');
  const [editCategoryOverride, setEditCategoryOverride] = useState<ShoppingCategoryKey | null>(null);
  const [editShouldRememberCategory, setEditShouldRememberCategory] = useState(false);
  const [editError, setEditError] = useState<string | null>(null);
  const [showAddDetails, setShowAddDetails] = useState(false);
  const [addQuantity, setAddQuantity] = useState('');
  const [addUnit, setAddUnit] = useState<ShoppingUnit | null>(null);
  const [addError, setAddError] = useState<string | null>(null);
  const [addDetailsFeedback, setAddDetailsFeedback] = useState<string | null>(null);
  const [orderedOpenItemIds, setOrderedOpenItemIds] = useState<string[]>([]);
  const [draggingOpenItemId, setDraggingOpenItemId] = useState<string | null>(null);
  const normalizedEditTitle = normalizeShoppingItemTitle(editName.trim());
  const editHasLearnedCategory = normalizedEditTitle.length > 0
    && categoryPreferences.getMemoryForTitle(listType, normalizedEditTitle) !== null;

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
    setEditShouldRememberCategory(false);
    setEditError(null);
  }

  function closeEdit() {
    setEditItemId(null);
    setEditName('');
    setEditQuantity('');
    setEditUnit('PCS');
    setEditCategoryOverride(null);
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

  async function handleAddItem(
    strings: AddLikeStrings,
    options?: { onRefocus?: () => void }
  ) {
    if (!newItemName.trim()) {
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
    await shopping.addItem(listId, addedName, parsedQuantity, effectiveUnit);
    setNewItemName('');
    setAddQuantity('');
    setAddUnit(null);
    setAddError(null);
    const quantityText =
      parsedQuantity !== null && effectiveUnit
        ? `${formatQuantityForFeedback(parsedQuantity)} ${formatUnitForFeedback(effectiveUnit)} `
        : '';
    setAddDetailsFeedback(`${strings.addDetailsAddedPrefix} ${quantityText}${addedName}`);
    if (addDetailsFeedbackTimerRef.current) {
      clearTimeout(addDetailsFeedbackTimerRef.current);
    }
    addDetailsFeedbackTimerRef.current = setTimeout(() => {
      setAddDetailsFeedback(null);
      addDetailsFeedbackTimerRef.current = null;
    }, 3200);
    if (options?.onRefocus) {
      requestAnimationFrame(() => {
        options.onRefocus?.();
      });
    }
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
    await shopping.updateItem(listId, editItemId, editName.trim(), parsedQuantity, effectiveUnit);
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
    await shopping.removeItem(listId, editItemId);
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
      editShouldRememberCategory,
      editHasLearnedCategory,
      editError,
      showAddDetails,
      addQuantity,
      addUnit,
      addError,
      addDetailsFeedback,
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
      handleSaveEdit,
      handleRemoveEdit,
      finishOpenDrag,
    },
  };
}
