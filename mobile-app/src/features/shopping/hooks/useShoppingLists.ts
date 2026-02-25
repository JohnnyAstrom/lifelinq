import { useEffect, useState } from 'react';
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
  type AddShoppingItemResponse,
  type ShoppingListResponse,
} from '../api/shoppingApi';

type State = {
  loading: boolean;
  error: string | null;
  lists: ShoppingListResponse[];
};

export function useShoppingLists(token: string | null) {
  const { handleApiError } = useAuth();
  const [state, setState] = useState<State>({
    loading: true,
    error: null,
    lists: [],
  });

  const load = async (options?: { silent?: boolean }) => {
    const silent = options?.silent ?? false;
    if (!token) {
      setState({ loading: false, error: 'Missing token', lists: [] });
      return;
    }
    if (!silent) {
      setState((prev) => ({ ...prev, loading: true, error: null }));
    }
    try {
      const lists = await listShoppingLists({ token });
      if (silent) {
        setState((prev) => ({ ...prev, error: null, lists }));
      } else {
        setState({ loading: false, error: null, lists });
      }
    } catch (err) {
      await handleApiError(err);
      setState({ loading: false, error: formatApiError(err), lists: [] });
    }
  };

  useEffect(() => {
    load();
  }, [token]);

  const createList = async (name: string) => {
    if (!token) {
      throw new Error('Missing token');
    }
    setState((prev) => ({ ...prev, loading: true, error: null }));
    try {
      await createShoppingList({ name }, { token });
      await load();
    } catch (err) {
      await handleApiError(err);
      setState((prev) => ({
        ...prev,
        loading: false,
        error: formatApiError(err),
      }));
    }
  };

  const addItem = async (
    listId: string,
    name: string,
    quantity?: number | null,
    unit?: ShoppingUnit | null
  ): Promise<AddShoppingItemResponse | void> => {
    if (!token) {
      throw new Error('Missing token');
    }
    setState((prev) => ({ ...prev, loading: true, error: null }));
    try {
      const response = await addShoppingItem(listId, { name, quantity, unit }, { token });
      await load();
      return response;
    } catch (err) {
      await handleApiError(err);
      setState((prev) => ({
        ...prev,
        loading: false,
        error: formatApiError(err),
      }));
    }
  };

  const removeList = async (listId: string) => {
    if (!token) {
      throw new Error('Missing token');
    }
    setState((prev) => ({ ...prev, loading: true, error: null }));
    try {
      await deleteShoppingList(listId, { token });
      await load();
    } catch (err) {
      await handleApiError(err);
      setState((prev) => ({
        ...prev,
        loading: false,
        error: formatApiError(err),
      }));
    }
  };

  const renameList = async (listId: string, name: string) => {
    if (!token) {
      throw new Error('Missing token');
    }
    setState((prev) => ({ ...prev, loading: true, error: null }));
    try {
      await updateShoppingList(listId, { name }, { token });
      await load();
    } catch (err) {
      await handleApiError(err);
      setState((prev) => ({
        ...prev,
        loading: false,
        error: formatApiError(err),
      }));
    }
  };

  const reorderList = async (
    listId: string,
    direction: 'UP' | 'DOWN',
    steps = 1
  ) => {
    if (!token) {
      throw new Error('Missing token');
    }
    try {
      const moveCount = Math.max(1, Math.floor(steps));
      for (let index = 0; index < moveCount; index += 1) {
        await reorderShoppingList(listId, { direction }, { token });
      }
      await load({ silent: true });
    } catch (err) {
      await handleApiError(err);
      setState((prev) => ({
        ...prev,
        loading: false,
        error: formatApiError(err),
      }));
    }
  };

  const toggleItem = async (listId: string, itemId: string) => {
    if (!token) {
      throw new Error('Missing token');
    }
    setState((prev) => ({ ...prev, loading: true, error: null }));
    try {
      await toggleShoppingItem(listId, itemId, { token });
      await load();
    } catch (err) {
      await handleApiError(err);
      setState((prev) => ({
        ...prev,
        loading: false,
        error: formatApiError(err),
      }));
    }
  };

  const removeItem = async (listId: string, itemId: string) => {
    if (!token) {
      throw new Error('Missing token');
    }
    setState((prev) => ({ ...prev, loading: true, error: null }));
    try {
      await deleteShoppingItem(listId, itemId, { token });
      await load();
    } catch (err) {
      await handleApiError(err);
      setState((prev) => ({
        ...prev,
        loading: false,
        error: formatApiError(err),
      }));
    }
  };

  const updateItem = async (
    listId: string,
    itemId: string,
    name: string,
    quantity?: number | null,
    unit?: ShoppingUnit | null
  ) => {
    if (!token) {
      throw new Error('Missing token');
    }
    setState((prev) => ({ ...prev, loading: true, error: null }));
    try {
      await updateShoppingItem(listId, itemId, { name, quantity, unit }, { token });
      await load();
    } catch (err) {
      await handleApiError(err);
      setState((prev) => ({
        ...prev,
        loading: false,
        error: formatApiError(err),
      }));
    }
  };

  const reorderItem = async (
    listId: string,
    itemId: string,
    direction: 'UP' | 'DOWN',
    steps = 1
  ) => {
    if (!token) {
      throw new Error('Missing token');
    }
    try {
      const moveCount = Math.max(1, Math.floor(steps));
      for (let index = 0; index < moveCount; index += 1) {
        await reorderShoppingItem(listId, itemId, { direction }, { token });
      }
      await load({ silent: true });
    } catch (err) {
      await handleApiError(err);
      setState((prev) => ({
        ...prev,
        loading: false,
        error: formatApiError(err),
      }));
    }
  };

  return {
    ...state,
    reload: load,
    createList,
    removeList,
    renameList,
    reorderList,
    addItem,
    toggleItem,
    removeItem,
    updateItem,
    reorderItem,
  };
}
