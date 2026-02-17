import { fetchJson } from '../../../shared/api/client';

export type TodoResponse = {
  id: string;
  householdId: string;
  text: string;
  status: 'OPEN' | 'COMPLETED';
  dueDate?: string | null;
  dueTime?: string | null;
};

type ListTodosResponse = {
  todos: TodoResponse[];
};

export async function fetchTodos(token: string, status?: string): Promise<TodoResponse[]> {
  const query = status ? `?status=${encodeURIComponent(status)}` : '';
  const response = await fetchJson<ListTodosResponse>(`/todos${query}`, {}, { token });
  return response.todos;
}

export async function fetchTodosForMonth(
  token: string,
  year: number,
  month: number
): Promise<TodoResponse[]> {
  const response = await fetchJson<ListTodosResponse>(
    `/todos/calendar/${year}/${month}`,
    {},
    { token }
  );
  return response.todos;
}

export async function createTodo(
  token: string,
  text: string,
  dueDate?: string | null,
  dueTime?: string | null
): Promise<void> {
  const payload: { text: string; dueDate?: string | null; dueTime?: string | null } = { text };
  if (dueDate) {
    payload.dueDate = dueDate;
  }
  if (dueTime) {
    payload.dueTime = dueTime;
  }
  await fetchJson<void>(
    '/todos',
    {
      method: 'POST',
      body: JSON.stringify(payload),
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

export async function updateTodo(
  token: string,
  id: string,
  text: string,
  dueDate?: string | null,
  dueTime?: string | null
): Promise<void> {
  const payload: { text: string; dueDate?: string | null; dueTime?: string | null } = { text };
  if (dueDate !== undefined) {
    payload.dueDate = dueDate;
  }
  if (dueTime !== undefined) {
    payload.dueTime = dueTime;
  }
  await fetchJson<void>(
    `/todos/${encodeURIComponent(id)}`,
    {
      method: 'PUT',
      body: JSON.stringify(payload),
    },
    { token }
  );
}
