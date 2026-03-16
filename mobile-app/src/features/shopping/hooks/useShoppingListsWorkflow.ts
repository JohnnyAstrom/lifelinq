import { useEffect, useMemo, useState } from 'react';
import { useShoppingLists } from './useShoppingLists';
import type { ShoppingListType } from '../api/shoppingApi';

type ShoppingListsHook = ReturnType<typeof useShoppingLists>;

type UseShoppingListsWorkflowArgs = {
  shopping: ShoppingListsHook;
};

type FinishDragListArgs = {
  draggingId: string | null;
  startIndex: number | null;
  finalIds: string[];
};

export function useShoppingListsWorkflow({ shopping }: UseShoppingListsWorkflowArgs) {
  const [newListName, setNewListName] = useState('');
  const [newListType, setNewListType] = useState<ShoppingListType>('grocery');
  const [showCreate, setShowCreate] = useState(false);
  const [activeListId, setActiveListId] = useState<string | null>(null);
  const [editListId, setEditListId] = useState<string | null>(null);
  const [editListName, setEditListName] = useState('');
  const [editListType, setEditListType] = useState<ShoppingListType>('grocery');
  const [orderedListIds, setOrderedListIds] = useState<string[]>([]);
  const [draggingListId, setDraggingListId] = useState<string | null>(null);

  const orderedLists = useMemo(() => {
    if (orderedListIds.length === 0) {
      return shopping.lists;
    }
    const byId = new Map(shopping.lists.map((list) => [list.id, list]));
    const result = orderedListIds
      .map((id) => byId.get(id))
      .filter((list): list is NonNullable<typeof list> => !!list);
    return result.length === shopping.lists.length ? result : shopping.lists;
  }, [orderedListIds, shopping.lists]);

  const canCreateList = newListName.trim().length > 0;

  function selectedActionList() {
    if (!activeListId) {
      return null;
    }
    return orderedLists.find((list) => list.id === activeListId) ?? null;
  }

  function closeCreate() {
    setShowCreate(false);
  }

  function closeActions() {
    setActiveListId(null);
  }

  function openActionsForList(listId: string) {
    setActiveListId(listId);
  }

  function closeEdit() {
    setEditListId(null);
    setEditListName('');
    setEditListType('grocery');
  }

  function openEdit() {
    const list = selectedActionList();
    if (!list) {
      return;
    }
    setEditListId(list.id);
    setEditListName(list.name);
    setEditListType(list.type);
    closeActions();
  }

  async function handleCreateList() {
    if (!newListName.trim()) {
      return;
    }
    const created = await shopping.createList(newListName.trim(), newListType);
    if (!created) {
      return;
    }
    setNewListName('');
    setNewListType('grocery');
    closeCreate();
  }

  async function handleEditList() {
    if (!editListId || !editListName.trim()) {
      return;
    }
    const updated = await shopping.updateList(editListId, editListName.trim(), editListType);
    if (!updated) {
      return;
    }
    closeEdit();
  }

  async function handleRemoveList(options?: { listId?: string | null }) {
    const targetId = options?.listId ?? selectedActionList()?.id ?? null;
    if (!targetId) {
      return;
    }
    const removed = await shopping.removeList(targetId);
    if (!removed) {
      return;
    }
    if (activeListId === targetId) {
      closeActions();
    }
    if (editListId === targetId) {
      closeEdit();
    }
  }

  const canEditList = editListName.trim().length > 0;

  async function finishDragList(args: FinishDragListArgs) {
    const { draggingId, startIndex, finalIds } = args;
    if (!draggingId || startIndex === null) {
      return;
    }
    const finalIndex = finalIds.indexOf(draggingId);
    if (finalIndex < 0 || finalIndex === startIndex) {
      return;
    }
    const direction = finalIndex > startIndex ? 'DOWN' : 'UP';
    const steps = Math.abs(finalIndex - startIndex);
    await shopping.reorderList(draggingId, direction, steps);
  }

  return {
    state: {
      newListName,
      newListType,
      showCreate,
      activeListId,
      editListId,
      editListName,
      editListType,
      orderedListIds,
      draggingListId,
      orderedLists,
      canCreateList,
      canEditList,
    },
    actions: {
      setNewListName,
      setNewListType,
      setShowCreate,
      setActiveListId,
      setEditListId,
      setEditListName,
      setEditListType,
      setOrderedListIds,
      setDraggingListId,
      handleCreateList,
      handleEditList,
      handleRemoveList,
      openActionsForList,
      closeActions,
      openEdit,
      closeEdit,
      closeCreate,
      finishDragList,
      selectedActionList,
    },
  };
}
