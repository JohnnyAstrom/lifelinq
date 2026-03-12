import { useEffect, useSyncExternalStore } from 'react';
import { useAuth } from '../../../shared/auth/AuthContext';
import {
  listShoppingCategoryPreferences,
  saveShoppingCategoryPreference,
} from '../api/shoppingCategoryPreferencesApi';
import type { ShoppingListType } from '../api/shoppingApi';
import type { ShoppingCategoryKey } from '../utils/shoppingCategories';

const itemCategoryOverrides = new Map<string, ShoppingCategoryKey>();
const rememberedCategoriesByTitle = new Map<string, ShoppingCategoryKey>();
const listeners = new Set<() => void>();
let version = 0;
let loadedGroupId: string | null = null;
let loadingGroupId: string | null = null;

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

function getMemoryForTitle(listType: ShoppingListType, normalizedTitle: string): ShoppingCategoryKey | null {
  return rememberedCategoriesByTitle.get(memoryKey(listType, normalizedTitle)) ?? null;
}

function resetCategoryMemoryStore(nextGroupId: string | null) {
  itemCategoryOverrides.clear();
  rememberedCategoriesByTitle.clear();
  loadedGroupId = nextGroupId;
  loadingGroupId = null;
  emitChange();
}

function setCategoryOverride(
  itemId: string,
  listType: ShoppingListType,
  normalizedTitle: string,
  categoryKey: ShoppingCategoryKey
) {
  itemCategoryOverrides.set(itemId, categoryKey);
  rememberedCategoriesByTitle.set(memoryKey(listType, normalizedTitle), categoryKey);
  emitChange();
}

function clearCategoryOverride(itemId: string) {
  if (itemCategoryOverrides.delete(itemId)) {
    emitChange();
  }
}

function memoryKey(listType: ShoppingListType, normalizedTitle: string): string {
  return `${listType}::${normalizedTitle}`;
}

export function useShoppingCategoryPreferences() {
  const { token, me, handleApiError } = useAuth();
  const currentVersion = useSyncExternalStore(subscribe, getSnapshot, getServerSnapshot);
  const activeGroupId = me?.activeGroupId ?? null;

  useEffect(() => {
    if (!token || !activeGroupId) {
      if (loadedGroupId !== null || rememberedCategoriesByTitle.size > 0 || itemCategoryOverrides.size > 0) {
        resetCategoryMemoryStore(null);
      }
      return;
    }
    if (loadedGroupId !== null && loadedGroupId !== activeGroupId) {
      resetCategoryMemoryStore(null);
    }
    if (loadedGroupId === activeGroupId || loadingGroupId === activeGroupId) {
      return;
    }

    loadingGroupId = activeGroupId;

    void (async () => {
      try {
        const preferences = await listShoppingCategoryPreferences({ token });
        if (loadingGroupId !== activeGroupId) {
          return;
        }
        itemCategoryOverrides.clear();
        rememberedCategoriesByTitle.clear();
        for (const preference of preferences) {
          rememberedCategoriesByTitle.set(
            memoryKey(preference.listType, preference.normalizedTitle),
            preference.preferredCategory as ShoppingCategoryKey
          );
        }
        loadedGroupId = activeGroupId;
        loadingGroupId = null;
        emitChange();
      } catch (error) {
        loadingGroupId = null;
        await handleApiError(error);
      }
    })();
  }, [activeGroupId, handleApiError, token]);

  async function persistCategoryPreference(
    listType: ShoppingListType,
    normalizedTitle: string,
    categoryKey: ShoppingCategoryKey
  ) {
    if (!token || !activeGroupId) {
      rememberedCategoriesByTitle.set(memoryKey(listType, normalizedTitle), categoryKey);
      emitChange();
      return;
    }
    try {
      const saved = await saveShoppingCategoryPreference(
        {
          listType,
          normalizedTitle,
          preferredCategory: categoryKey,
        },
        { token }
      );
      rememberedCategoriesByTitle.set(
        memoryKey(saved.listType, saved.normalizedTitle),
        saved.preferredCategory as ShoppingCategoryKey
      );
      loadedGroupId = activeGroupId;
      emitChange();
    } catch (error) {
      await handleApiError(error);
      throw error;
    }
  }

  return {
    version: currentVersion,
    getOverrideForItem,
    getMemoryForTitle,
    rememberCategory: persistCategoryPreference,
    setCategoryOverride,
    clearCategoryOverride,
  };
}
