import { fetchJson } from '../../../shared/api/client';

export type CreateShoppingItemResponse = {
  itemId: string;
};

export async function createShoppingItem(
  token: string,
  name: string
): Promise<CreateShoppingItemResponse> {
  return fetchJson<CreateShoppingItemResponse>(
    '/shopping-items',
    {
      method: 'POST',
      body: JSON.stringify({ name }),
    },
    { token }
  );
}
