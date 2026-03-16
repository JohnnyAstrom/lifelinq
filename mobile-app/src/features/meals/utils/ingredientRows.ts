import type { IngredientRequest, IngredientResponse } from '../api/mealsApi';

export type MealIngredientUnit = 'PCS' | 'PACK' | 'KG' | 'HG' | 'G' | 'L' | 'DL' | 'ML';

export type MealIngredientRow = {
  id: string;
  name: string;
  quantityText: string;
  unit: MealIngredientUnit | null;
};

export type MealIngredientUnitOption = {
  label: string;
  value: MealIngredientUnit;
};

export const MEAL_INGREDIENT_UNIT_OPTIONS: MealIngredientUnitOption[] = [
  { label: 'pcs', value: 'PCS' },
  { label: 'pack', value: 'PACK' },
  { label: 'kg', value: 'KG' },
  { label: 'hg', value: 'HG' },
  { label: 'g', value: 'G' },
  { label: 'l', value: 'L' },
  { label: 'dl', value: 'DL' },
  { label: 'ml', value: 'ML' },
];

function createRowId() {
  return `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
}

export function createEmptyIngredientRow(): MealIngredientRow {
  return {
    id: createRowId(),
    name: '',
    quantityText: '',
    unit: null,
  };
}

export function ingredientRowsFromResponse(ingredients: IngredientResponse[]): MealIngredientRow[] {
  if (ingredients.length === 0) {
    return [];
  }

  return ingredients.map((ingredient) => ({
    id: ingredient.id,
    name: ingredient.name,
    quantityText: ingredient.quantity == null ? '' : String(ingredient.quantity),
    unit: ingredient.unit as MealIngredientUnit | null,
  }));
}

export function sanitizeIngredientQuantityInput(value: string) {
  let normalized = '';
  let hasSeparator = false;

  for (const char of value) {
    if (/\d/.test(char)) {
      normalized += char;
      continue;
    }
    if ((char === '.' || char === ',') && !hasSeparator) {
      normalized += '.';
      hasSeparator = true;
    }
  }

  return normalized;
}

function parseIngredientQuantity(value: string) {
  const normalized = value.trim();
  if (!normalized) {
    return null;
  }

  const parsed = Number(normalized);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : null;
}

export function toIngredientRequests(rows: MealIngredientRow[]): IngredientRequest[] {
  return rows
    .map((row, index) => {
      const quantity = parseIngredientQuantity(row.quantityText);
      return {
        name: row.name.trim(),
        quantity,
        unit: quantity == null ? null : row.unit,
        position: index + 1,
      };
    })
    .filter((row) => row.name.length > 0);
}
