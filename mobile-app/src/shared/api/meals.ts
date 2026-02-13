import { fetchJson, type ApiClientOptions } from './client';

export type PlannedMealResponse = {
  dayOfWeek: number;
  mealType: 'BREAKFAST' | 'LUNCH' | 'DINNER';
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
  mealType: 'BREAKFAST' | 'LUNCH' | 'DINNER';
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
  mealType: 'BREAKFAST' | 'LUNCH' | 'DINNER',
  payload: AddMealRequest,
  clientOptions: ApiClientOptions = {}
): Promise<AddMealResponse> {
  return fetchJson<AddMealResponse>(
    `/meals/weeks/${year}/${isoWeek}/days/${dayOfWeek}/meals/${mealType}`,
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
  mealType: 'BREAKFAST' | 'LUNCH' | 'DINNER',
  clientOptions: ApiClientOptions = {}
): Promise<void> {
  return fetchJson<void>(
    `/meals/weeks/${year}/${isoWeek}/days/${dayOfWeek}/meals/${mealType}`,
    {
      method: 'DELETE',
    },
    clientOptions
  );
}
