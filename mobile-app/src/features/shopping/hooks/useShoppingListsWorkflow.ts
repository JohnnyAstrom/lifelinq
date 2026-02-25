import { useEffect, useMemo, useState } from 'react';
import { useShoppingLists } from './useShoppingLists';

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
  const [showCreate, setShowCreate] = useState(false);
  const [activeListId, setActiveListId] = useState<string | null>(null);
  const [renameListId, setRenameListId] = useState<string | null>(null);
  const [renameListName, setRenameListName] = useState('');
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

  function closeRename() {
    setRenameListId(null);
    setRenameListName('');
  }

  function openRename() {
    const list = selectedActionList();
    if (!list) {
      return;
    }
    setRenameListId(list.id);
    setRenameListName(list.name);
    closeActions();
  }

  async function handleCreateList() {
    if (!newListName.trim()) {
      return;
    }
    await shopping.createList(newListName.trim());
    setNewListName('');
    closeCreate();
  }

  async function handleRenameList() {
    if (!renameListId || !renameListName.trim()) {
      return;
    }
    await shopping.renameList(renameListId, renameListName.trim());
    closeRename();
  }

  async function handleRemoveList(options?: { listId?: string | null }) {
    const targetId = options?.listId ?? selectedActionList()?.id ?? null;
    if (!targetId) {
      return;
    }
    await shopping.removeList(targetId);
    if (activeListId === targetId) {
      closeActions();
    }
    if (renameListId === targetId) {
      closeRename();
    }
  }

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
      showCreate,
      activeListId,
      renameListId,
      renameListName,
      orderedListIds,
      draggingListId,
      orderedLists,
      canCreateList,
    },
    actions: {
      setNewListName,
      setShowCreate,
      setActiveListId,
      setRenameListId,
      setRenameListName,
      setOrderedListIds,
      setDraggingListId,
      handleCreateList,
      handleRenameList,
      handleRemoveList,
      openActionsForList,
      closeActions,
      openRename,
      closeRename,
      closeCreate,
      finishDragList,
      selectedActionList,
    },
  };
}
