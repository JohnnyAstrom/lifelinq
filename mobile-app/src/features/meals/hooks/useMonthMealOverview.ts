import { useEffect, useMemo, useState } from 'react';
import { formatApiError } from '../../../shared/api/client';
import { useAuth } from '../../../shared/auth/AuthContext';
import { getWeekPlan, type WeekPlanResponse } from '../api/mealsApi';
import { buildMonthGridCells, getIsoWeekParts, getWeekStartDate, toDateKey } from '../utils/mealDates';

type WeekKey = {
  key: string;
  year: number;
  isoWeek: number;
};

type State = {
  isInitialLoading: boolean;
  isRefreshing: boolean;
  hasLoaded: boolean;
  error: string | null;
  plansByWeekKey: Record<string, WeekPlanResponse>;
};

type Params = {
  token: string | null;
  anchorDate: Date;
  enabled: boolean;
  currentWeekPlan: WeekPlanResponse | null;
};

function toWeekKey(year: number, isoWeek: number) {
  return `${year}-${isoWeek}`;
}

function getMonthWeekKeys(anchorDate: Date): WeekKey[] {
  const seen = new Set<string>();
  const weeks: WeekKey[] = [];
  for (const cell of buildMonthGridCells(anchorDate)) {
    const { year, isoWeek } = getIsoWeekParts(cell.date);
    const key = toWeekKey(year, isoWeek);
    if (seen.has(key)) {
      continue;
    }
    seen.add(key);
    weeks.push({ key, year, isoWeek });
  }
  return weeks;
}

export function useMonthMealOverview({
  token,
  anchorDate,
  enabled,
  currentWeekPlan,
}: Params) {
  const { handleApiError } = useAuth();
  const [state, setState] = useState<State>({
    isInitialLoading: false,
    isRefreshing: false,
    hasLoaded: false,
    error: null,
    plansByWeekKey: {},
  });

  const monthWeekKeys = useMemo(() => getMonthWeekKeys(anchorDate), [anchorDate]);
  const visibleDateKeys = useMemo(() => {
    const keys = new Set<string>();
    for (const cell of buildMonthGridCells(anchorDate)) {
      keys.add(toDateKey(cell.date));
    }
    return keys;
  }, [anchorDate]);

  async function load(mode: 'initial' | 'reload') {
    if (!enabled) {
      return;
    }
    if (!token) {
      setState({
        isInitialLoading: false,
        isRefreshing: false,
        hasLoaded: false,
        error: 'Missing token',
        plansByWeekKey: {},
      });
      return;
    }

    if (mode === 'initial') {
      setState((prev) => ({
        ...prev,
        isInitialLoading: true,
        isRefreshing: false,
        error: null,
      }));
    } else {
      setState((prev) => ({
        ...prev,
        isInitialLoading: false,
        isRefreshing: true,
        error: null,
      }));
    }

    try {
      const plans = await Promise.all(
        monthWeekKeys.map(async ({ key, year, isoWeek }) => ({
          key,
          plan: await getWeekPlan(year, isoWeek, { token }),
        }))
      );
      const plansByWeekKey: Record<string, WeekPlanResponse> = {};
      for (const { key, plan } of plans) {
        plansByWeekKey[key] = plan;
      }
      setState({
        isInitialLoading: false,
        isRefreshing: false,
        hasLoaded: true,
        error: null,
        plansByWeekKey,
      });
    } catch (err) {
      await handleApiError(err);
      setState((prev) => ({
        ...prev,
        isInitialLoading: false,
        isRefreshing: false,
        error: formatApiError(err),
      }));
    }
  }

  useEffect(() => {
    if (!enabled) {
      return;
    }
    void load(state.hasLoaded ? 'reload' : 'initial');
  }, [enabled, token, anchorDate]);

  const mealCountByDateKey = useMemo(() => {
    const counts: Record<string, number> = {};
    const mergedPlansByWeekKey = { ...state.plansByWeekKey };
    if (currentWeekPlan) {
      mergedPlansByWeekKey[toWeekKey(currentWeekPlan.year, currentWeekPlan.isoWeek)] = currentWeekPlan;
    }

    for (const key of Object.keys(mergedPlansByWeekKey)) {
      const plan = mergedPlansByWeekKey[key];
      const weekStart = getWeekStartDate(plan.year, plan.isoWeek);
      for (const meal of plan.meals) {
        const date = new Date(weekStart.getTime());
        date.setUTCDate(weekStart.getUTCDate() + meal.dayOfWeek - 1);
        const localDate = new Date(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate());
        const dateKey = toDateKey(localDate);
        if (!visibleDateKeys.has(dateKey)) {
          continue;
        }
        counts[dateKey] = (counts[dateKey] ?? 0) + 1;
      }
    }

    return counts;
  }, [currentWeekPlan, state.plansByWeekKey, visibleDateKeys]);

  return {
    isInitialLoading: state.isInitialLoading,
    isRefreshing: state.isRefreshing,
    hasLoaded: state.hasLoaded,
    error: state.error,
    mealCountByDateKey,
    reload: () => load(state.hasLoaded ? 'reload' : 'initial'),
  };
}
