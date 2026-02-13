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
  ): Promise<boolean> {
    if (!token) {
      throw new Error('Missing token');
    }
    setError(null);
    try {
      await createTodo(token, text, options?.dueDate, options?.dueTime);
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
    options?: { dueDate?: string | null; dueTime?: string | null }
  ): Promise<boolean> {
    if (!token) {
      throw new Error('Missing token');
    }
    setError(null);
    try {
      await updateTodo(token, id, text, options?.dueDate, options?.dueTime);
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
  }, [token, status]);

  return { items, error, loading, reload: load, add, complete, update };
}
