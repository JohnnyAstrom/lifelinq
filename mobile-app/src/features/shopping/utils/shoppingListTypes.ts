import type { ShoppingListType } from '../api/shoppingApi';

export type ShoppingListTypeDefinition = {
  key: ShoppingListType;
  label: string;
  icon: string;
};

const SHOPPING_LIST_TYPE_DEFINITIONS: ShoppingListTypeDefinition[] = [
  { key: 'grocery', label: 'Groceries', icon: 'basket-outline' },
  { key: 'consumables', label: 'Consumables', icon: 'water-outline' },
  { key: 'supplies', label: 'Supplies', icon: 'construct-outline' },
  { key: 'mixed', label: 'Mixed', icon: 'layers-outline' },
];

export function getShoppingListTypeDefinitions(): ShoppingListTypeDefinition[] {
  return SHOPPING_LIST_TYPE_DEFINITIONS;
}

export function getShoppingListTypeLabel(type: ShoppingListType): string {
  return SHOPPING_LIST_TYPE_DEFINITIONS.find((definition) => definition.key === type)?.label ?? 'Mixed';
}

export function getShoppingListTypeDefinition(type: ShoppingListType): ShoppingListTypeDefinition {
  return SHOPPING_LIST_TYPE_DEFINITIONS.find((definition) => definition.key === type)
    ?? SHOPPING_LIST_TYPE_DEFINITIONS[SHOPPING_LIST_TYPE_DEFINITIONS.length - 1];
}
