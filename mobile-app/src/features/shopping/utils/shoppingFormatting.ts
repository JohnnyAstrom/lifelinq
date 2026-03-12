import { type ShoppingUnit } from '../api/shoppingApi';

type ShoppingQuantityShape = {
  quantity: number | null;
  unit: ShoppingUnit | null;
};

type ShoppingTitleShape = ShoppingQuantityShape & {
  title: string;
};

const UNIT_LABELS: Record<ShoppingUnit, string> = {
  PCS: 'pcs',
  PACK: 'pack',
  KG: 'kg',
  HG: 'hg',
  G: 'g',
  L: 'l',
  DL: 'dl',
  ML: 'ml',
};

export function formatItemMeta(item: ShoppingQuantityShape) {
  if (item.quantity == null || !item.unit) {
    return null;
  }
  const label = UNIT_LABELS[item.unit] ?? item.unit.toLowerCase();
  return `${item.quantity} ${label}`;
}

export function formatItemTitle(item: ShoppingTitleShape) {
  const meta = formatItemMeta(item);
  if (meta) {
    return `${meta} - ${item.title}`;
  }
  return item.title;
}
