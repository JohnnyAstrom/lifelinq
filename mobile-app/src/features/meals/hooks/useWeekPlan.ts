import { useEffect, useRef, useState } from 'react';
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
  isInitialLoading: boolean;
  isRefreshing: boolean;
  isMutating: boolean;
  error: string | null;
  hasLoaded: boolean;
  pendingMutation: PendingMutation | null;
  data: WeekPlanResponse | null;
};

type PendingMutation =
  | { kind: 'save-meal'; dayOfWeek: number; mealType: 'BREAKFAST' | 'LUNCH' | 'DINNER' }
  | { kind: 'remove-meal'; dayOfWeek: number; mealType: 'BREAKFAST' | 'LUNCH' | 'DINNER' };

export function useWeekPlan(
  token: string | null,
  year: number,
  isoWeek: number
) {
  const { handleApiError } = useAuth();
  const [state, setState] = useState<State>({
    loading: true,
    isInitialLoading: true,
    isRefreshing: false,
    isMutating: false,
    error: null,
    hasLoaded: false,
    pendingMutation: null,
    data: null,
  });
  const pendingMutationRef = useRef<PendingMutation | null>(null);

  function setPendingMutation(pendingMutation: PendingMutation | null) {
    pendingMutationRef.current = pendingMutation;
    setState((prev) => ({
      ...prev,
      pendingMutation,
      isMutating: pendingMutation !== null,
    }));
  }

  const load = async (options?: { mode?: 'initial' | 'reload' | 'mutation-followup' | 'silent' }) => {
    const mode = options?.mode ?? 'reload';
    if (!token) {
      pendingMutationRef.current = null;
      setState({
        loading: false,
        isInitialLoading: false,
        isRefreshing: false,
        isMutating: false,
        error: 'Missing token',
        hasLoaded: false,
        pendingMutation: null,
        data: null,
      });
      return;
    }

    if (mode === 'initial') {
      setState((prev) => ({
        ...prev,
        loading: true,
        isInitialLoading: true,
        isRefreshing: false,
        error: null,
      }));
    } else if (mode === 'reload') {
      setState((prev) => ({
        ...prev,
        loading: true,
        isInitialLoading: false,
        isRefreshing: true,
        error: null,
      }));
    }

    try {
      const data = await getWeekPlan(year, isoWeek, { token });
      setState((prev) => ({
        ...prev,
        loading: false,
        isInitialLoading: false,
        isRefreshing: false,
        error: null,
        hasLoaded: true,
        data,
      }));
    } catch (err) {
      await handleApiError(err);
      setState((prev) => ({
        ...prev,
        loading: false,
        isInitialLoading: false,
        isRefreshing: false,
        error: formatApiError(err),
        data: prev.hasLoaded ? prev.data : null,
      }));
    }
  };

  useEffect(() => {
    void load({ mode: 'initial' });
  }, [token, year, isoWeek]);

  const runMutation = async (
    pendingMutation: PendingMutation,
    mutation: () => Promise<unknown>,
    options?: { reloadMode?: 'mutation-followup' | 'silent'; reloadAfter?: boolean }
  ): Promise<boolean> => {
    if (pendingMutationRef.current) {
      return false;
    }

    setPendingMutation(pendingMutation);
    setState((prev) => ({
      ...prev,
      error: null,
    }));

    try {
      await mutation();
      if (options?.reloadAfter !== false) {
        await load({ mode: options?.reloadMode ?? 'mutation-followup' });
      }
      return true;
    } catch (err) {
      await handleApiError(err);
      setState((prev) => ({
        ...prev,
        loading: false,
        isInitialLoading: false,
        isRefreshing: false,
        error: formatApiError(err),
      }));
      throw err;
    } finally {
      setPendingMutation(null);
    }
  };

  const addMeal = async (
    dayOfWeek: number,
    mealType: 'BREAKFAST' | 'LUNCH' | 'DINNER',
    payload: AddMealRequest
  ) => {
    if (!token) {
      throw new Error('Missing token');
    }
    return runMutation(
      { kind: 'save-meal', dayOfWeek, mealType },
      () => addOrReplaceMeal(year, isoWeek, dayOfWeek, mealType, payload, {
        token,
      })
    );
  };

  const remove = async (
    dayOfWeek: number,
    mealType: 'BREAKFAST' | 'LUNCH' | 'DINNER'
  ) => {
    if (!token) {
      throw new Error('Missing token');
    }
    return runMutation(
      { kind: 'remove-meal', dayOfWeek, mealType },
      () => removeMeal(year, isoWeek, dayOfWeek, mealType, { token })
    );
  };

  return {
    ...state,
    reload: () => load({ mode: state.hasLoaded ? 'reload' : 'initial' }),
    addMeal,
    removeMeal: remove,
  };
}
