import { useEffect, useRef, useState } from 'react';
import { useAuth } from '../../../shared/auth/AuthContext';
import { formatApiError } from '../../../shared/api/client';
import {
  addShoppingItem,
  createShoppingList,
  deleteShoppingItem,
  deleteShoppingList,
  listShoppingLists,
  reorderShoppingItem,
  reorderShoppingList,
  toggleShoppingItem,
  updateShoppingList,
  updateShoppingItem,
  type ShoppingUnit,
  type CreateShoppingListResponse,
  type ShoppingListType,
  type AddShoppingItemResponse,
  type ShoppingListResponse,
} from '../api/shoppingApi';

type State = {
  loading: boolean;
  isInitialLoading: boolean;
  isRefreshing: boolean;
  isMutating: boolean;
  error: string | null;
  hasLoaded: boolean;
  pendingMutation: PendingMutation | null;
  lists: ShoppingListResponse[];
};

type PendingMutation =
  | { kind: 'create-list' }
  | { kind: 'remove-list'; listId: string }
  | { kind: 'update-list'; listId: string }
  | { kind: 'reorder-list'; listId: string }
  | { kind: 'add-item'; listId: string }
  | { kind: 'toggle-item'; listId: string; itemId: string }
  | { kind: 'remove-item'; listId: string; itemId: string }
  | { kind: 'update-item'; listId: string; itemId: string }
  | { kind: 'reorder-item'; listId: string; itemId: string };

export function useShoppingLists(token: string | null) {
  const { handleApiError } = useAuth();
  const [state, setState] = useState<State>({
    loading: true,
    isInitialLoading: true,
    isRefreshing: false,
    isMutating: false,
    error: null,
    hasLoaded: false,
    pendingMutation: null,
    lists: [],
  });
  const pendingMutationRef = useRef<PendingMutation | null>(null);

  function setPendingMutation(pendingMutation: PendingMutation | null) {
    pendingMutationRef.current = pendingMutation;
    setState((prev) => ({
      ...prev,
      pendingMutation,
      isMutating: pendingMutation !== null,
    }));
  }

  const load = async (options?: { mode?: 'initial' | 'reload' | 'mutation-followup' | 'silent' }) => {
    const mode = options?.mode ?? 'reload';
    if (!token) {
      pendingMutationRef.current = null;
      setState({
        loading: false,
        isInitialLoading: false,
        isRefreshing: false,
        isMutating: false,
        error: 'Missing token',
        hasLoaded: false,
        pendingMutation: null,
        lists: [],
      });
      return;
    }

    if (mode === 'initial') {
      setState((prev) => ({
        ...prev,
        loading: true,
        isInitialLoading: true,
        isRefreshing: false,
        error: null,
      }));
    } else if (mode === 'reload') {
      setState((prev) => ({
        ...prev,
        loading: true,
        isInitialLoading: false,
        isRefreshing: true,
        error: null,
      }));
    }

    try {
      const lists = await listShoppingLists({ token });
      setState((prev) => ({
        ...prev,
        loading: false,
        isInitialLoading: false,
        isRefreshing: false,
        error: null,
        hasLoaded: true,
        lists,
      }));
    } catch (err) {
      await handleApiError(err);
      setState((prev) => ({
        ...prev,
        loading: false,
        isInitialLoading: false,
        isRefreshing: false,
        error: formatApiError(err),
        lists: prev.hasLoaded ? prev.lists : [],
      }));
    }
  };

  useEffect(() => {
    void load({ mode: 'initial' });
  }, [token]);

  async function runMutation<T>(
    pendingMutation: PendingMutation,
    mutation: () => Promise<T>,
    options?: { reloadMode?: 'mutation-followup' | 'silent'; reloadAfter?: boolean }
  ): Promise<T | null> {
    if (pendingMutationRef.current) {
      return null;
    }

    setPendingMutation(pendingMutation);
    setState((prev) => ({
      ...prev,
      error: null,
    }));

    try {
      const result = await mutation();
      if (options?.reloadAfter !== false) {
        await load({ mode: options?.reloadMode ?? 'mutation-followup' });
      }
      return result;
    } catch (err) {
      await handleApiError(err);
      setState((prev) => ({
        ...prev,
        loading: false,
        isInitialLoading: false,
        isRefreshing: false,
        error: formatApiError(err),
      }));
      return null;
    } finally {
      setPendingMutation(null);
    }
  }

  const createList = async (
    name: string,
    type: ShoppingListType = 'mixed'
  ): Promise<CreateShoppingListResponse | null> => {
    if (!token) {
      throw new Error('Missing token');
    }
    return runMutation(
      { kind: 'create-list' },
      () => createShoppingList({ name, type }, { token })
    );
  };

  const addItem = async (
    listId: string,
    name: string,
    quantity?: number | null,
    unit?: ShoppingUnit | null,
    addAsNew?: boolean
  ): Promise<AddShoppingItemResponse | null> => {
    if (!token) {
      throw new Error('Missing token');
    }
    return runMutation(
      { kind: 'add-item', listId },
      () => addShoppingItem(listId, { name, quantity, unit, addAsNew }, { token })
    );
  };

  const removeList = async (listId: string): Promise<boolean> => {
    if (!token) {
      throw new Error('Missing token');
    }
    const result = await runMutation(
      { kind: 'remove-list', listId },
      () => deleteShoppingList(listId, { token })
    );
    return result !== null;
  };

  const updateList = async (listId: string, name: string, type: ShoppingListType): Promise<boolean> => {
    if (!token) {
      throw new Error('Missing token');
    }
    const result = await runMutation(
      { kind: 'update-list', listId },
      () => updateShoppingList(listId, { name, type }, { token })
    );
    return result !== null;
  };

  const reorderList = async (
    listId: string,
    direction: 'UP' | 'DOWN',
    steps = 1
  ): Promise<boolean> => {
    if (!token) {
      throw new Error('Missing token');
    }
    const result = await runMutation(
      { kind: 'reorder-list', listId },
      async () => {
        const moveCount = Math.max(1, Math.floor(steps));
        for (let index = 0; index < moveCount; index += 1) {
          await reorderShoppingList(listId, { direction }, { token });
        }
      },
      { reloadMode: 'silent' }
    );
    return result !== null;
  };

  const toggleItem = async (listId: string, itemId: string): Promise<boolean> => {
    if (!token) {
      throw new Error('Missing token');
    }
    const result = await runMutation(
      { kind: 'toggle-item', listId, itemId },
      () => toggleShoppingItem(listId, itemId, { token })
    );
    return result !== null;
  };

  const removeItem = async (listId: string, itemId: string): Promise<boolean> => {
    if (!token) {
      throw new Error('Missing token');
    }
    const result = await runMutation(
      { kind: 'remove-item', listId, itemId },
      () => deleteShoppingItem(listId, itemId, { token })
    );
    return result !== null;
  };

  const updateItem = async (
    listId: string,
    itemId: string,
    name: string,
    quantity?: number | null,
    unit?: ShoppingUnit | null
  ): Promise<boolean> => {
    if (!token) {
      throw new Error('Missing token');
    }
    const result = await runMutation(
      { kind: 'update-item', listId, itemId },
      () => updateShoppingItem(listId, itemId, { name, quantity, unit }, { token })
    );
    return result !== null;
  };

  const reorderItem = async (
    listId: string,
    itemId: string,
    direction: 'UP' | 'DOWN',
    steps = 1
  ): Promise<boolean> => {
    if (!token) {
      throw new Error('Missing token');
    }
    const result = await runMutation(
      { kind: 'reorder-item', listId, itemId },
      async () => {
        const moveCount = Math.max(1, Math.floor(steps));
        for (let index = 0; index < moveCount; index += 1) {
          await reorderShoppingItem(listId, itemId, { direction }, { token });
        }
      },
      { reloadMode: 'silent' }
    );
    return result !== null;
  };

  return {
    ...state,
    reload: () => load({ mode: state.hasLoaded ? 'reload' : 'initial' }),
    createList,
    removeList,
    updateList,
    reorderList,
    addItem,
    toggleItem,
    removeItem,
    updateItem,
    reorderItem,
  };
}
