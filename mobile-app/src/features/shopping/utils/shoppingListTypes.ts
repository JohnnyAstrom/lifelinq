import type { ShoppingListType } from '../api/shoppingApi';

export type ShoppingListTypeDefinition = {
  key: ShoppingListType;
  label: string;
};

const SHOPPING_LIST_TYPE_DEFINITIONS: ShoppingListTypeDefinition[] = [
  { key: 'grocery', label: 'Groceries' },
  { key: 'consumables', label: 'Consumables' },
  { key: 'supplies', label: 'Supplies' },
  { key: 'mixed', label: 'Mixed' },
];

export function getShoppingListTypeDefinitions(): ShoppingListTypeDefinition[] {
  return SHOPPING_LIST_TYPE_DEFINITIONS;
}

export function getShoppingListTypeLabel(type: ShoppingListType): string {
  return SHOPPING_LIST_TYPE_DEFINITIONS.find((definition) => definition.key === type)?.label ?? 'Mixed';
}
