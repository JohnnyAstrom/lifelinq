import { useEffect, useMemo, useState } from 'react';
import { ApiError, formatApiError } from '../../../shared/api/client';
import { useAuth } from '../../../shared/auth/AuthContext';
import {
  archiveRecipe,
  createRecipe,
  createRecipeImportDraft,
  getRecipe,
  listRecipes,
  updateRecipe,
  type RecipeImportDraftResponse,
  type RecipeResponse,
} from '../api/mealsApi';
import {
  createEmptyIngredientRow,
  ingredientRowsFromImportDraft,
  ingredientRowsFromResponse,
  sanitizeIngredientQuantityInput,
  toIngredientRequests,
  type MealIngredientRow,
  type MealIngredientUnit,
} from '../utils/ingredientRows';

type DetailPendingAction = 'save' | 'archive' | null;
type RecipeDetailMode = 'create' | 'saved' | 'import';

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
  const [recipeSource, setRecipeSource] = useState('');
  const [recipeSourceUrl, setRecipeSourceUrl] = useState('');
  const [recipeOriginKind, setRecipeOriginKind] = useState('MANUAL');
  const [recipeShortNote, setRecipeShortNote] = useState('');
  const [recipeInstructions, setRecipeInstructions] = useState('');
  const [ingredientRows, setIngredientRows] = useState<MealIngredientRow[]>([]);
  const [isRecipeLoading, setIsRecipeLoading] = useState(false);
  const [recipeDetailError, setRecipeDetailError] = useState<string | null>(null);
  const [pendingDetailAction, setPendingDetailAction] = useState<DetailPendingAction>(null);
  const [detailMode, setDetailMode] = useState<RecipeDetailMode>('create');

  const [isImportSheetOpen, setIsImportSheetOpen] = useState(false);
  const [importUrl, setImportUrl] = useState('');
  const [importError, setImportError] = useState<string | null>(null);
  const [isImportingDraft, setIsImportingDraft] = useState(false);

  const hasIngredients = useMemo(
    () => toIngredientRequests(ingredientRows).length > 0,
    [ingredientRows]
  );

  const recipeListItems = useMemo(() => {
    const nameCounts = new Map<string, number>();
    const activeRecipes = (recipes ?? []).filter((recipe) => recipe.archivedAt == null);
    for (const recipe of activeRecipes) {
      const normalizedName = recipe.name.trim().toLocaleLowerCase();
      nameCounts.set(normalizedName, (nameCounts.get(normalizedName) ?? 0) + 1);
    }

    return [...activeRecipes]
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

  function formatImportDraftError(err: unknown): string {
    if (err instanceof ApiError) {
      if (err.status === 400) {
        return 'Use a full public recipe URL that starts with http:// or https://.';
      }
      if (err.status === 422) {
        try {
          const payload = JSON.parse(err.body) as { code?: string; message?: string };
          if (payload.code === 'RECIPE_IMPORT_FAILED') {
            const message = payload.message?.toLowerCase() ?? '';
            if (message.includes('fetch recipe url')) {
              return 'We could not reach that recipe page. Check the URL or try another recipe site.';
            }
            if (message.includes('structured recipe data')) {
              return 'We could not find a usable recipe on that page. Try another recipe URL or create it manually.';
            }
            if (message.includes('missing ingredients')) {
              return 'We found the page, but the recipe draft was too incomplete. Try another URL or finish the recipe manually.';
            }
            return 'We could not import that recipe page yet. Try another URL or create the recipe manually.';
          }
        } catch {
          return 'We could not import that recipe page yet. Try another URL or create the recipe manually.';
        }
      }
    }
    return formatApiError(err);
  }

  function applyRecipe(recipe: RecipeResponse) {
    setRecipeId(recipe.recipeId);
    setRecipeTitle(recipe.name);
    setRecipeSource(recipe.sourceName ?? '');
    setRecipeSourceUrl(recipe.sourceUrl ?? '');
    setRecipeOriginKind(recipe.originKind);
    setRecipeShortNote(recipe.shortNote ?? '');
    setRecipeInstructions(recipe.instructions ?? '');
    setIngredientRows(ingredientRowsFromResponse(recipe.ingredients));
    setRecipeDetailError(null);
    setIsRecipeLoading(false);
    setDetailMode('saved');
    setIsRecipeDetailOpen(true);
  }

  function applyImportedDraft(draft: RecipeImportDraftResponse) {
    setRecipeId(null);
    setRecipeTitle(draft.name);
    setRecipeSource(draft.sourceName ?? '');
    setRecipeSourceUrl(draft.sourceUrl);
    setRecipeOriginKind(draft.originKind);
    setRecipeShortNote(draft.shortNote ?? '');
    setRecipeInstructions(draft.instructions ?? '');
    setIngredientRows(ingredientRowsFromImportDraft(draft.ingredients));
    setRecipeDetailError(null);
    setIsRecipeLoading(false);
    setDetailMode('import');
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
    setRecipeSource('');
    setRecipeSourceUrl('');
    setRecipeOriginKind('MANUAL');
    setRecipeShortNote('');
    setRecipeInstructions('');
    setIngredientRows([]);
    setRecipeDetailError(null);
    setIsRecipeLoading(false);
    setDetailMode('create');
    setIsRecipeDetailOpen(true);
  }

  function openImportRecipe() {
    if (isImportingDraft || pendingDetailAction) {
      return;
    }
    setImportUrl('');
    setImportError(null);
    setIsImportSheetOpen(true);
  }

  function closeImportRecipe() {
    if (isImportingDraft) {
      return;
    }
    setIsImportSheetOpen(false);
    setImportError(null);
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

  async function importRecipeDraft() {
    if (isImportingDraft || !importUrl.trim()) {
      return;
    }

    setIsImportingDraft(true);
    setImportError(null);
    try {
      const draft = await createRecipeImportDraft(
        { url: importUrl.trim() },
        { token }
      );
      applyImportedDraft(draft);
      setIsImportSheetOpen(false);
      setImportUrl('');
    } catch (err) {
      await handleApiError(err);
      setImportError(formatImportDraftError(err));
    } finally {
      setIsImportingDraft(false);
    }
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
        sourceName: recipeSource.trim() || null,
        sourceUrl: recipeSourceUrl.trim() || null,
        originKind: recipeOriginKind,
        shortNote: recipeShortNote.trim() || null,
        instructions: recipeInstructions.trim() || null,
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

  async function archiveCurrentRecipe() {
    if (!recipeId || pendingDetailAction || isRecipeLoading) {
      return;
    }

    setPendingDetailAction('archive');
    setRecipeDetailError(null);
    try {
      const archived = await archiveRecipe(recipeId, { token });
      setRecipes((current) => (current ?? []).filter((entry) => entry.recipeId !== archived.recipeId));
      setHasLoaded(true);
      setIsRecipeDetailOpen(false);
      setRecipeId(null);
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
      openImportRecipe,
    },
    importDraft: {
      isOpen: isImportSheetOpen,
      importUrl,
      error: importError,
      isImportingDraft,
      setImportUrl,
      openImportRecipe,
      closeImportRecipe,
      importRecipeDraft,
    },
    recipeDetail: {
      isOpen: isRecipeDetailOpen,
      recipeId,
      recipeTitle,
      recipeSource,
      recipeSourceUrl,
      recipeShortNote,
      recipeInstructions,
      ingredientRows,
      isRecipeLoading,
      hasExistingRecipe: !!recipeId,
      isImportDraft: detailMode === 'import',
      canArchiveRecipe: !!recipeId && detailMode === 'saved',
      hasIngredients,
      error: recipeDetailError,
      isSavingRecipe: pendingDetailAction === 'save',
      isArchivingRecipe: pendingDetailAction === 'archive',
      isActionPending: pendingDetailAction !== null,
      setRecipeTitle,
      setRecipeSource,
      setRecipeSourceUrl,
      setRecipeShortNote,
      setRecipeInstructions,
      addIngredientRow,
      removeIngredientRow,
      setIngredientName,
      setIngredientQuantity,
      setIngredientUnit,
      saveRecipe,
      archiveCurrentRecipe,
      closeRecipeDetail,
    },
  };
}
