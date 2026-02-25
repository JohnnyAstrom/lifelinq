import { type ShoppingUnit } from '../api/shoppingApi';

export function parseQuantity(value: string): number | null {
  if (!value.trim()) {
    return null;
  }
  const parsed = Number(value.replace(',', '.'));
  if (!Number.isFinite(parsed) || parsed <= 0) {
    return NaN;
  }
  return parsed;
}

export function formatQuantityForFeedback(value: number): string {
  return Number.isInteger(value) ? String(value) : String(value);
}

export function formatUnitForFeedback(unit: ShoppingUnit): string {
  switch (unit) {
    case 'ST':
      return 'pcs';
    case 'FORP':
      return 'pack';
    case 'KG':
      return 'kg';
    case 'HG':
      return 'hg';
    case 'G':
      return 'g';
    case 'L':
      return 'l';
    case 'DL':
      return 'dl';
    case 'ML':
      return 'ml';
    default:
      return '';
  }
}
