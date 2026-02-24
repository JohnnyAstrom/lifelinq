import { fetchJson } from '../../../shared/api/client';

export type TodoResponse = {
  id: string;
  householdId: string;
  text: string;
  status: 'OPEN' | 'COMPLETED';
  scope?: 'DAY' | 'WEEK' | 'MONTH' | 'LATER' | null;
  scopeYear?: number | null;
  scopeWeek?: number | null;
  scopeMonth?: number | null;
  dueDate?: string | null;
  dueTime?: string | null;
  createdAt?: string | null;
  completedAt?: string | null;
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
  scheduling?: {
    scope: 'DAY' | 'WEEK' | 'MONTH' | 'LATER';
    dueDate?: string | null;
    dueTime?: string | null;
    scopeYear?: number | null;
    scopeWeek?: number | null;
    scopeMonth?: number | null;
  }
): Promise<void> {
  const payload: {
    text: string;
    scope?: 'DAY' | 'WEEK' | 'MONTH' | 'LATER';
    dueDate?: string | null;
    dueTime?: string | null;
    scopeYear?: number | null;
    scopeWeek?: number | null;
    scopeMonth?: number | null;
  } = { text };
  if (scheduling) {
    payload.scope = scheduling.scope;
    if (scheduling.dueDate !== undefined) {
      payload.dueDate = scheduling.dueDate;
    }
    if (scheduling.dueTime !== undefined) {
      payload.dueTime = scheduling.dueTime;
    }
    if (scheduling.scopeYear !== undefined) {
      payload.scopeYear = scheduling.scopeYear;
    }
    if (scheduling.scopeWeek !== undefined) {
      payload.scopeWeek = scheduling.scopeWeek;
    }
    if (scheduling.scopeMonth !== undefined) {
      payload.scopeMonth = scheduling.scopeMonth;
    }
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
  scheduling?: {
    scope: 'DAY' | 'WEEK' | 'MONTH' | 'LATER';
    dueDate?: string | null;
    dueTime?: string | null;
    scopeYear?: number | null;
    scopeWeek?: number | null;
    scopeMonth?: number | null;
  }
): Promise<void> {
  const payload: {
    text: string;
    scope?: 'DAY' | 'WEEK' | 'MONTH' | 'LATER';
    dueDate?: string | null;
    dueTime?: string | null;
    scopeYear?: number | null;
    scopeWeek?: number | null;
    scopeMonth?: number | null;
  } = { text };
  if (scheduling) {
    payload.scope = scheduling.scope;
    if (scheduling.dueDate !== undefined) {
      payload.dueDate = scheduling.dueDate;
    }
    if (scheduling.dueTime !== undefined) {
      payload.dueTime = scheduling.dueTime;
    }
    if (scheduling.scopeYear !== undefined) {
      payload.scopeYear = scheduling.scopeYear;
    }
    if (scheduling.scopeWeek !== undefined) {
      payload.scopeWeek = scheduling.scopeWeek;
    }
    if (scheduling.scopeMonth !== undefined) {
      payload.scopeMonth = scheduling.scopeMonth;
    }
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
