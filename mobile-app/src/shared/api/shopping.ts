import { fetchJson, type ApiClientOptions } from './client';

export type ShoppingItemResponse = {
  id: string;
  name: string;
  status: string;
  createdAt: string;
  boughtAt: string | null;
};

export type ShoppingListResponse = {
  id: string;
  name: string;
  items: ShoppingItemResponse[];
};

export type CreateShoppingListRequest = {
  name: string;
};

export type CreateShoppingListResponse = {
  listId: string;
  name: string;
};

export type AddShoppingItemRequest = {
  name: string;
};

export type AddShoppingItemResponse = {
  itemId: string;
  name: string;
  status: string;
  createdAt: string;
  boughtAt: string | null;
};

export type ToggleShoppingItemResponse = {
  itemId: string;
  status: string;
  boughtAt: string | null;
};

export async function createShoppingList(
  payload: CreateShoppingListRequest,
  clientOptions: ApiClientOptions = {}
): Promise<CreateShoppingListResponse> {
  return fetchJson<CreateShoppingListResponse>(
    '/shopping-lists',
    {
      method: 'POST',
      body: JSON.stringify(payload),
    },
    clientOptions
  );
}

export async function listShoppingLists(
  clientOptions: ApiClientOptions = {}
): Promise<ShoppingListResponse[]> {
  return fetchJson<ShoppingListResponse[]>('/shopping-lists', {}, clientOptions);
}

export async function addShoppingItem(
  listId: string,
  payload: AddShoppingItemRequest,
  clientOptions: ApiClientOptions = {}
): Promise<AddShoppingItemResponse> {
  return fetchJson<AddShoppingItemResponse>(
    `/shopping-lists/${listId}/items`,
    {
      method: 'POST',
      body: JSON.stringify(payload),
    },
    clientOptions
  );
}

export async function toggleShoppingItem(
  listId: string,
  itemId: string,
  clientOptions: ApiClientOptions = {}
): Promise<ToggleShoppingItemResponse> {
  return fetchJson<ToggleShoppingItemResponse>(
    `/shopping-lists/${listId}/items/${itemId}/toggle`,
    {
      method: 'PATCH',
    },
    clientOptions
  );
}

export async function deleteShoppingItem(
  listId: string,
  itemId: string,
  clientOptions: ApiClientOptions = {}
): Promise<void> {
  return fetchJson<void>(
    `/shopping-lists/${listId}/items/${itemId}`,
    {
      method: 'DELETE',
    },
    clientOptions
  );
}
