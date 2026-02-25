import { type ShoppingItemResponse, type ShoppingUnit } from '../api/shoppingApi';

const UNIT_LABELS: Record<ShoppingUnit, string> = {
  ST: 'pcs',
  FORP: 'pack',
  KG: 'kg',
  HG: 'hg',
  G: 'g',
  L: 'l',
  DL: 'dl',
  ML: 'ml',
};

export function formatItemMeta(item: ShoppingItemResponse) {
  if (item.quantity == null || !item.unit) {
    return null;
  }
  const label = UNIT_LABELS[item.unit] ?? item.unit.toLowerCase();
  return `${item.quantity} ${label}`;
}

export function formatItemTitle(item: ShoppingItemResponse) {
  const meta = formatItemMeta(item);
  if (meta) {
    return `${meta} - ${item.name}`;
  }
  return item.name;
}
