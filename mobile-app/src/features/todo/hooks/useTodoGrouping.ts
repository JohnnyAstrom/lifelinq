import { useMemo } from 'react';
import type { ScopedTodoItem } from './useScopedTodos';
import { getStartOfWeekMonday, isSameDay, toDateKey } from '../utils/todoDates';

type WeeklyDayRow = {
  key: string;
  date: Date;
  open: number;
  done: number;
};

type MonthGridCell = {
  date: Date;
  isCurrentMonth: boolean;
};

type Input = {
  normalizedTodos: ScopedTodoItem[];
  selectedDailyDate: Date;
  selectedWeeklyStart: Date;
  calendarMonth: Date;
};

export function useTodoGrouping({
  normalizedTodos,
  selectedDailyDate,
  selectedWeeklyStart,
  calendarMonth,
}: Input) {
  const weeklyDays = useMemo(() => {
    return Array.from({ length: 7 }).map((_, index) => {
      const date = new Date(selectedWeeklyStart.getTime());
      date.setDate(date.getDate() + index);
      date.setHours(0, 0, 0, 0);
      return date;
    });
  }, [selectedWeeklyStart]);

  const monthGridCells = useMemo<MonthGridCell[]>(() => {
    const monthStartDate = new Date(calendarMonth.getFullYear(), calendarMonth.getMonth(), 1);
    monthStartDate.setHours(0, 0, 0, 0);
    const monthGridStartDate = getStartOfWeekMonday(monthStartDate);
    return Array.from({ length: 42 }).map((_, index) => {
      const date = new Date(monthGridStartDate.getTime());
      date.setDate(date.getDate() + index);
      date.setHours(0, 0, 0, 0);
      const isCurrentMonth =
        date.getMonth() === calendarMonth.getMonth() && date.getFullYear() === calendarMonth.getFullYear();
      return { date, isCurrentMonth };
    });
  }, [calendarMonth]);

  const unplanned = useMemo(() => {
    return normalizedTodos.filter((item) => item.status === 'OPEN' && item.frontendScope === 'LATER');
  }, [normalizedTodos]);

  const daily = useMemo(() => {
    const openDayItems = normalizedTodos.filter((item) =>
      item.status === 'OPEN'
      && item.frontendScope === 'DAY'
      && !!item.parsedDueDate
      && isSameDay(item.parsedDueDate, selectedDailyDate));

    const doneDayItems = normalizedTodos.filter((item) =>
      item.status === 'COMPLETED'
      && item.frontendScope === 'DAY'
      && !!item.parsedDueDate
      && isSameDay(item.parsedDueDate, selectedDailyDate));

    return {
      openDayItems,
      doneDayItems,
      laterItems: unplanned,
    };
  }, [normalizedTodos, selectedDailyDate, unplanned]);

  const weekly = useMemo(() => {
    const weeklyDayKeys = new Set(weeklyDays.map((day) => toDateKey(day)));

    const weekGoalsOpen = normalizedTodos.filter((item) => item.status === 'OPEN' && item.frontendScope === 'WEEK');
    const weekGoalsDone = normalizedTodos.filter((item) => item.status === 'COMPLETED' && item.frontendScope === 'WEEK');

    const weeklyDayOpen = normalizedTodos.filter((item) =>
      item.status === 'OPEN'
      && item.frontendScope === 'DAY'
      && !!item.parsedDueDate
      && weeklyDayKeys.has(item.parsedDueDateKey ?? ''));

    const weeklyDayDone = normalizedTodos.filter((item) =>
      item.status === 'COMPLETED'
      && item.frontendScope === 'DAY'
      && !!item.parsedDueDate
      && weeklyDayKeys.has(item.parsedDueDateKey ?? ''));

    const summaryByKey = weeklyDays.reduce<Record<string, { open: number; done: number }>>((acc, day) => {
      acc[toDateKey(day)] = { open: 0, done: 0 };
      return acc;
    }, {});

    for (const item of weeklyDayOpen) {
      if (item.parsedDueDateKey && summaryByKey[item.parsedDueDateKey]) {
        summaryByKey[item.parsedDueDateKey].open += 1;
      }
    }
    for (const item of weeklyDayDone) {
      if (item.parsedDueDateKey && summaryByKey[item.parsedDueDateKey]) {
        summaryByKey[item.parsedDueDateKey].done += 1;
      }
    }

    const dayRows: WeeklyDayRow[] = weeklyDays.map((date) => {
      const key = toDateKey(date);
      const summary = summaryByKey[key] ?? { open: 0, done: 0 };
      return { key, date, open: summary.open, done: summary.done };
    });

    return {
      dayRows,
      weekGoalsOpen,
      weekGoalsDone,
    };
  }, [normalizedTodos, weeklyDays]);

  const monthly = useMemo(() => {
    const monthGoalsOpen = normalizedTodos.filter((item) => item.status === 'OPEN' && item.frontendScope === 'MONTH');
    const monthGoalsDone = normalizedTodos.filter((item) => item.status === 'COMPLETED' && item.frontendScope === 'MONTH');

    const dayCountMap = normalizedTodos.reduce<Record<string, number>>((acc, item) => {
      if (item.frontendScope !== 'DAY' || !item.parsedDueDateKey) {
        return acc;
      }
      acc[item.parsedDueDateKey] = (acc[item.parsedDueDateKey] ?? 0) + 1;
      return acc;
    }, {});

    return {
      gridCells: monthGridCells,
      monthGoalsOpen,
      monthGoalsDone,
      dayCountMap,
    };
  }, [normalizedTodos, monthGridCells]);

  return {
    daily,
    weekly,
    monthly,
    unplanned,
  };
}

