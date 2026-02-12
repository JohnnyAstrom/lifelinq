import { fetchJson } from '../../../shared/api/client';

export type TodoResponse = {
  id: string;
  householdId: string;
  text: string;
  status: 'OPEN' | 'COMPLETED';
};

export async function fetchTodos(token: string, status?: string): Promise<TodoResponse[]> {
  const query = status ? `?status=${encodeURIComponent(status)}` : '';
  return fetchJson<TodoResponse[]>(`/todos${query}`, {}, { token });
}

export async function createTodo(token: string, text: string): Promise<void> {
  await fetchJson<void>(
    '/todos',
    {
      method: 'POST',
      body: JSON.stringify({ text }),
    },
    { token }
  );
}
