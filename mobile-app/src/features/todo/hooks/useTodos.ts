import { useEffect, useRef, useState } from 'react';
import { formatApiError } from '../../../shared/api/client';
import { handleScopedApiError } from '../../../shared/api/handleScopedApiError';
import { useAuth } from '../../../shared/auth/AuthContext';
import { completeTodo, createTodo, deleteTodo, fetchTodos, fetchTodosForMonth, TodoResponse, updateTodo } from '../api/todoApi';

type CalendarMonthQuery = {
  enabled: boolean;
  year: number;
  month: number;
};

type ScopedTodoOptions = {
  onContextInvalidated?: () => void | Promise<void>;
};

type State = {
  items: TodoResponse[];
  error: string | null;
  loading: boolean;
  isInitialLoading: boolean;
  isRefreshing: boolean;
  isMutating: boolean;
  hasLoaded: boolean;
  pendingMutation: PendingMutation | null;
};

type PendingMutation =
  | { kind: 'add' }
  | { kind: 'complete'; id: string }
  | { kind: 'update'; id: string }
  | { kind: 'remove'; id: string };

export function useTodos(
  token: string | null,
  status: 'OPEN' | 'COMPLETED' | 'ALL' = 'OPEN',
  calendarMonthQuery?: CalendarMonthQuery,
  scopedOptions?: ScopedTodoOptions
) {
  const [state, setState] = useState<State>({
    items: [],
    error: null,
    loading: true,
    isInitialLoading: true,
    isRefreshing: false,
    isMutating: false,
    hasLoaded: false,
    pendingMutation: null,
  });
  const pendingMutationRef = useRef<PendingMutation | null>(null);
  const { handleApiError } = useAuth();

  function setPendingMutation(pendingMutation: PendingMutation | null) {
    pendingMutationRef.current = pendingMutation;
    setState((prev) => ({
      ...prev,
      pendingMutation,
      isMutating: pendingMutation !== null,
    }));
  }

  async function load(options?: { mode?: 'initial' | 'refresh' | 'mutation-followup' | 'silent' }) {
    const mode = options?.mode ?? 'refresh';
    if (!token) {
      pendingMutationRef.current = null;
      setState({
        items: [],
        error: null,
        loading: false,
        isInitialLoading: false,
        isRefreshing: false,
        isMutating: false,
        hasLoaded: false,
        pendingMutation: null,
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
    } else if (mode === 'refresh') {
      setState((prev) => ({
        ...prev,
        loading: true,
        isInitialLoading: false,
        isRefreshing: true,
        error: null,
      }));
    }

    try {
      if (calendarMonthQuery?.enabled) {
        const result = await fetchTodosForMonth(token, calendarMonthQuery.year, calendarMonthQuery.month);
        const filtered = status === 'ALL'
          ? result
          : result.filter((item) => item.status === status);
        setState((prev) => ({
          ...prev,
          items: filtered,
          error: null,
          loading: false,
          isInitialLoading: false,
          isRefreshing: false,
          hasLoaded: true,
        }));
      } else {
        const queryStatus = status === 'ALL' ? undefined : status;
        const result = await fetchTodos(token, queryStatus);
        setState((prev) => ({
          ...prev,
          items: result,
          error: null,
          loading: false,
          isInitialLoading: false,
          isRefreshing: false,
          hasLoaded: true,
        }));
      }
    } catch (err) {
      await handleScopedApiError(err, {
        onContextInvalidated: scopedOptions?.onContextInvalidated,
      });
      await handleApiError(err);
      setState((prev) => ({
        ...prev,
        error: formatApiError(err),
        loading: false,
        isInitialLoading: false,
        isRefreshing: false,
        items: prev.hasLoaded ? prev.items : [],
      }));
    }
  }

  async function runMutation(
    pendingMutation: PendingMutation,
    mutation: () => Promise<void>,
    options?: { reloadMode?: 'mutation-followup' | 'silent'; reloadAfter?: boolean }
  ): Promise<boolean> {
    if (pendingMutationRef.current) {
      return false;
    }

    setPendingMutation(pendingMutation);
    setState((prev) => ({
      ...prev,
      error: null,
    }));

    try {
      await mutation();
      if (options?.reloadAfter !== false) {
        await load({ mode: options?.reloadMode ?? 'mutation-followup' });
      }
      return true;
    } catch (err) {
      await handleScopedApiError(err, {
        onContextInvalidated: scopedOptions?.onContextInvalidated,
      });
      await handleApiError(err);
      setState((prev) => ({
        ...prev,
        error: formatApiError(err),
        loading: false,
        isInitialLoading: false,
        isRefreshing: false,
      }));
      return false;
    } finally {
      setPendingMutation(null);
    }
  }

  async function add(
    text: string,
    options?: {
      scope: 'DAY' | 'WEEK' | 'MONTH' | 'LATER';
      dueDate?: string | null;
      dueTime?: string | null;
      scopeYear?: number | null;
      scopeWeek?: number | null;
      scopeMonth?: number | null;
    }
  ): Promise<boolean> {
    if (!token) {
      throw new Error('Missing token');
    }
    return runMutation(
      { kind: 'add' },
      () => createTodo(token, text, options)
    );
  }

  async function complete(id: string): Promise<boolean> {
    if (!token) {
      throw new Error('Missing token');
    }
    return runMutation(
      { kind: 'complete', id },
      () => completeTodo(token, id)
    );
  }

  async function update(
    id: string,
    text: string,
    options?: {
      scope: 'DAY' | 'WEEK' | 'MONTH' | 'LATER';
      dueDate?: string | null;
      dueTime?: string | null;
      scopeYear?: number | null;
      scopeWeek?: number | null;
      scopeMonth?: number | null;
    }
  ): Promise<boolean> {
    if (!token) {
      throw new Error('Missing token');
    }
    return runMutation(
      { kind: 'update', id },
      () => updateTodo(token, id, text, options)
    );
  }

  async function remove(id: string): Promise<boolean> {
    if (!token) {
      throw new Error('Missing token');
    }
    return runMutation(
      { kind: 'remove', id },
      () => deleteTodo(token, id)
    );
  }

  useEffect(() => {
    void load({ mode: 'initial' });
  }, [token, status, calendarMonthQuery?.enabled, calendarMonthQuery?.year, calendarMonthQuery?.month]);

  return {
    ...state,
    reload: () => load({ mode: state.hasLoaded ? 'refresh' : 'initial' }),
    add,
    complete,
    update,
    remove,
  };
}
