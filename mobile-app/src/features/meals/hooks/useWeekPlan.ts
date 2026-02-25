import { useEffect, useState } from 'react';
import { useAuth } from '../../../shared/auth/AuthContext';
import { formatApiError } from '../../../shared/api/client';
import {
  addOrReplaceMeal,
  getWeekPlan,
  removeMeal,
  type WeekPlanResponse,
  type AddMealRequest,
} from '../api/mealsApi';

type State = {
  loading: boolean;
  error: string | null;
  data: WeekPlanResponse | null;
};

export function useWeekPlan(
  token: string | null,
  year: number,
  isoWeek: number
) {
  const { handleApiError } = useAuth();
  const [state, setState] = useState<State>({
    loading: true,
    error: null,
    data: null,
  });

  const load = async () => {
    if (!token) {
      setState({ loading: false, error: 'Missing token', data: null });
      return;
    }
    setState((prev) => ({ ...prev, loading: true, error: null }));
    try {
      const data = await getWeekPlan(year, isoWeek, { token });
      setState({ loading: false, error: null, data });
    } catch (err) {
      await handleApiError(err);
      setState({ loading: false, error: formatApiError(err), data: null });
    }
  };

  useEffect(() => {
    load();
  }, [token, year, isoWeek]);

  const addMeal = async (
    dayOfWeek: number,
    mealType: 'BREAKFAST' | 'LUNCH' | 'DINNER',
    payload: AddMealRequest
  ) => {
    if (!token) {
      throw new Error('Missing token');
    }
    setState((prev) => ({ ...prev, loading: true, error: null }));
    try {
      await addOrReplaceMeal(year, isoWeek, dayOfWeek, mealType, payload, {
        token,
      });
      await load();
    } catch (err) {
      await handleApiError(err);
      setState((prev) => ({
        ...prev,
        loading: false,
        error: formatApiError(err),
      }));
      throw err;
    }
  };

  const remove = async (
    dayOfWeek: number,
    mealType: 'BREAKFAST' | 'LUNCH' | 'DINNER'
  ) => {
    if (!token) {
      throw new Error('Missing token');
    }
    setState((prev) => ({ ...prev, loading: true, error: null }));
    try {
      await removeMeal(year, isoWeek, dayOfWeek, mealType, { token });
      await load();
    } catch (err) {
      await handleApiError(err);
      setState((prev) => ({
        ...prev,
        loading: false,
        error: formatApiError(err),
      }));
      throw err;
    }
  };

  return {
    ...state,
    reload: load,
    addMeal,
    removeMeal: remove,
  };
}
