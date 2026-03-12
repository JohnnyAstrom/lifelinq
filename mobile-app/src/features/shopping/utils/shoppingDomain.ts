import { type ShoppingListResponse, type ShoppingItemResponse, type ShoppingUnit } from '../api/shoppingApi';
import {
  DEFAULT_SHOPPING_CATEGORY,
  type ShoppingCategoryKey,
  inferShoppingCategory,
} from './shoppingCategories';
import { normalizeShoppingItemTitle } from './shoppingCategoryInference';
import { formatItemMeta, formatItemTitle } from './shoppingFormatting';

export type ShoppingListType = 'grocery' | 'consumables' | 'supplies' | 'mixed';
export type ShoppingGroupingMode = 'effective-category';
export type ShoppingItemStatus = 'OPEN' | 'BOUGHT';
export type ShoppingItemSourceKind = 'unknown' | 'manual' | 'meal-plan' | 'recipe';
export type ShoppingCategoryOrigin = 'fallback' | 'suggested' | 'override';

export type ShoppingCategoryRef = {
  key: ShoppingCategoryKey;
  label: string;
  origin: ShoppingCategoryOrigin;
};

export type ShoppingItemSource = {
  kind: ShoppingItemSourceKind;
};

export type ShoppingItemModel = {
  id: string;
  title: string;
  normalizedTitle: string;
  status: ShoppingItemStatus;
  quantity: number | null;
  unit: ShoppingUnit | null;
  note: null;
  categorySuggestion: ShoppingCategoryRef | null;
  categoryOverride: ShoppingCategoryRef | null;
  effectiveCategory: ShoppingCategoryRef;
  source: ShoppingItemSource;
  createdAt: string;
  boughtAt: string | null;
  displayMeta: string | null;
  displayTitle: string;
};

export type ShoppingListModel = {
  id: string;
  name: string;
  type: ShoppingListType;
  groupingMode: ShoppingGroupingMode;
  items: ShoppingItemModel[];
};

export function mapShoppingListFromTransport(list: ShoppingListResponse): ShoppingListModel {
  const listType = deriveListType(list);
  const groupingMode = deriveGroupingMode(listType);

  return {
    id: list.id,
    name: list.name,
    type: listType,
    groupingMode,
    items: list.items.map((item) => mapShoppingItemFromTransport(item)),
  };
}

export function mapShoppingItemFromTransport(item: ShoppingItemResponse): ShoppingItemModel {
  const normalizedTitle = normalizeShoppingItemTitle(item.name);
  const inferredCategory = inferShoppingCategory(normalizedTitle);
  const categorySuggestion: ShoppingCategoryRef | null =
    inferredCategory.key === DEFAULT_SHOPPING_CATEGORY.key
      ? null
      : {
        key: inferredCategory.key,
        label: inferredCategory.label,
        origin: 'suggested',
      };
  const effectiveCategory = categorySuggestion ?? {
    key: DEFAULT_SHOPPING_CATEGORY.key,
    label: DEFAULT_SHOPPING_CATEGORY.label,
    origin: 'fallback' as const,
  };

  return {
    id: item.id,
    title: item.name,
    normalizedTitle,
    status: item.status === 'BOUGHT' ? 'BOUGHT' : 'OPEN',
    quantity: item.quantity,
    unit: item.unit,
    note: null,
    categorySuggestion,
    categoryOverride: null,
    effectiveCategory,
    source: { kind: 'unknown' },
    createdAt: item.createdAt,
    boughtAt: item.boughtAt,
    displayMeta: formatItemMeta({
      quantity: item.quantity,
      unit: item.unit,
    }),
    displayTitle: formatItemTitle({
      title: item.name,
      quantity: item.quantity,
      unit: item.unit,
    }),
  };
}

function deriveListType(_list: ShoppingListResponse): ShoppingListType {
  // Current transport has no list type, so the feature keeps a neutral default.
  return 'mixed';
}

function deriveGroupingMode(_listType: ShoppingListType): ShoppingGroupingMode {
  return 'effective-category';
}
