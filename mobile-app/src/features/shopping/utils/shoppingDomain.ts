import { type ShoppingListResponse, type ShoppingItemResponse, type ShoppingUnit } from '../api/shoppingApi';
import {
  DEFAULT_SHOPPING_CATEGORY,
  type ShoppingCategoryKey,
  type ShoppingCategoryDefinition,
  getShoppingCategoryDefinition,
  inferShoppingCategory,
} from './shoppingCategories';
import { normalizeShoppingItemTitle } from './shoppingCategoryInference';
import { formatItemMeta, formatItemTitle } from './shoppingFormatting';

export type ShoppingListType = 'grocery' | 'consumables' | 'supplies' | 'mixed';
export type ShoppingGroupingMode = 'effective-category';
export type ShoppingItemStatus = 'OPEN' | 'BOUGHT';
export type ShoppingItemSourceKind = 'unknown' | 'manual' | 'meal-plan' | 'recipe';
export type ShoppingCategoryOrigin = 'fallback' | 'suggested' | 'memory' | 'override';

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

export type ShoppingCategoryContext = {
  getOverrideForItem: (itemId: string) => ShoppingCategoryKey | null;
  getMemoryForTitle: (normalizedTitle: string) => ShoppingCategoryKey | null;
};

export function mapShoppingListFromTransport(
  list: ShoppingListResponse,
  categoryContext: ShoppingCategoryContext
): ShoppingListModel {
  const listType = deriveListType(list);
  const groupingMode = deriveGroupingMode(listType);

  return {
    id: list.id,
    name: list.name,
    type: listType,
    groupingMode,
    items: list.items.map((item) => mapShoppingItemFromTransport(item, categoryContext)),
  };
}

export function mapShoppingItemFromTransport(
  item: ShoppingItemResponse,
  categoryContext: ShoppingCategoryContext
): ShoppingItemModel {
  const normalizedTitle = normalizeShoppingItemTitle(item.name);
  const categoryResolution = resolveShoppingCategory({
    itemId: item.id,
    normalizedTitle,
    categoryContext,
  });

  return {
    id: item.id,
    title: item.name,
    normalizedTitle,
    status: item.status === 'BOUGHT' ? 'BOUGHT' : 'OPEN',
    quantity: item.quantity,
    unit: item.unit,
    note: null,
    categorySuggestion: categoryResolution.categorySuggestion,
    categoryOverride: categoryResolution.categoryOverride,
    effectiveCategory: categoryResolution.effectiveCategory,
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

type ResolveShoppingCategoryArgs = {
  itemId: string;
  normalizedTitle: string;
  categoryContext: ShoppingCategoryContext;
};

function resolveShoppingCategory({
  itemId,
  normalizedTitle,
  categoryContext,
}: ResolveShoppingCategoryArgs): Pick<ShoppingItemModel, 'categorySuggestion' | 'categoryOverride' | 'effectiveCategory'> {
  const overrideKey = categoryContext.getOverrideForItem(itemId);
  const memoryKey = categoryContext.getMemoryForTitle(normalizedTitle);
  const inferredCategory = inferShoppingCategory(normalizedTitle);

  const categoryOverride = overrideKey ? toCategoryRef(getShoppingCategoryDefinition(overrideKey), 'override') : null;
  const categorySuggestion = memoryKey
    ? toCategoryRef(getShoppingCategoryDefinition(memoryKey), 'memory')
    : inferredCategory.key !== DEFAULT_SHOPPING_CATEGORY.key
      ? toCategoryRef(inferredCategory, 'suggested')
      : null;
  const effectiveCategory = categoryOverride
    ?? categorySuggestion
    ?? toCategoryRef(DEFAULT_SHOPPING_CATEGORY, 'fallback');

  return {
    categorySuggestion,
    categoryOverride,
    effectiveCategory,
  };
}

function toCategoryRef(definition: ShoppingCategoryDefinition, origin: ShoppingCategoryOrigin): ShoppingCategoryRef {
  return {
    key: definition.key,
    label: definition.label,
    origin,
  };
}

function deriveListType(_list: ShoppingListResponse): ShoppingListType {
  // Current transport has no list type, so the feature keeps a neutral default.
  return 'mixed';
}

function deriveGroupingMode(_listType: ShoppingListType): ShoppingGroupingMode {
  return 'effective-category';
}
