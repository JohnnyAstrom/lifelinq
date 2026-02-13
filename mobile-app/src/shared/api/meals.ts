import { fetchJson, type ApiClientOptions } from './client';

export type PlannedMealResponse = {
  dayOfWeek: number;
  recipeId: string;
  recipeTitle: string;
};

export type WeekPlanResponse = {
  weekPlanId: string | null;
  year: number;
  isoWeek: number;
  createdAt: string | null;
  meals: PlannedMealResponse[];
};

export type AddMealRequest = {
  recipeId: string;
  recipeTitle: string;
  targetShoppingListId?: string | null;
};

export type AddMealResponse = {
  weekPlanId: string;
  year: number;
  isoWeek: number;
  meal: PlannedMealResponse;
};

export async function getWeekPlan(
  year: number,
  isoWeek: number,
  clientOptions: ApiClientOptions = {}
): Promise<WeekPlanResponse> {
  return fetchJson<WeekPlanResponse>(
    `/meals/weeks/${year}/${isoWeek}`,
    {},
    clientOptions
  );
}

export async function addOrReplaceMeal(
  year: number,
  isoWeek: number,
  dayOfWeek: number,
  payload: AddMealRequest,
  clientOptions: ApiClientOptions = {}
): Promise<AddMealResponse> {
  return fetchJson<AddMealResponse>(
    `/meals/weeks/${year}/${isoWeek}/days/${dayOfWeek}`,
    {
      method: 'POST',
      body: JSON.stringify(payload),
    },
    clientOptions
  );
}

export async function removeMeal(
  year: number,
  isoWeek: number,
  dayOfWeek: number,
  clientOptions: ApiClientOptions = {}
): Promise<void> {
  return fetchJson<void>(
    `/meals/weeks/${year}/${isoWeek}/days/${dayOfWeek}`,
    {
      method: 'DELETE',
    },
    clientOptions
  );
}
