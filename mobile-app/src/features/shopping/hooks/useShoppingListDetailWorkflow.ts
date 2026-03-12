import { useEffect, useRef, useState } from 'react';
import type { ShoppingUnit } from '../api/shoppingApi';
import { formatQuantityForFeedback, formatUnitForFeedback, parseQuantity } from '../utils/shoppingQuantity';
import { useShoppingLists } from './useShoppingLists';

type ShoppingListsHook = ReturnType<typeof useShoppingLists>;
type EditableShoppingItem = {
  id: string;
  title: string;
  quantity: number | null;
  unit: ShoppingUnit | null;
};

type AddLikeStrings = {
  addErrorQuantity: string;
  addErrorQuantityUnit: string;
  addDetailsAddedSuffix: string;
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
  const [editItemId, setEditItemId] = useState<string | null>(null);
  const [editName, setEditName] = useState('');
  const [editQuantity, setEditQuantity] = useState('');
  const [editUnit, setEditUnit] = useState<ShoppingUnit | null>('PCS');
  const [editError, setEditError] = useState<string | null>(null);
  const [showAddDetails, setShowAddDetails] = useState(false);
  const [addQuantity, setAddQuantity] = useState('');
  const [addUnit, setAddUnit] = useState<ShoppingUnit | null>(null);
  const [addError, setAddError] = useState<string | null>(null);
  const [addDetailsFeedback, setAddDetailsFeedback] = useState<string | null>(null);
  const [orderedOpenItemIds, setOrderedOpenItemIds] = useState<string[]>([]);
  const [draggingOpenItemId, setDraggingOpenItemId] = useState<string | null>(null);

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
    setEditError(null);
  }

  function closeEdit() {
    setEditItemId(null);
    setEditName('');
    setEditQuantity('');
    setEditUnit('PCS');
    setEditError(null);
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
      handleAddItem,
      handleSaveEdit,
      handleRemoveEdit,
      finishOpenDrag,
    },
  };
}
