import { fetchJson } from '../../../shared/api/client';
export {
  addShoppingItem,
  createShoppingList,
  deleteShoppingItem,
  deleteShoppingList,
  listShoppingLists,
  reorderShoppingItem,
  reorderShoppingList,
  toggleShoppingItem,
  updateShoppingItem,
  updateShoppingList,
  type AddShoppingItemRequest,
  type AddShoppingItemResponse,
  type CreateShoppingListRequest,
  type CreateShoppingListResponse,
  type ReorderShoppingItemRequest,
  type ReorderShoppingListRequest,
  type ShoppingItemResponse,
  type ShoppingListResponse,
  type ShoppingListType,
  type ShoppingUnit,
  type ToggleShoppingItemResponse,
  type UpdateShoppingItemRequest,
  type UpdateShoppingItemResponse,
  type UpdateShoppingListRequest,
} from '../../../shared/api/shopping';

export type CreateShoppingItemResponse = {
  itemId: string;
};

export async function createShoppingItem(
  token: string,
  listId: string,
  name: string
): Promise<CreateShoppingItemResponse> {
  return fetchJson<CreateShoppingItemResponse>(
    `/shopping-lists/${encodeURIComponent(listId)}/items`,
    {
      method: 'POST',
      body: JSON.stringify({ name }),
    },
    { token }
  );
}
