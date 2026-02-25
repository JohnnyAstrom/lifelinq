import { useMemo } from 'react';

type Progress = {
  done: number;
  total: number;
  ratio: number;
};

type WeeklyDayRow = {
  open: number;
  done: number;
};

type GroupedData = {
  daily: {
    openDayItems: Array<unknown>;
    doneDayItems: Array<unknown>;
  };
  weekly: {
    dayRows: WeeklyDayRow[];
    weekGoalsOpen: Array<unknown>;
    weekGoalsDone: Array<unknown>;
  };
};

export function useTodoProgress(grouped: GroupedData) {
  return useMemo(() => {
    const dailyDone = grouped.daily.doneDayItems.length;
    const dailyTotal = grouped.daily.openDayItems.length + dailyDone;

    const weeklyDayOpenCount = grouped.weekly.dayRows.reduce((sum, row) => sum + row.open, 0);
    const weeklyDayDoneCount = grouped.weekly.dayRows.reduce((sum, row) => sum + row.done, 0);
    const weeklyDone = grouped.weekly.weekGoalsDone.length + weeklyDayDoneCount;
    const weeklyTotal =
      grouped.weekly.weekGoalsOpen.length
      + grouped.weekly.weekGoalsDone.length
      + weeklyDayOpenCount
      + weeklyDayDoneCount;

    const asProgress = (done: number, total: number): Progress => ({
      done,
      total,
      ratio: total > 0 ? done / total : 0,
    });

    return {
      daily: asProgress(dailyDone, dailyTotal),
      weekly: asProgress(weeklyDone, weeklyTotal),
    };
  }, [grouped]);
}

