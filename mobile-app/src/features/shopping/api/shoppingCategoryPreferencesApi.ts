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

export async function clearShoppingCategoryPreference(
  listType: ShoppingListType,
  normalizedTitle: string,
  clientOptions: ApiClientOptions = {}
): Promise<void> {
  return fetchJson<void>(
    `/shopping/category-preferences?listType=${encodeURIComponent(listType)}&normalizedTitle=${encodeURIComponent(normalizedTitle)}`,
    {
      method: 'DELETE',
    },
    clientOptions
  );
}
