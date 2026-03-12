import type { ShoppingListType } from './shoppingApi';
import { fetchJson, type ApiClientOptions } from '../../../shared/api/client';

export type ShoppingCategoryPreferenceResponse = {
  listType: ShoppingListType;
  normalizedTitle: string;
  preferredCategory: string;
};

export type SaveShoppingCategoryPreferenceRequest = {
  listType: ShoppingListType;
  normalizedTitle: string;
  preferredCategory: string;
};

export async function listShoppingCategoryPreferences(
  clientOptions: ApiClientOptions = {}
): Promise<ShoppingCategoryPreferenceResponse[]> {
  return fetchJson<ShoppingCategoryPreferenceResponse[]>('/shopping/category-preferences', {}, clientOptions);
}

export async function saveShoppingCategoryPreference(
  payload: SaveShoppingCategoryPreferenceRequest,
  clientOptions: ApiClientOptions = {}
): Promise<ShoppingCategoryPreferenceResponse> {
  return fetchJson<ShoppingCategoryPreferenceResponse>(
    '/shopping/category-preferences',
    {
      method: 'PUT',
      body: JSON.stringify(payload),
    },
    clientOptions
  );
}
