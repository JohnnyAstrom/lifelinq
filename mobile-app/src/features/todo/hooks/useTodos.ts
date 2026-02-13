import { useEffect, useState } from 'react';
import { formatApiError } from '../../../shared/api/client';
import { useAuth } from '../../../shared/auth/AuthContext';
import { completeTodo, createTodo, fetchTodos, TodoResponse, updateTodo } from '../api/todoApi';

export function useTodos(token: string | null, status: 'OPEN' | 'COMPLETED' | 'ALL' = 'OPEN') {
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
      const queryStatus = status === 'ALL' ? undefined : status;
      const result = await fetchTodos(token, queryStatus);
      setItems(result);
    } catch (err) {
      await handleApiError(err);
      setError(formatApiError(err));
    } finally {
      setLoading(false);
    }
  }

  async function add(
    text: string,
    options?: { dueDate?: string | null; dueTime?: string | null }
  ) {
    if (!token) {
      throw new Error('Missing token');
    }
    setError(null);
    try {
      await createTodo(token, text, options?.dueDate, options?.dueTime);
      await load();
    } catch (err) {
      await handleApiError(err);
      setError(formatApiError(err));
    }
  }

  async function complete(id: string) {
    if (!token) {
      throw new Error('Missing token');
    }
    setError(null);
    try {
      await completeTodo(token, id);
      await load();
    } catch (err) {
      await handleApiError(err);
      setError(formatApiError(err));
    }
  }

  async function update(
    id: string,
    text: string,
    options?: { dueDate?: string | null; dueTime?: string | null }
  ) {
    if (!token) {
      throw new Error('Missing token');
    }
    setError(null);
    try {
      await updateTodo(token, id, text, options?.dueDate, options?.dueTime);
      await load();
    } catch (err) {
      await handleApiError(err);
      setError(formatApiError(err));
    }
  }

  useEffect(() => {
    load();
  }, [token, status]);

  return { items, error, loading, reload: load, add, complete, update };
}
