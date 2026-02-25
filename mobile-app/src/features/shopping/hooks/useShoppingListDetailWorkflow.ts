import { useEffect, useMemo, useRef, useState } from 'react';
import type { ShoppingListResponse, ShoppingUnit } from '../api/shoppingApi';
import { formatQuantityForFeedback, formatUnitForFeedback, parseQuantity } from '../utils/shoppingQuantity';
import { useShoppingLists } from './useShoppingLists';

type ShoppingListsHook = ReturnType<typeof useShoppingLists>;
type ShoppingItem = ShoppingListResponse['items'][number];

type AddLikeStrings = {
  addErrorQuantity: string;
  addErrorQuantityUnit: string;
  addDetailsAddedSuffix: string;
};

type QuickAddStrings = {
  quickAddAddedSuffix: string;
};

type EditStrings = {
  nameRequired: string;
  quantityInvalid: string;
  quantityUnitMismatch: string;
};

type UseShoppingListDetailWorkflowArgs = {
  shopping: ShoppingListsHook;
  listId: string;
};

type FinishOpenDragArgs = {
  draggedId: string | null;
  startIndex: number | null;
  finalIds: string[];
};

export function useShoppingListDetailWorkflow({ shopping, listId }: UseShoppingListDetailWorkflowArgs) {
  const [newItemName, setNewItemName] = useState('');
  const [quickAddName, setQuickAddName] = useState('');
  const [quickAddFeedback, setQuickAddFeedback] = useState<string | null>(null);
  const [showQuickAdd, setShowQuickAdd] = useState(false);
  const [editItemId, setEditItemId] = useState<string | null>(null);
  const [editName, setEditName] = useState('');
  const [editQuantity, setEditQuantity] = useState('');
  const [editUnit, setEditUnit] = useState<ShoppingUnit | null>('ST');
  const [editError, setEditError] = useState<string | null>(null);
  const [showAddDetails, setShowAddDetails] = useState(false);
  const [addQuantity, setAddQuantity] = useState('');
  const [addUnit, setAddUnit] = useState<ShoppingUnit | null>('ST');
  const [addError, setAddError] = useState<string | null>(null);
  const [addDetailsFeedback, setAddDetailsFeedback] = useState<string | null>(null);
  const [orderedOpenItemIds, setOrderedOpenItemIds] = useState<string[]>([]);
  const [draggingOpenItemId, setDraggingOpenItemId] = useState<string | null>(null);

  const quickAddFeedbackTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const addDetailsFeedbackTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const selected = useMemo(() => {
    return shopping.lists.find((list) => list.id === listId) ?? null;
  }, [shopping.lists, listId]);

  const selectedMeal = useMemo(() => {
    if (!selected || !editItemId) {
      return null;
    }
    return selected.items.find((item) => item.id === editItemId) ?? null;
  }, [selected, editItemId]);

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

  function openEdit(item: ShoppingItem) {
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

  function closeQuickAdd() {
    setQuickAddName('');
    setQuickAddFeedback(null);
    if (quickAddFeedbackTimerRef.current) {
      clearTimeout(quickAddFeedbackTimerRef.current);
      quickAddFeedbackTimerRef.current = null;
    }
    setShowQuickAdd(false);
  }

  function closeAddDetails() {
    setShowAddDetails(false);
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
    if (options?.onRefocus) {
      requestAnimationFrame(() => {
        options.onRefocus?.();
      });
    }
  }

  async function handleQuickAdd(
    strings: QuickAddStrings,
    options?: { onRefocus?: () => void }
  ) {
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
    if (options?.onRefocus) {
      requestAnimationFrame(() => {
        options.onRefocus?.();
      });
    }
  }

  async function handleSaveEdit(strings: EditStrings, options?: { onClose?: () => void }) {
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
    await shopping.updateItem(selected.id, editItemId, editName.trim(), parsedQuantity, effectiveUnit);
    if (options?.onClose) {
      options.onClose();
      return;
    }
    closeEdit();
  }

  async function handleRemoveEdit(options?: { onClose?: () => void }) {
    if (!selected || !editItemId) {
      return;
    }
    await shopping.removeItem(selected.id, editItemId);
    if (options?.onClose) {
      options.onClose();
      return;
    }
    closeEdit();
  }

  async function finishOpenDrag(args: FinishOpenDragArgs) {
    if (!selected) {
      return;
    }
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
    await shopping.reorderItem(selected.id, draggedId, direction, steps);
  }

  return {
    state: {
      newItemName,
      quickAddName,
      quickAddFeedback,
      showQuickAdd,
      editItemId,
      editName,
      editQuantity,
      editUnit,
      editError,
      showAddDetails,
      addQuantity,
      addUnit,
      addError,
      addDetailsFeedback,
      orderedOpenItemIds,
      draggingOpenItemId,
      selectedMeal,
    },
    actions: {
      setNewItemName,
      setQuickAddName,
      setShowQuickAdd,
      setEditName,
      setEditQuantity,
      setEditUnit,
      setEditError,
      setShowAddDetails,
      setAddQuantity,
      setAddUnit,
      setAddError,
      setOrderedOpenItemIds,
      setDraggingOpenItemId,
      setQuickAddFeedback,
      setAddDetailsFeedback,
      openEdit,
      closeEdit,
      closeQuickAdd,
      closeAddDetails,
      handleAddItem,
      handleQuickAdd,
      handleSaveEdit,
      handleRemoveEdit,
      finishOpenDrag,
    },
  };
}
