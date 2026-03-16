import {
  type ShoppingItemResponse,
  type ShoppingItemSourceKind,
  type ShoppingListResponse,
  type ShoppingListType,
  type ShoppingUnit,
} from '../api/shoppingApi';
import {
  DEFAULT_SHOPPING_CATEGORY,
  type ShoppingCategoryKey,
  type ShoppingCategoryDefinition,
  getShoppingCategoryDefinition,
  inferShoppingCategory,
} from './shoppingCategories';
import { normalizeShoppingItemTitle } from './shoppingCategoryInference';
import { formatItemMeta, formatItemTitle } from './shoppingFormatting';

export type ShoppingGroupingMode = 'effective-category';
export type ShoppingItemStatus = 'OPEN' | 'BOUGHT';
export type ShoppingItemSourceKindModel = 'unknown' | ShoppingItemSourceKind;
export type ShoppingCategoryOrigin = 'fallback' | 'suggested' | 'memory' | 'override';

export type ShoppingCategoryRef = {
  key: ShoppingCategoryKey;
  label: string;
  origin: ShoppingCategoryOrigin;
};

export type ShoppingItemSource = {
  kind: ShoppingItemSourceKindModel;
  label: string | null;
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
  getMemoryForTitle: (listType: ShoppingListType, normalizedTitle: string) => ShoppingCategoryKey | null;
};

type ResolveShoppingCategoryArgs = {
  itemId?: string | null;
  explicitOverrideKey?: ShoppingCategoryKey | null;
  listType: ShoppingListType;
  normalizedTitle: string;
  categoryContext: ShoppingCategoryContext;
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
    items: list.items.map((item) => mapShoppingItemFromTransport(item, listType, categoryContext)),
  };
}

export function mapShoppingItemFromTransport(
  item: ShoppingItemResponse,
  listType: ShoppingListType,
  categoryContext: ShoppingCategoryContext
): ShoppingItemModel {
  const normalizedTitle = normalizeShoppingItemTitle(item.name);
  const categoryResolution = resolveShoppingCategory({
    itemId: item.id,
    listType,
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
    source: {
      kind: item.sourceKind ?? 'unknown',
      label: item.sourceLabel ?? null,
    },
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

export function resolveShoppingCategory({
  itemId,
  explicitOverrideKey,
  listType,
  normalizedTitle,
  categoryContext,
}: ResolveShoppingCategoryArgs): Pick<ShoppingItemModel, 'categorySuggestion' | 'categoryOverride' | 'effectiveCategory'> {
  const overrideKey = explicitOverrideKey ?? (itemId ? categoryContext.getOverrideForItem(itemId) : null);
  const memoryKey = categoryContext.getMemoryForTitle(listType, normalizedTitle);
  const inferredCategory = inferShoppingCategory(normalizedTitle, listType);

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

function deriveListType(list: ShoppingListResponse): ShoppingListType {
  return list.type ?? 'mixed';
}

function deriveGroupingMode(_listType: ShoppingListType): ShoppingGroupingMode {
  return 'effective-category';
}
