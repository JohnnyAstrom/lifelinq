import { useMemo } from 'react';
import type { ShoppingListResponse } from '../api/shoppingApi';
import { mapShoppingListFromTransport, type ShoppingCategoryContext } from '../utils/shoppingDomain';
import { projectShoppingListDetail } from '../utils/shoppingSections';

type Args = {
  lists: ShoppingListResponse[];
  listId: string;
  orderedOpenItemIds: string[];
  categoryContext: ShoppingCategoryContext;
  categoryPreferencesVersion: number;
};

export function useShoppingListDetailProjection({
  lists,
  listId,
  orderedOpenItemIds,
  categoryContext,
  categoryPreferencesVersion,
}: Args) {
  return useMemo(() => {
    const selectedTransportList = lists.find((list) => list.id === listId) ?? null;
    const selectedList = selectedTransportList
      ? mapShoppingListFromTransport(selectedTransportList, categoryContext)
      : null;
    return projectShoppingListDetail(selectedList, orderedOpenItemIds);
  }, [categoryPreferencesVersion, listId, lists, orderedOpenItemIds]);
}
