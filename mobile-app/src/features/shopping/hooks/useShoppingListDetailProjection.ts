import { useMemo } from 'react';
import type { ShoppingListResponse } from '../api/shoppingApi';
import { mapShoppingListFromTransport } from '../utils/shoppingDomain';
import { projectShoppingListDetail } from '../utils/shoppingSections';

type Args = {
  lists: ShoppingListResponse[];
  listId: string;
  orderedOpenItemIds: string[];
};

export function useShoppingListDetailProjection({ lists, listId, orderedOpenItemIds }: Args) {
  return useMemo(() => {
    const selectedTransportList = lists.find((list) => list.id === listId) ?? null;
    const selectedList = selectedTransportList ? mapShoppingListFromTransport(selectedTransportList) : null;
    return projectShoppingListDetail(selectedList, orderedOpenItemIds);
  }, [listId, lists, orderedOpenItemIds]);
}
