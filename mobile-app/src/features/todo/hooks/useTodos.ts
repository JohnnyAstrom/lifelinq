import { useEffect, useState } from 'react';
import { createTodo, fetchTodos, TodoResponse } from '../api/todoApi';

export function useTodos(token: string | null, status: 'OPEN' | 'COMPLETED' | 'ALL' = 'OPEN') {
  const [items, setItems] = useState<TodoResponse[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

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
      setError(err instanceof Error ? err.message : 'Unknown error');
    } finally {
      setLoading(false);
    }
  }

  async function add(text: string) {
    if (!token) {
      throw new Error('Missing token');
    }
    await createTodo(token, text);
    await load();
  }

  useEffect(() => {
    load();
  }, [token, status]);

  return { items, error, loading, reload: load, add };
}
