import { useSyncExternalStore } from 'react';
import type { ShoppingCategoryKey } from '../utils/shoppingCategories';

const itemCategoryOverrides = new Map<string, ShoppingCategoryKey>();
const rememberedCategoriesByTitle = new Map<string, ShoppingCategoryKey>();
const listeners = new Set<() => void>();
let version = 0;

function emitChange() {
  version += 1;
  listeners.forEach((listener) => listener());
}

function subscribe(listener: () => void) {
  listeners.add(listener);
  return () => {
    listeners.delete(listener);
  };
}

function getSnapshot(): number {
  return version;
}

function getServerSnapshot(): number {
  return 0;
}

function getOverrideForItem(itemId: string): ShoppingCategoryKey | null {
  return itemCategoryOverrides.get(itemId) ?? null;
}

function getMemoryForTitle(normalizedTitle: string): ShoppingCategoryKey | null {
  return rememberedCategoriesByTitle.get(normalizedTitle) ?? null;
}

async function rememberCategory(normalizedTitle: string, categoryKey: ShoppingCategoryKey) {
  rememberedCategoriesByTitle.set(normalizedTitle, categoryKey);
  emitChange();
}

function setCategoryOverride(itemId: string, normalizedTitle: string, categoryKey: ShoppingCategoryKey) {
  itemCategoryOverrides.set(itemId, categoryKey);
  rememberedCategoriesByTitle.set(normalizedTitle, categoryKey);
  emitChange();
}

function clearCategoryOverride(itemId: string) {
  if (itemCategoryOverrides.delete(itemId)) {
    emitChange();
  }
}

export function useShoppingCategoryPreferences() {
  const currentVersion = useSyncExternalStore(subscribe, getSnapshot, getServerSnapshot);

  return {
    version: currentVersion,
    getOverrideForItem,
    getMemoryForTitle,
    rememberCategory,
    setCategoryOverride,
    clearCategoryOverride,
  };
}
