import { useMemo } from 'react';
import type { TodoResponse } from '../api/todoApi';
import { parseApiDate, toDateKey } from '../utils/todoDates';

export type FrontendTodoScope = 'DAY' | 'WEEK' | 'MONTH' | 'LATER';

export type FrontendScopeState =
  | { scope: 'DAY'; scopeValue: string }
  | { scope: 'WEEK'; scopeValue: { year: number; week: number } }
  | { scope: 'MONTH'; scopeValue: { year: number; month: number } }
  | { scope: 'LATER'; scopeValue: null };

export type ScopedTodoItem = TodoResponse & {
  parsedDueDate: Date | null;
  parsedDueDateKey: string | null;
  frontendScope: FrontendTodoScope;
  frontendScopeState: FrontendScopeState;
};

function deriveScopeStateFromItem(item: TodoResponse): FrontendScopeState {
  if (item.scope === 'DAY' && item.dueDate) {
    return { scope: 'DAY', scopeValue: item.dueDate };
  }
  if (item.scope === 'WEEK' && item.scopeYear && item.scopeWeek) {
    return { scope: 'WEEK', scopeValue: { year: item.scopeYear, week: item.scopeWeek } };
  }
  if (item.scope === 'MONTH' && item.scopeYear && item.scopeMonth) {
    return { scope: 'MONTH', scopeValue: { year: item.scopeYear, month: item.scopeMonth } };
  }
  if (item.scope === 'LATER') {
    return { scope: 'LATER', scopeValue: null };
  }

  // Legacy fallback for rows created before backend scope migration/backfill.
  if (item.dueDate) {
    return { scope: 'DAY', scopeValue: item.dueDate };
  }
  return { scope: 'LATER', scopeValue: null };
}

export function useScopedTodos(items: TodoResponse[]) {
  return useMemo<ScopedTodoItem[]>(() => {
    return items.map((item) => {
      const scopeState = deriveScopeStateFromItem(item);
      const parsedDueDate = parseApiDate(item.dueDate);
      return {
        ...item,
        parsedDueDate,
        parsedDueDateKey: parsedDueDate ? toDateKey(parsedDueDate) : null,
        frontendScope: scopeState.scope,
        frontendScopeState: scopeState,
      };
    });
  }, [items]);
}

