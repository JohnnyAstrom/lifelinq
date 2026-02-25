import { useEffect, useState } from 'react';
import { formatApiError } from '../../../shared/api/client';
import { useAuth } from '../../../shared/auth/AuthContext';
import { completeTodo, createTodo, deleteTodo, fetchTodos, fetchTodosForMonth, TodoResponse, updateTodo } from '../api/todoApi';

type CalendarMonthQuery = {
  enabled: boolean;
  year: number;
  month: number;
};

export function useTodos(
  token: string | null,
  status: 'OPEN' | 'COMPLETED' | 'ALL' = 'OPEN',
  calendarMonthQuery?: CalendarMonthQuery
) {
  const [items, setItems] = useState<TodoResponse[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const { handleApiError } = useAuth();

  async function load() {
    if (!token) {
      setItems([]);
      return;
    }

    setLoading(true);
    setError(null);

    try {
      if (calendarMonthQuery?.enabled) {
        const result = await fetchTodosForMonth(token, calendarMonthQuery.year, calendarMonthQuery.month);
        const filtered = status === 'ALL'
          ? result
          : result.filter((item) => item.status === status);
        setItems(filtered);
      } else {
        const queryStatus = status === 'ALL' ? undefined : status;
        const result = await fetchTodos(token, queryStatus);
        setItems(result);
      }
    } catch (err) {
      await handleApiError(err);
      setError(formatApiError(err));
    } finally {
      setLoading(false);
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
    setError(null);
    try {
      await createTodo(token, text, options);
      await load();
      return true;
    } catch (err) {
      await handleApiError(err);
      setError(formatApiError(err));
      return false;
    }
  }

  async function complete(id: string): Promise<boolean> {
    if (!token) {
      throw new Error('Missing token');
    }
    setError(null);
    try {
      await completeTodo(token, id);
      await load();
      return true;
    } catch (err) {
      await handleApiError(err);
      setError(formatApiError(err));
      return false;
    }
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
    setError(null);
    try {
      await updateTodo(token, id, text, options);
      await load();
      return true;
    } catch (err) {
      await handleApiError(err);
      setError(formatApiError(err));
      return false;
    }
  }

  async function remove(id: string): Promise<boolean> {
    if (!token) {
      throw new Error('Missing token');
    }
    setError(null);
    try {
      await deleteTodo(token, id);
      await load();
      return true;
    } catch (err) {
      await handleApiError(err);
      setError(formatApiError(err));
      return false;
    }
  }

  useEffect(() => {
    load();
  }, [token, status, calendarMonthQuery?.enabled, calendarMonthQuery?.year, calendarMonthQuery?.month]);

  return { items, error, loading, reload: load, add, complete, update, remove };
}
