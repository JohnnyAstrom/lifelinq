import { fetchJson } from '../../../shared/api/client';

export type TodoResponse = {
  id: string;
  householdId: string;
  text: string;
  status: 'OPEN' | 'COMPLETED';
};

type ListTodosResponse = {
  todos: TodoResponse[];
};

export async function fetchTodos(token: string, status?: string): Promise<TodoResponse[]> {
  const query = status ? `?status=${encodeURIComponent(status)}` : '';
  const response = await fetchJson<ListTodosResponse>(`/todos${query}`, {}, { token });
  return response.todos;
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

export async function completeTodo(token: string, id: string): Promise<void> {
  await fetchJson<void>(
    `/todos/${encodeURIComponent(id)}/complete`,
    {
      method: 'POST',
    },
    { token }
  );
}
