import { useEffect, useMemo, useState } from 'react';
import { formatApiError } from '../../../shared/api/client';
import { useAuth } from '../../../shared/auth/AuthContext';
import {
  createRecipe,
  getRecipe,
  listRecipes,
  updateRecipe,
  type RecipeResponse,
} from '../api/mealsApi';
import {
  createEmptyIngredientRow,
  ingredientRowsFromResponse,
  sanitizeIngredientQuantityInput,
  toIngredientRequests,
  type MealIngredientRow,
  type MealIngredientUnit,
} from '../utils/ingredientRows';

type DetailPendingAction = 'save' | null;

type Params = {
  token: string;
  enabled: boolean;
};

export function useMealsRecipesWorkspace({ token, enabled }: Params) {
  const { handleApiError } = useAuth();
  const [recipes, setRecipes] = useState<RecipeResponse[] | null>(null);
  const [isListLoading, setIsListLoading] = useState(false);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [hasLoaded, setHasLoaded] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [isRecipeDetailOpen, setIsRecipeDetailOpen] = useState(false);
  const [recipeId, setRecipeId] = useState<string | null>(null);
  const [recipeTitle, setRecipeTitle] = useState('');
  const [ingredientRows, setIngredientRows] = useState<MealIngredientRow[]>([]);
  const [isRecipeLoading, setIsRecipeLoading] = useState(false);
  const [recipeDetailError, setRecipeDetailError] = useState<string | null>(null);
  const [pendingDetailAction, setPendingDetailAction] = useState<DetailPendingAction>(null);

  const hasIngredients = useMemo(
    () => toIngredientRequests(ingredientRows).length > 0,
    [ingredientRows]
  );

  const recipeListItems = useMemo(() => {
    const nameCounts = new Map<string, number>();
    for (const recipe of recipes ?? []) {
      const normalizedName = recipe.name.trim().toLocaleLowerCase();
      nameCounts.set(normalizedName, (nameCounts.get(normalizedName) ?? 0) + 1);
    }

    return [...(recipes ?? [])]
      .sort((left, right) => {
        const nameComparison = left.name.localeCompare(right.name);
        if (nameComparison !== 0) {
          return nameComparison;
        }
        return right.createdAt.localeCompare(left.createdAt);
      })
      .map((recipe) => ({
        recipeId: recipe.recipeId,
        name: recipe.name,
        ingredientCount: recipe.ingredients.length,
        duplicateNameCount: nameCounts.get(recipe.name.trim().toLocaleLowerCase()) ?? 1,
        createdLabel: new Date(recipe.createdAt).toLocaleDateString(undefined, {
          day: 'numeric',
          month: 'short',
        }),
      }));
  }, [recipes]);

  async function loadRecipes(options?: { refreshing?: boolean }) {
    if (!token) {
      return;
    }
    const refreshing = options?.refreshing ?? false;
    if (refreshing) {
      setIsRefreshing(true);
    } else {
      setIsListLoading(true);
    }
    setError(null);
    try {
      const nextRecipes = await listRecipes({ token });
      setRecipes(nextRecipes);
      setHasLoaded(true);
    } catch (err) {
      await handleApiError(err);
      setError(formatApiError(err));
    } finally {
      if (refreshing) {
        setIsRefreshing(false);
      } else {
        setIsListLoading(false);
      }
    }
  }

  useEffect(() => {
    if (!enabled || hasLoaded || isListLoading) {
      return;
    }
    void loadRecipes();
  }, [enabled, hasLoaded, isListLoading, token]);

  function applyRecipe(recipe: RecipeResponse) {
    setRecipeId(recipe.recipeId);
    setRecipeTitle(recipe.name);
    setIngredientRows(ingredientRowsFromResponse(recipe.ingredients));
    setRecipeDetailError(null);
    setIsRecipeLoading(false);
    setIsRecipeDetailOpen(true);
  }

  async function openRecipe(recipeIdToOpen: string) {
    const existing = recipes?.find((recipe) => recipe.recipeId === recipeIdToOpen) ?? null;
    setRecipeDetailError(null);
    setIsRecipeDetailOpen(true);

    if (existing) {
      applyRecipe(existing);
      return;
    }

    setIsRecipeLoading(true);
    try {
      const recipe = await getRecipe(recipeIdToOpen, { token });
      setRecipes((current) => {
        const others = (current ?? []).filter((entry) => entry.recipeId !== recipe.recipeId);
        return [...others, recipe];
      });
      applyRecipe(recipe);
    } catch (err) {
      await handleApiError(err);
      setRecipeDetailError(formatApiError(err));
      setIsRecipeLoading(false);
    }
  }

  function openCreateRecipe() {
    setRecipeId(null);
    setRecipeTitle('');
    setIngredientRows([]);
    setRecipeDetailError(null);
    setIsRecipeLoading(false);
    setIsRecipeDetailOpen(true);
  }

  function closeRecipeDetail() {
    if (pendingDetailAction) {
      return;
    }
    setIsRecipeDetailOpen(false);
    setRecipeDetailError(null);
  }

  function addIngredientRow() {
    setIngredientRows((current) => [...current, createEmptyIngredientRow()]);
  }

  function updateIngredientRow(
    rowId: string,
    updater: (row: MealIngredientRow) => MealIngredientRow
  ) {
    setIngredientRows((current) => current.map((row) => (row.id === rowId ? updater(row) : row)));
  }

  function removeIngredientRow(rowId: string) {
    setIngredientRows((current) => {
      if (current.length === 1) {
        return [];
      }
      return current.filter((row) => row.id !== rowId);
    });
  }

  function setIngredientName(rowId: string, value: string) {
    updateIngredientRow(rowId, (row) => ({ ...row, name: value }));
  }

  function setIngredientQuantity(rowId: string, value: string) {
    const quantityText = sanitizeIngredientQuantityInput(value);
    updateIngredientRow(rowId, (row) => ({
      ...row,
      quantityText,
      unit: quantityText.length === 0 ? null : row.unit,
    }));
  }

  function setIngredientUnit(rowId: string, unit: MealIngredientUnit) {
    updateIngredientRow(rowId, (row) => ({
      ...row,
      unit: row.unit === unit ? null : unit,
    }));
  }

  async function saveRecipe() {
    if (pendingDetailAction || isRecipeLoading || !recipeTitle.trim()) {
      return;
    }

    setPendingDetailAction('save');
    setRecipeDetailError(null);
    try {
      const request = {
        name: recipeTitle.trim(),
        ingredients: toIngredientRequests(ingredientRows),
      };
      const saved = recipeId
        ? await updateRecipe(recipeId, request, { token })
        : await createRecipe(request, { token });

      setRecipes((current) => {
        const others = (current ?? []).filter((entry) => entry.recipeId !== saved.recipeId);
        return [...others, saved];
      });
      setHasLoaded(true);
      applyRecipe(saved);
    } catch (err) {
      await handleApiError(err);
      setRecipeDetailError(formatApiError(err));
    } finally {
      setPendingDetailAction(null);
    }
  }

  return {
    recipes: {
      items: recipeListItems,
      isInitialLoading: !hasLoaded && isListLoading,
      isRefreshing,
      error,
      hasLoaded,
      reload: () => loadRecipes({ refreshing: true }),
      openRecipe,
      openCreateRecipe,
    },
    recipeDetail: {
      isOpen: isRecipeDetailOpen,
      recipeId,
      recipeTitle,
      ingredientRows,
      isRecipeLoading,
      hasExistingRecipe: !!recipeId,
      hasIngredients,
      error: recipeDetailError,
      isSavingRecipe: pendingDetailAction === 'save',
      isActionPending: pendingDetailAction !== null,
      setRecipeTitle,
      addIngredientRow,
      removeIngredientRow,
      setIngredientName,
      setIngredientQuantity,
      setIngredientUnit,
      saveRecipe,
      closeRecipeDetail,
    },
  };
}
