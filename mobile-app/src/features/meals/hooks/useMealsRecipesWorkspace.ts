import { useEffect, useMemo, useState } from 'react';
import { ApiError, formatApiError } from '../../../shared/api/client';
import { useAuth } from '../../../shared/auth/AuthContext';
import {
  archiveRecipe,
  createRecipe,
  createRecipeImportDraft,
  deleteRecipe,
  getRecipe,
  listArchivedRecipes,
  listRecipes,
  restoreRecipe,
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

type DetailPendingAction = 'save' | 'archive' | 'delete' | null;
type RecipeDetailMode = 'create' | 'saved' | 'import';
type RecipeListMode = 'active' | 'archived';

type Params = {
  token: string;
  enabled: boolean;
};

export function useMealsRecipesWorkspace({ token, enabled }: Params) {
  const { handleApiError } = useAuth();
  const [activeRecipes, setActiveRecipes] = useState<RecipeResponse[] | null>(null);
  const [archivedRecipes, setArchivedRecipes] = useState<RecipeResponse[] | null>(null);
  const [isListLoading, setIsListLoading] = useState(false);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [hasLoaded, setHasLoaded] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [listMode, setListMode] = useState<RecipeListMode>('active');

  const [isRecipeDetailOpen, setIsRecipeDetailOpen] = useState(false);
  const [recipeId, setRecipeId] = useState<string | null>(null);
  const [recipeTitle, setRecipeTitle] = useState('');
  const [recipeSource, setRecipeSource] = useState('');
  const [recipeSourceUrl, setRecipeSourceUrl] = useState('');
  const [recipeOriginKind, setRecipeOriginKind] = useState('MANUAL');
  const [recipeShortNote, setRecipeShortNote] = useState('');
  const [recipeInstructions, setRecipeInstructions] = useState('');
  const [recipeArchivedAt, setRecipeArchivedAt] = useState<string | null>(null);
  const [recipeDeleteEligible, setRecipeDeleteEligible] = useState(false);
  const [recipeDeleteBlockedReason, setRecipeDeleteBlockedReason] = useState<string | null>(null);
  const [ingredientRows, setIngredientRows] = useState<MealIngredientRow[]>([]);
  const [isRecipeLoading, setIsRecipeLoading] = useState(false);
  const [recipeDetailError, setRecipeDetailError] = useState<string | null>(null);
  const [pendingDetailAction, setPendingDetailAction] = useState<DetailPendingAction>(null);
  const [detailMode, setDetailMode] = useState<RecipeDetailMode>('create');
  const [isRecipeReadMode, setIsRecipeReadMode] = useState(false);

  const [isImportSheetOpen, setIsImportSheetOpen] = useState(false);
  const [importUrl, setImportUrl] = useState('');
  const [importError, setImportError] = useState<string | null>(null);
  const [isImportingDraft, setIsImportingDraft] = useState(false);

  const hasIngredients = useMemo(
    () => toIngredientRequests(ingredientRows).length > 0,
    [ingredientRows]
  );

  const visibleRecipes = useMemo(
    () => listMode === 'active' ? (activeRecipes ?? []) : (archivedRecipes ?? []),
    [activeRecipes, archivedRecipes, listMode]
  );

  const recipeListItems = useMemo(() => {
    const nameCounts = new Map<string, number>();
    const titleFamilyCounts = new Map<string, number>();

    function normalizeTitle(value: string) {
      return value
        .trim()
        .toLocaleLowerCase()
        .replace(/[^\p{L}\p{N}\s]/gu, ' ')
        .replace(/\s+/g, ' ')
        .trim();
    }

    function buildTitleFamilyKey(name: string) {
      const tokens = normalizeTitle(name)
        .split(' ')
        .filter((token) => token.length > 1)
        .filter((token) => !['and', 'med', 'och', 'with'].includes(token));
      if (tokens.length < 2) {
        return tokens[0] ?? null;
      }
      return `${tokens[0]} ${tokens[1]}`;
    }

    for (const recipe of visibleRecipes) {
      const normalizedName = normalizeTitle(recipe.name);
      nameCounts.set(normalizedName, (nameCounts.get(normalizedName) ?? 0) + 1);

      const titleFamilyKey = buildTitleFamilyKey(recipe.name);
      if (titleFamilyKey) {
        titleFamilyCounts.set(titleFamilyKey, (titleFamilyCounts.get(titleFamilyKey) ?? 0) + 1);
      }
    }

    function buildIdentitySummary(recipe: RecipeResponse) {
      const sourceName = recipe.sourceName?.trim() ?? '';
      if (sourceName) {
        return sourceName;
      }

      const shortNote = recipe.shortNote?.trim() ?? '';
      if (shortNote) {
        return shortNote.length > 56 ? `${shortNote.slice(0, 53).trimEnd()}...` : shortNote;
      }

      const ingredientNames = recipe.ingredients
        .map((ingredient) => ingredient.name.trim())
        .filter((name) => name.length > 0)
        .slice(0, 2);

      if (ingredientNames.length > 0) {
        return ingredientNames.join(', ');
      }

      return null;
    }

    return [...visibleRecipes]
      .sort((left, right) => {
        const nameComparison = left.name.localeCompare(right.name);
        if (nameComparison !== 0) {
          return nameComparison;
        }
        const sourceComparison = (left.sourceName ?? '').localeCompare(right.sourceName ?? '');
        if (sourceComparison !== 0) {
          return sourceComparison;
        }
        return right.createdAt.localeCompare(left.createdAt);
      })
      .map((recipe) => ({
        recipeId: recipe.recipeId,
        name: recipe.name,
        ingredientCount: recipe.ingredients.length,
        duplicateNameCount: nameCounts.get(normalizeTitle(recipe.name)) ?? 1,
        similarNameCount: titleFamilyCounts.get(buildTitleFamilyKey(recipe.name) ?? '') ?? 1,
        identitySummary: buildIdentitySummary(recipe),
        archivedAt: recipe.archivedAt,
      }));
  }, [visibleRecipes]);

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
      const [nextActiveRecipes, nextArchivedRecipes] = await Promise.all([
        listRecipes({ token }),
        listArchivedRecipes({ token }),
      ]);
      setActiveRecipes(nextActiveRecipes);
      setArchivedRecipes(nextArchivedRecipes);
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
    setRecipeArchivedAt(recipe.archivedAt);
    setRecipeDeleteEligible(recipe.deleteEligible);
    setRecipeDeleteBlockedReason(recipe.deleteBlockedReason);
    setIngredientRows(ingredientRowsFromResponse(recipe.ingredients));
    setRecipeDetailError(null);
    setIsRecipeLoading(false);
    setDetailMode('saved');
    setIsRecipeReadMode(true);
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
    setRecipeArchivedAt(null);
    setRecipeDeleteEligible(false);
    setRecipeDeleteBlockedReason(null);
    setIngredientRows(ingredientRowsFromImportDraft(draft.ingredients));
    setRecipeDetailError(null);
    setIsRecipeLoading(false);
    setDetailMode('import');
    setIsRecipeReadMode(false);
    setIsRecipeDetailOpen(true);
  }

  async function openRecipe(recipeIdToOpen: string) {
    const existing = activeRecipes?.find((recipe) => recipe.recipeId === recipeIdToOpen)
      ?? archivedRecipes?.find((recipe) => recipe.recipeId === recipeIdToOpen)
      ?? null;
    setRecipeDetailError(null);
    setIsRecipeDetailOpen(true);

    if (existing && !existing.archivedAt) {
      applyRecipe(existing);
      return;
    }

    if (existing) {
      applyRecipe(existing);
      setIsRecipeLoading(true);
    } else {
      setIsRecipeLoading(true);
    }
    try {
      const recipe = await getRecipe(recipeIdToOpen, { token });
      if (recipe.archivedAt) {
        setArchivedRecipes((current) => {
          const others = (current ?? []).filter((entry) => entry.recipeId !== recipe.recipeId);
          return [...others, recipe];
        });
      } else {
        setActiveRecipes((current) => {
          const others = (current ?? []).filter((entry) => entry.recipeId !== recipe.recipeId);
          return [...others, recipe];
        });
      }
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
    setRecipeArchivedAt(null);
    setRecipeDeleteEligible(false);
    setRecipeDeleteBlockedReason(null);
    setIngredientRows([createEmptyIngredientRow()]);
    setRecipeDetailError(null);
    setIsRecipeLoading(false);
    setDetailMode('create');
    setIsRecipeReadMode(false);
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

  function startEditingRecipe() {
    if (pendingDetailAction || isRecipeLoading || detailMode !== 'saved' || !!recipeArchivedAt) {
      return;
    }
    setRecipeDetailError(null);
    setIsRecipeReadMode(false);
  }

  function showActiveRecipes() {
    setListMode('active');
  }

  function showArchivedRecipes() {
    setListMode('archived');
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

      if (saved.archivedAt) {
        setArchivedRecipes((current) => {
          const others = (current ?? []).filter((entry) => entry.recipeId !== saved.recipeId);
          return [...others, saved];
        });
      } else {
        setActiveRecipes((current) => {
          const others = (current ?? []).filter((entry) => entry.recipeId !== saved.recipeId);
          return [...others, saved];
        });
        setArchivedRecipes((current) => (current ?? []).filter((entry) => entry.recipeId !== saved.recipeId));
      }
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
      setActiveRecipes((current) => (current ?? []).filter((entry) => entry.recipeId !== archived.recipeId));
      setArchivedRecipes((current) => {
        const others = (current ?? []).filter((entry) => entry.recipeId !== archived.recipeId);
        return [...others, archived];
      });
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

  async function restoreCurrentRecipe() {
    if (!recipeId || pendingDetailAction || isRecipeLoading) {
      return;
    }

    setPendingDetailAction('archive');
    setRecipeDetailError(null);
    try {
      const restored = await restoreRecipe(recipeId, { token });
      setArchivedRecipes((current) => (current ?? []).filter((entry) => entry.recipeId !== restored.recipeId));
      setActiveRecipes((current) => {
        const others = (current ?? []).filter((entry) => entry.recipeId !== restored.recipeId);
        return [...others, restored];
      });
      setHasLoaded(true);
      setListMode('active');
      applyRecipe(restored);
    } catch (err) {
      await handleApiError(err);
      setRecipeDetailError(formatApiError(err));
    } finally {
      setPendingDetailAction(null);
    }
  }

  async function deleteCurrentRecipe() {
    if (!recipeId || pendingDetailAction || isRecipeLoading || !recipeArchivedAt || !recipeDeleteEligible) {
      return;
    }

    setPendingDetailAction('delete');
    setRecipeDetailError(null);
    try {
      await deleteRecipe(recipeId, { token });
      setArchivedRecipes((current) => (current ?? []).filter((entry) => entry.recipeId !== recipeId));
      setHasLoaded(true);
      setIsRecipeDetailOpen(false);
      setRecipeId(null);
      setRecipeDeleteEligible(false);
      setRecipeDeleteBlockedReason(null);
    } catch (err) {
      await handleApiError(err);
      if (err instanceof ApiError && err.status === 409) {
        try {
          const payload = JSON.parse(err.body) as { code?: string; message?: string };
          if (payload.code === 'RECIPE_DELETE_BLOCKED') {
            setRecipeDeleteEligible(false);
            setRecipeDeleteBlockedReason(payload.message ?? 'This recipe cannot be deleted yet.');
            return;
          }
        } catch {
          // fall through to generic error handling below
        }
      }
      setRecipeDetailError(formatApiError(err));
    } finally {
      setPendingDetailAction(null);
    }
  }

  return {
    recipes: {
      items: recipeListItems,
      listMode,
      activeCount: activeRecipes?.length ?? 0,
      archivedCount: archivedRecipes?.length ?? 0,
      isInitialLoading: !hasLoaded && isListLoading,
      isRefreshing,
      error,
      hasLoaded,
      reload: () => loadRecipes({ refreshing: true }),
      showActiveRecipes,
      showArchivedRecipes,
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
      recipeArchivedAt,
      recipeDeleteEligible,
      recipeDeleteBlockedReason,
      ingredientRows,
      isRecipeLoading,
      hasExistingRecipe: !!recipeId,
      isImportDraft: detailMode === 'import',
      isArchivedRecipe: !!recipeArchivedAt,
      isReadMode: detailMode === 'saved' && isRecipeReadMode,
      canEnterEditMode: detailMode === 'saved' && isRecipeReadMode && !recipeArchivedAt,
      canArchiveRecipe: !!recipeId && detailMode === 'saved' && !recipeArchivedAt,
      canRestoreRecipe: !!recipeId && detailMode === 'saved' && !!recipeArchivedAt,
      canDeleteRecipe: !!recipeId && detailMode === 'saved' && !!recipeArchivedAt && recipeDeleteEligible,
      showDeleteRecipeAction: !!recipeId && detailMode === 'saved' && !!recipeArchivedAt,
      hasIngredients,
      error: recipeDetailError,
      isSavingRecipe: pendingDetailAction === 'save',
      isArchivingRecipe: pendingDetailAction === 'archive',
      isDeletingRecipe: pendingDetailAction === 'delete',
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
      startEditingRecipe,
      saveRecipe,
      archiveCurrentRecipe,
      restoreCurrentRecipe,
      deleteCurrentRecipe,
      closeRecipeDetail,
    },
  };
}
