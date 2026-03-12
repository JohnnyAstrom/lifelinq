import type { ShoppingItemModel, ShoppingListModel } from './shoppingDomain';
import { getShoppingCategoryDefinition } from './shoppingCategories';

export type ShoppingItemSection = {
  id: string;
  title: string;
  items: ShoppingItemModel[];
  itemCount: number;
  variant: 'open' | 'bought';
};

export type ShoppingListDetailProjection = {
  list: ShoppingListModel | null;
  openItems: ShoppingItemModel[];
  openSections: ShoppingItemSection[];
  boughtSection: ShoppingItemSection | null;
  openItemCount: number;
  boughtItemCount: number;
};

export function projectShoppingListDetail(
  list: ShoppingListModel | null,
  orderedOpenItemIds: string[]
): ShoppingListDetailProjection {
  if (!list) {
    return {
      list: null,
      openItems: [],
      openSections: [],
      boughtSection: null,
      openItemCount: 0,
      boughtItemCount: 0,
    };
  }

  const openItemsBase = list.items.filter((item) => item.status !== 'BOUGHT');
  const openItems = applyOpenOrdering(openItemsBase, orderedOpenItemIds);
  const openSections = groupOpenItems(openItems);
  const boughtItems = list.items.filter((item) => item.status === 'BOUGHT');

  return {
    list,
    openItems,
    openSections,
    boughtSection: {
      id: 'bought',
      title: 'Bought',
      items: boughtItems,
      itemCount: boughtItems.length,
      variant: 'bought',
    },
    openItemCount: openItems.length,
    boughtItemCount: boughtItems.length,
  };
}

function applyOpenOrdering(
  openItems: ShoppingItemModel[],
  orderedOpenItemIds: string[]
): ShoppingItemModel[] {
  if (orderedOpenItemIds.length === 0) {
    return openItems;
  }

  const byId = new Map(openItems.map((item) => [item.id, item]));
  const ordered = orderedOpenItemIds
    .map((id) => byId.get(id))
    .filter((item): item is ShoppingItemModel => !!item);

  return ordered.length === openItems.length ? ordered : openItems;
}

function groupOpenItems(openItems: ShoppingItemModel[]): ShoppingItemSection[] {
  const sections = new Map<string, ShoppingItemSection>();

  for (const item of openItems) {
    const sectionId = `category:${item.effectiveCategory.key}`;

    if (!sections.has(sectionId)) {
      sections.set(sectionId, {
        id: sectionId,
        title: item.effectiveCategory.label,
        items: [],
        itemCount: 0,
        variant: 'open',
      });
    }

    const section = sections.get(sectionId)!;
    section.items.push(item);
    section.itemCount = section.items.length;
  }

  return Array.from(sections.values()).sort((left, right) => {
    const leftOrder = getShoppingCategoryDefinition(left.items[0]?.effectiveCategory.key ?? 'other').order;
    const rightOrder = getShoppingCategoryDefinition(right.items[0]?.effectiveCategory.key ?? 'other').order;
    return leftOrder - rightOrder;
  });
}
