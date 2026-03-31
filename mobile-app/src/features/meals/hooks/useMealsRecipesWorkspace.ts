import * as Clipboard from 'expo-clipboard';
import { useEffect, useMemo, useState } from 'react';
import { ApiError, formatApiError } from '../../../shared/api/client';
import { useAuth } from '../../../shared/auth/AuthContext';
import {
  acceptRecipeDraft,
  archiveRecipeDetail,
  clearRecipeDetailMakeSoon,
  createManualRecipeDraft,
  createRecipeDraftFromAsset,
  createRecipeDraftFromText,
  createRecipeDraftFromUrl,
  stageRecipeDocumentAsset,
  deleteRecipe,
  getRecipeChoiceSupportMemory,
  getRecipeDraftDuplicateAssessment,
  getRecipeDetail,
  listRecipeLibraryItems,
  listRecentRecipeLibraryItems,
  markRecipeDetailMakeSoon,
  restoreRecipeDetail,
  updateRecipeDraft,
  updateRecipeDetail,
  type RecipeDetailResponse,
  type RecipeDraftResponse,
  type RecipeDuplicateAssessmentResponse,
  type RecipeLibraryItemResponse,
  type RecipeUsageSummaryResponse,
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
import type { RecipeDuplicateCandidate } from '../utils/recipeDuplicateGuard';

type DetailPendingAction = 'save' | 'archive' | 'delete' | 'make-soon' | null;
type RecipeDetailMode = 'create' | 'saved' | 'import';
type RecipeListMode = 'active' | 'archived';
type RecipeBrowseMode = 'all' | 'makeSoon' | 'recent';
type RecipeListItem = {
  recipeId: string;
  name: string;
  sourceName: string | null;
  ingredientCount: number;
  duplicateNameCount: number;
  similarNameCount: number;
  identitySummary: string | null;
  archivedAt: string | null;
  lifecycleState: 'active' | 'archived';
  makeSoonAt: string | null;
  searchText: string;
};

function normalizeRecipeSearchText(value: string | null | undefined) {
  return (value ?? '')
    .trim()
    .toLocaleLowerCase()
    .replace(/[^\p{L}\p{N}\s]/gu, ' ')
    .replace(/\s+/g, ' ')
    .trim();
}

function normalizeRecipeTitle(value: string) {
  return normalizeRecipeSearchText(value);
}

function summarizeRecipeSource(
  sourceName: string | null | undefined,
  sourceUrl: string | null | undefined
) {
  const normalizedSourceName = sourceName?.trim() ?? '';
  if (normalizedSourceName.length > 0) {
    return normalizedSourceName;
  }

  const normalizedSourceUrl = sourceUrl?.trim() ?? '';
  if (normalizedSourceUrl.length === 0) {
    return null;
  }

  try {
    const parsed = new URL(normalizedSourceUrl);
    const hostname = parsed.hostname.replace(/^www\./i, '');
    return hostname.length > 0 ? hostname : normalizedSourceUrl;
  } catch {
    return normalizedSourceUrl;
  }
}

function buildTitleFamilyKey(name: string) {
  const tokens = normalizeRecipeTitle(name)
    .split(' ')
    .filter((token) => token.length > 1)
    .filter((token) => !['and', 'med', 'och', 'with'].includes(token));
  if (tokens.length < 2) {
    return tokens[0] ?? null;
  }
  return `${tokens[0]} ${tokens[1]}`;
}

function normalizeImportUrlCandidate(value: string) {
  const trimmed = value.trim();
  if (trimmed.length === 0) {
    return null;
  }

  try {
    const parsed = new URL(trimmed);
    const isHttp = parsed.protocol === 'http:' || parsed.protocol === 'https:';
    const hasLikelyPagePath = parsed.pathname.trim().length > 1 || parsed.search.trim().length > 0;
    if (!isHttp || !parsed.hostname.includes('.') || !hasLikelyPagePath) {
      return null;
    }
    return parsed.toString();
  } catch {
    return null;
  }
}

function getAssetEntryLabel(assetKind: RecipeAssetImport['assetKind'], options?: { sharedEntry?: boolean }) {
  if (options?.sharedEntry) {
    return assetKind === 'IMAGE' ? 'shared photo' : 'shared file';
  }
  return assetKind === 'IMAGE' ? 'photo' : 'file';
}

async function loadDocumentPickerModule() {
  try {
    return await import('expo-document-picker');
  } catch {
    return null;
  }
}

async function loadImagePickerModule() {
  try {
    return await import('expo-image-picker');
  } catch {
    return null;
  }
}

function toDuplicateCandidate(
  assessment: RecipeDuplicateAssessmentResponse
): RecipeDuplicateCandidate | null {
  if (!assessment.attentionRequired || !assessment.matchingRecipe) {
    return null;
  }

  return {
    recipeId: assessment.matchingRecipe.recipeId,
    name: assessment.matchingRecipe.name,
    sourceName: assessment.matchingRecipe.source.sourceName ?? null,
    sourceUrl: assessment.matchingRecipe.source.sourceUrl ?? null,
    archivedAt: assessment.matchingRecipe.lifecycle.state === 'archived'
      ? 'archived'
      : null,
    matchType: assessment.matchType === 'exact_source_url' ? 'source-url' : 'name-and-source',
  };
}

type Params = {
  token: string;
  enabled: boolean;
};

type RecipeImportCallbacks = {
  onError?: (message: string) => void;
  onComplete?: () => void;
};

type RecipeAssetImport = {
  assetKind: 'DOCUMENT' | 'IMAGE';
  referenceId: string;
  sourceLabel?: string | null;
  originalFilename?: string | null;
  mimeType?: string | null;
};

export function useMealsRecipesWorkspace({ token, enabled }: Params) {
  const { handleApiError } = useAuth();
  const [activeRecipes, setActiveRecipes] = useState<RecipeLibraryItemResponse[] | null>(null);
  const [archivedRecipes, setArchivedRecipes] = useState<RecipeLibraryItemResponse[] | null>(null);
  const [isListLoading, setIsListLoading] = useState(false);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [hasLoaded, setHasLoaded] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [listMode, setListMode] = useState<RecipeListMode>('active');
  const [browseMode, setBrowseMode] = useState<RecipeBrowseMode>('all');
  const [recipeSearchQuery, setRecipeSearchQuery] = useState('');
  const [recentlyUsedRecipes, setRecentlyUsedRecipes] = useState<RecipeLibraryItemResponse[] | null>(null);

  const [isRecipeDetailOpen, setIsRecipeDetailOpen] = useState(false);
  const [recipeDraftId, setRecipeDraftId] = useState<string | null>(null);
  const [recipeDraftState, setRecipeDraftState] = useState<string | null>(null);
  const [recipeId, setRecipeId] = useState<string | null>(null);
  const [recipeTitle, setRecipeTitle] = useState('');
  const [recipeSource, setRecipeSource] = useState('');
  const [recipeSourceUrl, setRecipeSourceUrl] = useState('');
  const [recipeOriginKind, setRecipeOriginKind] = useState('MANUAL');
  const [recipeServings, setRecipeServings] = useState('');
  const [recipeMakeSoonAt, setRecipeMakeSoonAt] = useState<string | null>(null);
  const [recipeShortNote, setRecipeShortNote] = useState('');
  const [recipeInstructions, setRecipeInstructions] = useState('');
  const [recipeLifecycleState, setRecipeLifecycleState] = useState<'active' | 'archived' | null>(null);
  const [recipeDeleteEligible, setRecipeDeleteEligible] = useState(false);
  const [recipeDeleteBlockedReason, setRecipeDeleteBlockedReason] = useState<string | null>(null);
  const [ingredientRows, setIngredientRows] = useState<MealIngredientRow[]>([]);
  const [isRecipeLoading, setIsRecipeLoading] = useState(false);
  const [recipeDetailError, setRecipeDetailError] = useState<string | null>(null);
  const [pendingDetailAction, setPendingDetailAction] = useState<DetailPendingAction>(null);
  const [detailMode, setDetailMode] = useState<RecipeDetailMode>('create');
  const [isRecipeReadMode, setIsRecipeReadMode] = useState(false);
  const [pendingDuplicateCandidate, setPendingDuplicateCandidate] = useState<RecipeDuplicateCandidate | null>(null);
  const [recipeMemory, setRecipeMemory] = useState<RecipeUsageSummaryResponse | null>(null);
  const [isRecipeMemoryLoading, setIsRecipeMemoryLoading] = useState(false);

  const [isImportSheetOpen, setIsImportSheetOpen] = useState(false);
  const [importUrl, setImportUrl] = useState('');
  const [importError, setImportError] = useState<string | null>(null);
  const [isImportingDraft, setIsImportingDraft] = useState(false);
  const [clipboardImportUrl, setClipboardImportUrl] = useState<string | null>(null);
  const [isAddRecipeSheetOpen, setIsAddRecipeSheetOpen] = useState(false);
  const [isCreateRecipeSheetOpen, setIsCreateRecipeSheetOpen] = useState(false);
  const [isImportRecipeSheetOpen, setIsImportRecipeSheetOpen] = useState(false);
  const [isTextImportSheetOpen, setIsTextImportSheetOpen] = useState(false);
  const [importText, setImportText] = useState('');
  const [importTextError, setImportTextError] = useState<string | null>(null);
  const [isImportingTextDraft, setIsImportingTextDraft] = useState(false);
  const [isSelectingImportDocument, setIsSelectingImportDocument] = useState(false);
  const [isSelectingImportImage, setIsSelectingImportImage] = useState(false);

  const hasIngredients = useMemo(
    () => toIngredientRequests(ingredientRows).length > 0,
    [ingredientRows]
  );

  function focusMainRecipeLibrary() {
    setListMode('active');
    setBrowseMode('all');
    setRecipeSearchQuery('');
  }

  function toRecipeListItems(
    recipes: RecipeLibraryItemResponse[],
    options?: { preserveOrder?: boolean },
  ): RecipeListItem[] {
    const nameCounts = new Map<string, number>();
    const titleFamilyCounts = new Map<string, number>();

    for (const recipe of recipes) {
      const normalizedName = normalizeRecipeTitle(recipe.name);
      nameCounts.set(normalizedName, (nameCounts.get(normalizedName) ?? 0) + 1);

      const titleFamilyKey = buildTitleFamilyKey(recipe.name);
      if (titleFamilyKey) {
        titleFamilyCounts.set(titleFamilyKey, (titleFamilyCounts.get(titleFamilyKey) ?? 0) + 1);
      }
    }

    function buildIdentitySummary(recipe: RecipeLibraryItemResponse) {
      return summarizeRecipeSource(recipe.source.sourceName, recipe.source.sourceUrl);
    }

    const orderedRecipes = options?.preserveOrder
      ? [...recipes]
      : [...recipes].sort((left, right) => {
          const nameComparison = left.name.localeCompare(right.name);
          if (nameComparison !== 0) {
            return nameComparison;
          }
          const sourceComparison = (left.source.sourceName ?? '').localeCompare(right.source.sourceName ?? '');
          if (sourceComparison !== 0) {
            return sourceComparison;
          }
          return right.updatedAt.localeCompare(left.updatedAt);
        });

    return orderedRecipes
      .map((recipe) => ({
        recipeId: recipe.recipeId,
        name: recipe.name,
        sourceName: recipe.source.sourceName ?? null,
        ingredientCount: recipe.ingredientCount,
        duplicateNameCount: nameCounts.get(normalizeRecipeTitle(recipe.name)) ?? 1,
        similarNameCount: titleFamilyCounts.get(buildTitleFamilyKey(recipe.name) ?? '') ?? 1,
        identitySummary: buildIdentitySummary(recipe),
        archivedAt: recipe.lifecycle.state === 'archived' ? recipe.updatedAt : null,
        lifecycleState: recipe.lifecycle.state === 'archived' ? 'archived' : 'active',
        makeSoonAt: recipe.makeSoonAt,
        searchText: normalizeRecipeSearchText([
          recipe.name,
          recipe.source.sourceName,
          recipe.source.sourceUrl,
          buildIdentitySummary(recipe),
        ].filter(Boolean).join(' ')),
      }));
  }

  const activeRecipeListItems = useMemo(
    () => toRecipeListItems(activeRecipes ?? []),
    [activeRecipes]
  );
  const archivedRecipeListItems = useMemo(
    () => toRecipeListItems(archivedRecipes ?? []),
    [archivedRecipes]
  );
  const recentlyUsedRecipeItems = useMemo(
    () => {
      const makeSoonRecipeIds = new Set(
        (activeRecipes ?? [])
          .filter((recipe) => recipe.makeSoonAt != null)
          .map((recipe) => recipe.recipeId)
      );

      return toRecipeListItems(
        (recentlyUsedRecipes ?? [])
          .filter((recipe) => !makeSoonRecipeIds.has(recipe.recipeId))
          ,
        { preserveOrder: true }
      );
    },
    [activeRecipes, recentlyUsedRecipes]
  );
  const makeSoonRecipeItems = useMemo(
    () => toRecipeListItems(
      (activeRecipes ?? [])
        .filter((recipe) => recipe.makeSoonAt != null)
        .sort((left, right) => (right.makeSoonAt ?? '').localeCompare(left.makeSoonAt ?? '')),
      { preserveOrder: true }
    ),
    [activeRecipes]
  );
  const visibleRecipeListItems = useMemo(
    () => {
      if (listMode === 'archived') {
        return archivedRecipeListItems;
      }
      if (browseMode === 'makeSoon') {
        return makeSoonRecipeItems;
      }
      if (browseMode === 'recent') {
        return recentlyUsedRecipeItems;
      }
      return activeRecipeListItems;
    },
    [
      activeRecipeListItems,
      archivedRecipeListItems,
      browseMode,
      listMode,
      makeSoonRecipeItems,
      recentlyUsedRecipeItems,
    ]
  );

  const filteredRecipeListItems = useMemo(() => {
    const normalizedQuery = normalizeRecipeSearchText(recipeSearchQuery);
    if (normalizedQuery.length === 0) {
      return visibleRecipeListItems;
    }

    function getMatchScore(recipe: RecipeListItem) {
      const title = normalizeRecipeTitle(recipe.name);
      const source = normalizeRecipeSearchText(recipe.sourceName);
      const summary = normalizeRecipeSearchText(recipe.identitySummary);
      if (title === normalizedQuery) {
        return 0;
      }
      if (title.startsWith(normalizedQuery)) {
        return 1;
      }
      if (title.split(' ').some((token) => token.startsWith(normalizedQuery))) {
        return 2;
      }
      if (source.startsWith(normalizedQuery)) {
        return 3;
      }
      if (source.includes(normalizedQuery)) {
        return 4;
      }
      if (summary.includes(normalizedQuery)) {
        return 5;
      }
      if (recipe.searchText.includes(normalizedQuery)) {
        return 6;
      }
      return null;
    }

    return visibleRecipeListItems
      .map((recipe) => ({ recipe, score: getMatchScore(recipe) }))
      .filter((entry): entry is { recipe: RecipeListItem; score: number } => entry.score != null)
      .sort((left, right) => {
        if (left.score !== right.score) {
          return left.score - right.score;
        }
        const nameComparison = left.recipe.name.localeCompare(right.recipe.name);
        if (nameComparison !== 0) {
          return nameComparison;
        }
        return (left.recipe.sourceName ?? '').localeCompare(right.recipe.sourceName ?? '');
      })
      .map((entry) => entry.recipe);
  }, [visibleRecipeListItems, recipeSearchQuery]);

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
      const [nextActiveRecipes, nextArchivedRecipes, nextRecentlyUsedRecipes] = await Promise.all([
        listRecipeLibraryItems('active', { token }),
        listRecipeLibraryItems('archived', { token }),
        listRecentRecipeLibraryItems({ token }),
      ]);
      setActiveRecipes(nextActiveRecipes);
      setArchivedRecipes(nextArchivedRecipes);
      setRecentlyUsedRecipes(nextRecentlyUsedRecipes);
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
              return 'We could not find a usable recipe on that page. Try another link or create the recipe yourself.';
            }
            if (message.includes('missing ingredients')) {
              return 'We found the page, but it was too incomplete to review. Try another link or finish the recipe yourself.';
            }
            return 'We could not import that recipe page yet. Try another link or create the recipe yourself.';
          }
        } catch {
          return 'We could not import that recipe page yet. Try another link or create the recipe yourself.';
        }
      }
    }
    return formatApiError(err);
  }

  function formatTextImportDraftError(err: unknown): string {
    if (err instanceof ApiError) {
      if (err.status === 400) {
        return 'Paste the recipe text you want to review first.';
      }
      if (err.status === 422) {
        return 'Paste a little more of the recipe so you have something useful to review.';
      }
    }
    return formatApiError(err);
  }

  function formatAssetImportDraftError(
    err: unknown,
    assetKind: RecipeAssetImport['assetKind'],
    options?: { sharedEntry?: boolean }
  ): string {
    const entryLabel = getAssetEntryLabel(assetKind, options);
    if (err instanceof ApiError) {
      if (err.status === 400) {
        return `We could not use that ${entryLabel}. Try again.`;
      }
      if (err.status === 422) {
        return `We could not turn that ${entryLabel} into a recipe yet.`;
      }
      if (err.status === 503) {
        try {
          const payload = JSON.parse(err.body) as { code?: string };
          if (payload.code === 'RECIPE_ASSET_INTAKE_UNAVAILABLE') {
            return assetKind === 'IMAGE'
              ? `${options?.sharedEntry ? 'Shared photo import' : 'Photo import'} is not available yet.`
              : `${options?.sharedEntry ? 'Shared file import' : 'File import'} is not available yet.`;
          }
        } catch {
          return assetKind === 'IMAGE'
            ? `${options?.sharedEntry ? 'Shared photo import' : 'Photo import'} is not available yet.`
            : `${options?.sharedEntry ? 'Shared file import' : 'File import'} is not available yet.`;
        }
      }
    }
    return formatApiError(err);
  }

  function formatDocumentImportSelectionError() {
    return 'We could not open that file. Try another one or try again.';
  }

  function formatDocumentImportUploadError(err: unknown) {
    if (err instanceof ApiError) {
      if (err.status === 400) {
        return 'That file is not supported here yet. Try a recipe PDF or document.';
      }
      if (err.status === 413) {
        return 'That file is too large for recipe import. Try a smaller PDF or document.';
      }
      if (err.status === 422) {
        return 'We could not use that file for recipe capture yet. Try another recipe PDF or document.';
      }
    }
    return formatApiError(err);
  }

  function formatImageImportSelectionError() {
    return 'We could not use that photo. Try another one or try again.';
  }

  function formatMissingNativeAssetPickerError(assetKind: RecipeAssetImport['assetKind']) {
    return assetKind === 'IMAGE'
      ? 'Photo import is not available in this app build yet. Rebuild the app and try again.'
      : 'File import is not available in this app build yet. Rebuild the app and try again.';
  }

  function isSupportedRecipeDocumentMimeType(mimeType: string | null | undefined) {
    if (!mimeType) {
      return true;
    }

    const normalized = mimeType.trim().toLowerCase();
    if (normalized.length === 0) {
      return true;
    }
    if (normalized.startsWith('image/') || normalized.startsWith('audio/') || normalized.startsWith('video/')) {
      return false;
    }

    return normalized === 'application/pdf'
      || normalized.startsWith('application/')
      || normalized.startsWith('text/');
  }

  function isSupportedRecipeImageMimeType(mimeType: string | null | undefined) {
    if (!mimeType) {
      return true;
    }

    const normalized = mimeType.trim().toLowerCase();
    if (normalized.length === 0) {
      return true;
    }

    return normalized.startsWith('image/');
  }

  function resetRecipeDetailState() {
    setRecipeDraftId(null);
    setRecipeDraftState(null);
    setRecipeId(null);
    setRecipeTitle('');
    setRecipeSource('');
    setRecipeSourceUrl('');
    setRecipeOriginKind('MANUAL');
    setRecipeServings('');
    setRecipeMakeSoonAt(null);
    setRecipeShortNote('');
    setRecipeInstructions('');
    setRecipeLifecycleState(null);
    setRecipeDeleteEligible(false);
    setRecipeDeleteBlockedReason(null);
    setIngredientRows([]);
    setDetailMode('create');
    setIsRecipeReadMode(false);
    setPendingDuplicateCandidate(null);
    setRecipeMemory(null);
    setIsRecipeMemoryLoading(false);
  }

  function applyRecipeDraft(draft: RecipeDraftResponse, mode: RecipeDetailMode) {
    setRecipeDraftId(draft.draftId);
    setRecipeDraftState(draft.state);
    setRecipeId(null);
    setRecipeTitle(draft.name ?? '');
    setRecipeSource(draft.source.sourceName ?? '');
    setRecipeSourceUrl(draft.source.sourceUrl ?? '');
    setRecipeOriginKind(draft.provenance.originKind?.toUpperCase() ?? 'MANUAL');
    setRecipeServings(draft.servings ?? '');
    setRecipeMakeSoonAt(null);
    setRecipeShortNote(draft.shortNote ?? '');
    setRecipeInstructions(draft.instructions ?? '');
    setRecipeLifecycleState(null);
    setRecipeDeleteEligible(false);
    setRecipeDeleteBlockedReason(null);
    setIngredientRows(ingredientRowsFromResponse(draft.ingredients));
    setRecipeDetailError(null);
    setIsRecipeLoading(false);
    setDetailMode(mode);
    setIsRecipeReadMode(false);
    setIsRecipeDetailOpen(true);
  }

  function applyRecipeDetail(detail: RecipeDetailResponse) {
    setRecipeDraftId(null);
    setRecipeDraftState(null);
    setRecipeId(detail.recipeId);
    setRecipeTitle(detail.name);
    setRecipeSource(detail.source.sourceName ?? '');
    setRecipeSourceUrl(detail.source.sourceUrl ?? '');
    setRecipeOriginKind(detail.provenance.originKind?.toUpperCase() ?? 'MANUAL');
    setRecipeServings(detail.servings ?? '');
    setRecipeMakeSoonAt(detail.makeSoonAt);
    setRecipeShortNote(detail.shortNote ?? '');
    setRecipeInstructions(detail.instructions ?? '');
    setRecipeLifecycleState(detail.lifecycle.state === 'archived' ? 'archived' : 'active');
    setRecipeDeleteEligible(detail.lifecycle.deleteEligible);
    setRecipeDeleteBlockedReason(detail.lifecycle.deleteBlockedReason);
    setIngredientRows(ingredientRowsFromResponse(detail.ingredients));
    setRecipeDetailError(null);
    setIsRecipeLoading(false);
    setDetailMode('saved');
    setIsRecipeReadMode(true);
    setIsRecipeDetailOpen(true);
  }

  async function refreshRecipeCollections() {
    if (!token) {
      return;
    }
    const [nextActiveRecipes, nextArchivedRecipes, nextRecentlyUsedRecipes] = await Promise.all([
      listRecipeLibraryItems('active', { token }),
      listRecipeLibraryItems('archived', { token }),
      listRecentRecipeLibraryItems({ token }),
    ]);
    setActiveRecipes(nextActiveRecipes);
    setArchivedRecipes(nextArchivedRecipes);
    setRecentlyUsedRecipes(nextRecentlyUsedRecipes);
    setHasLoaded(true);
  }

  async function openRecipe(recipeIdToOpen: string) {
    resetRecipeDetailState();
    setRecipeDetailError(null);
    setIsRecipeDetailOpen(true);
    setIsRecipeLoading(true);
    try {
      const recipe = await getRecipeDetail(recipeIdToOpen, { token });
      applyRecipeDetail(recipe);
    } catch (err) {
      await handleApiError(err);
      setRecipeDetailError(formatApiError(err));
      setIsRecipeLoading(false);
    }
  }

  useEffect(() => {
    let cancelled = false;

    if (!token || !recipeId || detailMode !== 'saved' || recipeLifecycleState !== 'active' || !isRecipeReadMode) {
      setRecipeMemory(null);
      setIsRecipeMemoryLoading(false);
      return () => {
        cancelled = true;
      };
    }

    setIsRecipeMemoryLoading(true);
    void getRecipeChoiceSupportMemory(recipeId, { token })
      .then((memory) => {
        if (cancelled) {
          return;
        }
        setRecipeMemory(memory);
      })
      .catch(async (err) => {
        await handleApiError(err);
        if (cancelled) {
          return;
        }
        setRecipeMemory(null);
      })
      .finally(() => {
        if (!cancelled) {
          setIsRecipeMemoryLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [detailMode, handleApiError, isRecipeReadMode, recipeId, recipeLifecycleState, token]);

  async function openCreateRecipe() {
    if (pendingDetailAction || isRecipeLoading) {
      return;
    }
    setIsAddRecipeSheetOpen(false);
    setIsCreateRecipeSheetOpen(false);
    setIsImportRecipeSheetOpen(false);
    setIsImportSheetOpen(false);
    setIsTextImportSheetOpen(false);
    setImportError(null);
    setImportTextError(null);
    focusMainRecipeLibrary();
    resetRecipeDetailState();
    setRecipeDetailError(null);
    setIsRecipeLoading(true);
    setIsRecipeDetailOpen(true);
    try {
      const draft = await createManualRecipeDraft({ token });
      applyRecipeDraft(draft, 'create');
      if (draft.ingredients.length === 0) {
        setIngredientRows([createEmptyIngredientRow()]);
      }
    } catch (err) {
      await handleApiError(err);
      setRecipeDetailError(formatApiError(err));
      setIsRecipeLoading(false);
    }
  }

  function openAddRecipe() {
    if (
      pendingDetailAction
      || isRecipeLoading
      || isImportingDraft
      || isImportingTextDraft
      || isSelectingImportDocument
      || isSelectingImportImage
    ) {
      return;
    }
    focusMainRecipeLibrary();
    setImportError(null);
    setImportTextError(null);
    setIsAddRecipeSheetOpen(true);
    setIsCreateRecipeSheetOpen(false);
    setIsImportRecipeSheetOpen(false);
  }

  function closeAddRecipe() {
    if (
      isImportingDraft
      || isImportingTextDraft
      || isRecipeLoading
      || isSelectingImportDocument
      || isSelectingImportImage
    ) {
      return;
    }
    setIsAddRecipeSheetOpen(false);
    setIsCreateRecipeSheetOpen(false);
    setIsImportRecipeSheetOpen(false);
  }

  function openCreateRecipeOptions() {
    if (
      isImportingTextDraft
      || pendingDetailAction
      || isRecipeLoading
      || isSelectingImportDocument
      || isSelectingImportImage
    ) {
      return;
    }
    focusMainRecipeLibrary();
    setIsAddRecipeSheetOpen(false);
    setIsImportSheetOpen(false);
    setImportError(null);
    setImportTextError(null);
    setIsImportRecipeSheetOpen(false);
    setIsCreateRecipeSheetOpen(true);
  }

  function closeCreateRecipeOptions() {
    if (isImportingTextDraft || isRecipeLoading || isSelectingImportDocument || isSelectingImportImage) {
      return;
    }
    setIsCreateRecipeSheetOpen(false);
    setImportTextError(null);
    setIsAddRecipeSheetOpen(true);
  }

  function openImportRecipeOptions() {
    if (
      pendingDetailAction
      || isRecipeLoading
      || isImportingTextDraft
      || isSelectingImportDocument
      || isSelectingImportImage
    ) {
      return;
    }
    focusMainRecipeLibrary();
    setIsAddRecipeSheetOpen(false);
    setIsImportSheetOpen(false);
    setImportError(null);
    setImportTextError(null);
    setIsCreateRecipeSheetOpen(false);
    setIsImportRecipeSheetOpen(true);
  }

  function closeImportRecipeOptions() {
    if (isRecipeLoading || isSelectingImportDocument || isSelectingImportImage) {
      return;
    }
    setIsImportRecipeSheetOpen(false);
    setIsAddRecipeSheetOpen(true);
  }

  async function hydrateImportUrlFromClipboard() {
    try {
      const clipboardValue = await Clipboard.getStringAsync();
      const normalizedUrl = normalizeImportUrlCandidate(clipboardValue);
      if (!normalizedUrl) {
        setClipboardImportUrl(null);
        return;
      }
      setClipboardImportUrl(normalizedUrl);
      setImportUrl((current) => (current.trim().length === 0 ? normalizedUrl : current));
    } catch {
      setClipboardImportUrl(null);
    }
  }

  async function importSharedRecipeUrl(
    sharedUrl: string,
    callbacks?: RecipeImportCallbacks
  ) {
    if (
      isRecipeLoading
      || pendingDetailAction
      || isImportingDraft
      || isImportingTextDraft
      || isSelectingImportDocument
      || isSelectingImportImage
    ) {
      callbacks?.onError?.('Finish the current recipe first, then try sharing the link again.');
      callbacks?.onComplete?.();
      return;
    }

    const normalizedUrl = normalizeImportUrlCandidate(sharedUrl);
    if (!normalizedUrl) {
      callbacks?.onError?.('We could not use that shared link. Try sharing a full recipe page link.');
      callbacks?.onComplete?.();
      return;
    }

    focusMainRecipeLibrary();
    setIsAddRecipeSheetOpen(false);
    setIsCreateRecipeSheetOpen(false);
    setIsImportRecipeSheetOpen(false);
    setIsImportSheetOpen(false);
    setIsTextImportSheetOpen(false);
    setImportError(null);
    setImportTextError(null);
    resetRecipeDetailState();
    setRecipeDetailError(null);
    setIsRecipeLoading(true);
    setIsRecipeDetailOpen(true);

    try {
      const draft = await createRecipeDraftFromUrl(
        { url: normalizedUrl },
        { token }
      );
      applyRecipeDraft(draft, 'import');
      callbacks?.onComplete?.();
    } catch (err) {
      await handleApiError(err);
      setIsRecipeDetailOpen(false);
      setIsRecipeLoading(false);
      setRecipeDetailError(null);
      resetRecipeDetailState();
      callbacks?.onError?.(formatImportDraftError(err));
      callbacks?.onComplete?.();
    }
  }

  async function importRecipeAsset(
    asset: RecipeAssetImport,
    callbacks?: RecipeImportCallbacks,
    options?: { sharedEntry?: boolean }
  ) {
    if (
      isRecipeLoading
      || pendingDetailAction
      || isImportingDraft
      || isImportingTextDraft
      || isSelectingImportDocument
      || isSelectingImportImage
    ) {
      callbacks?.onError?.(
        options?.sharedEntry
          ? `Finish the current recipe first, then try sharing the ${asset.assetKind === 'IMAGE' ? 'photo' : 'file'} again.`
          : `Finish the current recipe first, then try importing the ${asset.assetKind === 'IMAGE' ? 'photo' : 'file'} again.`
      );
      callbacks?.onComplete?.();
      return;
    }

    if (!asset.referenceId.trim()) {
      callbacks?.onError?.(
        `We could not use that ${getAssetEntryLabel(asset.assetKind, options)}. Try again.`
      );
      callbacks?.onComplete?.();
      return;
    }

    focusMainRecipeLibrary();
    setIsAddRecipeSheetOpen(false);
    setIsCreateRecipeSheetOpen(false);
    setIsImportRecipeSheetOpen(false);
    setIsImportSheetOpen(false);
    setIsTextImportSheetOpen(false);
    setImportError(null);
    setImportTextError(null);
    resetRecipeDetailState();
    setRecipeDetailError(null);
    setIsRecipeLoading(true);
    setIsRecipeDetailOpen(true);

    try {
      const draft = await createRecipeDraftFromAsset(
        {
          assetKind: asset.assetKind === 'IMAGE' ? 'image' : 'document',
          referenceId: asset.referenceId.trim(),
          sourceLabel: asset.sourceLabel?.trim() || null,
          originalFilename: asset.originalFilename?.trim() || null,
          mimeType: asset.mimeType?.trim() || null,
        },
        { token }
      );
      applyRecipeDraft(draft, 'import');
      callbacks?.onComplete?.();
    } catch (err) {
      await handleApiError(err);
      setIsRecipeDetailOpen(false);
      setIsRecipeLoading(false);
      setRecipeDetailError(null);
      resetRecipeDetailState();
      callbacks?.onError?.(formatAssetImportDraftError(err, asset.assetKind, options));
      callbacks?.onComplete?.();
    }
  }

  async function importSharedRecipeAsset(
    asset: RecipeAssetImport,
    callbacks?: RecipeImportCallbacks
  ) {
    await importRecipeAsset(asset, callbacks, { sharedEntry: true });
  }

  function openImportRecipe() {
    if (isImportingDraft || pendingDetailAction) {
      return;
    }
    focusMainRecipeLibrary();
    setIsAddRecipeSheetOpen(false);
    setIsCreateRecipeSheetOpen(false);
    setIsImportRecipeSheetOpen(false);
    setIsTextImportSheetOpen(false);
    setImportUrl('');
    setImportError(null);
    setClipboardImportUrl(null);
    setIsImportSheetOpen(true);
    void hydrateImportUrlFromClipboard();
  }

  function closeImportRecipe() {
    if (isImportingDraft) {
      return;
    }
    setIsImportSheetOpen(false);
    setImportError(null);
    setClipboardImportUrl(null);
    setIsAddRecipeSheetOpen(true);
  }

  function openImportRecipeText() {
    if (isImportingTextDraft || pendingDetailAction) {
      return;
    }
    focusMainRecipeLibrary();
    setIsAddRecipeSheetOpen(false);
    setIsCreateRecipeSheetOpen(false);
    setIsImportRecipeSheetOpen(false);
    setIsImportSheetOpen(false);
    setImportText('');
    setImportTextError(null);
    setIsTextImportSheetOpen(true);
  }

  function closeImportRecipeText() {
    if (isImportingTextDraft) {
      return;
    }
    setIsTextImportSheetOpen(false);
    setImportTextError(null);
    setIsCreateRecipeSheetOpen(true);
  }

  function closeRecipeDetail() {
    if (pendingDetailAction) {
      return;
    }
    setIsRecipeDetailOpen(false);
    setRecipeDetailError(null);
    resetRecipeDetailState();
  }

  function startEditingRecipe() {
    if (pendingDetailAction || isRecipeLoading || detailMode !== 'saved' || recipeLifecycleState === 'archived') {
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

  function showAllBrowseRecipes() {
    setBrowseMode('all');
  }

  function showMakeSoonBrowseRecipes() {
    setBrowseMode('makeSoon');
  }

  function showRecentBrowseRecipes() {
    setBrowseMode('recent');
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
      const draft = await createRecipeDraftFromUrl(
        { url: importUrl.trim() },
        { token }
      );
      focusMainRecipeLibrary();
      applyRecipeDraft(draft, 'import');
      setIsImportSheetOpen(false);
      setImportUrl('');
    } catch (err) {
      await handleApiError(err);
      setImportError(formatImportDraftError(err));
    } finally {
      setIsImportingDraft(false);
    }
  }

  async function importRecipeTextDraft() {
    if (isImportingTextDraft || !importText.trim()) {
      return;
    }

    setIsImportingTextDraft(true);
    setImportTextError(null);
    try {
      const draft = await createRecipeDraftFromText(
        { text: importText.trim() },
        { token }
      );
      focusMainRecipeLibrary();
      applyRecipeDraft(draft, 'import');
      setIsTextImportSheetOpen(false);
      setImportText('');
    } catch (err) {
      await handleApiError(err);
      setImportTextError(formatTextImportDraftError(err));
    } finally {
      setIsImportingTextDraft(false);
    }
  }

  async function pickImportRecipeDocument(
    callbacks?: RecipeImportCallbacks
  ) {
    if (
      isSelectingImportDocument
      || isRecipeLoading
      || pendingDetailAction
      || isImportingDraft
      || isImportingTextDraft
    ) {
      callbacks?.onError?.('Finish the current recipe first, then try importing the file again.');
      callbacks?.onComplete?.();
      return;
    }

    setIsSelectingImportDocument(true);
    try {
      const documentPicker = await loadDocumentPickerModule();
      if (!documentPicker) {
        callbacks?.onError?.(formatMissingNativeAssetPickerError('DOCUMENT'));
        callbacks?.onComplete?.();
        return;
      }

      const result = await documentPicker.getDocumentAsync({
        multiple: false,
        type: ['application/pdf', 'application/*', 'text/*'],
      });
      if (result.canceled) {
        return;
      }

      const asset = result.assets[0];
      const referenceId = asset?.uri?.trim() ?? '';
      if (!referenceId) {
        callbacks?.onError?.(formatDocumentImportSelectionError());
        callbacks?.onComplete?.();
        return;
      }

      const mimeType = asset.mimeType?.trim() || null;
      if (!isSupportedRecipeDocumentMimeType(mimeType)) {
        callbacks?.onError?.('That file is not supported here yet. Try a recipe PDF or document.');
        callbacks?.onComplete?.();
        return;
      }

      const stagedAsset = await stageRecipeDocumentAsset(
        {
          uri: referenceId,
          name: asset.name?.trim() || 'recipe-document',
          mimeType,
        },
        { token }
      );

      await importRecipeAsset(
        {
          assetKind: 'DOCUMENT',
          referenceId: stagedAsset.referenceId,
          sourceLabel: stagedAsset.sourceLabel,
          originalFilename: stagedAsset.originalFilename,
          mimeType: stagedAsset.mimeType,
        },
        callbacks
      );
    } catch (err) {
      callbacks?.onError?.(
        err instanceof ApiError
          ? formatDocumentImportUploadError(err)
          : formatDocumentImportSelectionError()
      );
      callbacks?.onComplete?.();
    } finally {
      setIsSelectingImportDocument(false);
    }
  }

  async function pickImportRecipeImage(
    source: 'camera' | 'library',
    callbacks?: RecipeImportCallbacks
  ) {
    if (
      isSelectingImportImage
      || isSelectingImportDocument
      || isRecipeLoading
      || pendingDetailAction
      || isImportingDraft
      || isImportingTextDraft
    ) {
      callbacks?.onError?.('Finish the current recipe first, then try the photo again.');
      callbacks?.onComplete?.();
      return;
    }

    setIsSelectingImportImage(true);
    setIsAddRecipeSheetOpen(false);
    setIsCreateRecipeSheetOpen(false);

    try {
      const imagePicker = await loadImagePickerModule();
      if (!imagePicker) {
        callbacks?.onError?.(formatMissingNativeAssetPickerError('IMAGE'));
        callbacks?.onComplete?.();
        return;
      }

      const permission = source === 'camera'
        ? await imagePicker.requestCameraPermissionsAsync()
        : await imagePicker.requestMediaLibraryPermissionsAsync();

      if (!permission.granted) {
        callbacks?.onError?.(
          source === 'camera'
            ? 'Allow camera access to take a recipe photo.'
            : 'Allow photo access to choose a recipe photo.'
        );
        callbacks?.onComplete?.();
        return;
      }

      const result = source === 'camera'
        ? await imagePicker.launchCameraAsync({
            mediaTypes: ['images'],
            allowsEditing: false,
            quality: 1,
          })
        : await imagePicker.launchImageLibraryAsync({
            mediaTypes: ['images'],
            allowsMultipleSelection: false,
            allowsEditing: false,
            quality: 1,
          });

      if (result.canceled) {
        return;
      }

      const asset = result.assets[0];
      const referenceId = asset?.uri?.trim() ?? '';
      if (!referenceId) {
        callbacks?.onError?.(formatImageImportSelectionError());
        callbacks?.onComplete?.();
        return;
      }

      if (asset.type === 'video') {
        callbacks?.onError?.('That photo is not supported here yet. Try another recipe photo.');
        callbacks?.onComplete?.();
        return;
      }

      const mimeType = asset.mimeType?.trim() || null;
      if (!isSupportedRecipeImageMimeType(mimeType)) {
        callbacks?.onError?.('That photo is not supported here yet. Try another recipe photo.');
        callbacks?.onComplete?.();
        return;
      }

      await importRecipeAsset(
        {
          assetKind: 'IMAGE',
          referenceId,
          sourceLabel: asset.fileName?.trim() || null,
          originalFilename: asset.fileName?.trim() || null,
          mimeType,
        },
        callbacks,
        { sharedEntry: false }
      );
    } catch {
      callbacks?.onError?.(formatImageImportSelectionError());
      callbacks?.onComplete?.();
    } finally {
      setIsSelectingImportImage(false);
    }
  }

  function buildRecipeSaveRequest() {
    return {
      name: recipeTitle.trim(),
      sourceName: recipeSource.trim() || null,
      sourceUrl: recipeSourceUrl.trim() || null,
      originKind: recipeOriginKind,
      servings: recipeServings.trim() || null,
      shortNote: recipeShortNote.trim() || null,
      instructions: recipeInstructions.trim() || null,
      ingredients: toIngredientRequests(ingredientRows),
    };
  }

  async function saveRecipe() {
    if (pendingDetailAction || isRecipeLoading || !recipeTitle.trim()) {
      return;
    }
    if (!recipeId && !recipeDraftId) {
      setRecipeDetailError('We could not start this recipe draft. Close and try again.');
      return;
    }

    const request = buildRecipeSaveRequest();
    setPendingDetailAction('save');
    setRecipeDetailError(null);
    try {
      if (!recipeId && recipeDraftId) {
        const updatedDraft = await updateRecipeDraft(
          recipeDraftId,
          {
            name: request.name,
            sourceName: request.sourceName,
            sourceUrl: request.sourceUrl,
            servings: request.servings,
            shortNote: request.shortNote,
            instructions: request.instructions,
            markReady: true,
            ingredients: request.ingredients,
          },
          { token }
        );
        setRecipeDraftState(updatedDraft.state);
        const duplicateAssessment = await getRecipeDraftDuplicateAssessment(recipeDraftId, { token });
        const duplicateCandidate = toDuplicateCandidate(duplicateAssessment);
        if (duplicateCandidate) {
          setPendingDuplicateCandidate(duplicateCandidate);
          return;
        }
        const savedDetail = await acceptRecipeDraft(recipeDraftId, { allowDuplicate: false }, { token });
        focusMainRecipeLibrary();
        setPendingDuplicateCandidate(null);
        await refreshRecipeCollections();
        applyRecipeDetail(savedDetail);
      } else {
        const saved = await updateRecipeDetail(recipeId!, request, { token });
        setPendingDuplicateCandidate(null);
        await refreshRecipeCollections();
        applyRecipeDetail(saved);
      }
    } catch (err) {
      await handleApiError(err);
      if (!recipeId && recipeDraftId && err instanceof ApiError && err.status === 409) {
        try {
          const payload = JSON.parse(err.body) as { code?: string };
          if (payload.code === 'RECIPE_DUPLICATE_ATTENTION_REQUIRED') {
            const duplicateAssessment = await getRecipeDraftDuplicateAssessment(recipeDraftId, { token });
            const duplicateCandidate = toDuplicateCandidate(duplicateAssessment);
            if (duplicateCandidate) {
              setPendingDuplicateCandidate(duplicateCandidate);
              return;
            }
          }
        } catch {
          // fall through to generic error handling
        }
      }
      setRecipeDetailError(formatApiError(err));
    } finally {
      setPendingDetailAction(null);
    }
  }

  async function toggleRecipeMakeSoon() {
    if (!recipeId || pendingDetailAction || isRecipeLoading || detailMode !== 'saved' || recipeLifecycleState === 'archived') {
      return;
    }

    setPendingDetailAction('make-soon');
    setRecipeDetailError(null);
    try {
      const saved = recipeMakeSoonAt
        ? await clearRecipeDetailMakeSoon(recipeId, { token })
        : await markRecipeDetailMakeSoon(recipeId, { token });
      await refreshRecipeCollections();
      applyRecipeDetail(saved);
    } catch (err) {
      await handleApiError(err);
      setRecipeDetailError(formatApiError(err));
    } finally {
      setPendingDetailAction(null);
    }
  }

  async function saveDuplicateRecipeAnyway() {
    if (!recipeDraftId || !pendingDuplicateCandidate || pendingDetailAction || isRecipeLoading) {
      return;
    }

    setPendingDetailAction('save');
    setRecipeDetailError(null);
    try {
      const savedDetail = await acceptRecipeDraft(recipeDraftId, { allowDuplicate: true }, { token });
      focusMainRecipeLibrary();
      await refreshRecipeCollections();
      applyRecipeDetail(savedDetail);
      setPendingDuplicateCandidate(null);
    } catch (err) {
      await handleApiError(err);
      setRecipeDetailError(formatApiError(err));
    } finally {
      setPendingDetailAction(null);
    }
  }

  async function openDuplicateRecipe() {
    if (!pendingDuplicateCandidate) {
      return;
    }
    if (pendingDuplicateCandidate.archivedAt) {
      setListMode('archived');
      setBrowseMode('all');
    } else {
      focusMainRecipeLibrary();
    }
    const duplicateRecipeId = pendingDuplicateCandidate.recipeId;
    setPendingDuplicateCandidate(null);
    await openRecipe(duplicateRecipeId);
  }

  function dismissDuplicateRecipeWarning() {
    setPendingDuplicateCandidate(null);
  }

  async function archiveCurrentRecipe() {
    if (!recipeId || pendingDetailAction || isRecipeLoading) {
      return;
    }

    setPendingDetailAction('archive');
    setRecipeDetailError(null);
    try {
      const archived = await archiveRecipeDetail(recipeId, { token });
      await refreshRecipeCollections();
      setListMode('archived');
      setBrowseMode('all');
      applyRecipeDetail(archived);
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
      const restored = await restoreRecipeDetail(recipeId, { token });
      await refreshRecipeCollections();
      setListMode('active');
      applyRecipeDetail(restored);
    } catch (err) {
      await handleApiError(err);
      setRecipeDetailError(formatApiError(err));
    } finally {
      setPendingDetailAction(null);
    }
  }

  async function deleteCurrentRecipe() {
    if (!recipeId || pendingDetailAction || isRecipeLoading || recipeLifecycleState !== 'archived' || !recipeDeleteEligible) {
      return;
    }

    setPendingDetailAction('delete');
    setRecipeDetailError(null);
    try {
      await deleteRecipe(recipeId, { token });
      await refreshRecipeCollections();
      setIsRecipeDetailOpen(false);
      setRecipeId(null);
      setRecipeLifecycleState(null);
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
      items: filteredRecipeListItems,
      recentItems: recentlyUsedRecipeItems,
      makeSoonItems: makeSoonRecipeItems,
      searchQuery: recipeSearchQuery,
      listMode,
      browseMode,
      activeCount: activeRecipes?.length ?? 0,
      archivedCount: archivedRecipes?.length ?? 0,
      makeSoonCount: makeSoonRecipeItems.length,
      recentCount: recentlyUsedRecipeItems.length,
      isInitialLoading: !hasLoaded && isListLoading,
      isRefreshing,
      error,
      hasLoaded,
      reload: () => loadRecipes({ refreshing: true }),
      showActiveRecipes,
      showArchivedRecipes,
      showAllBrowseRecipes,
      showMakeSoonBrowseRecipes,
      showRecentBrowseRecipes,
      setSearchQuery: setRecipeSearchQuery,
      openRecipe,
      openAddRecipe,
    },
    addRecipe: {
      isOpen: isAddRecipeSheetOpen,
      openAddRecipe,
      closeAddRecipe,
      chooseLink: openImportRecipe,
      chooseCreate: openCreateRecipeOptions,
      chooseImport: openImportRecipeOptions,
    },
    createRecipe: {
      isOpen: isCreateRecipeSheetOpen,
      closeCreateRecipeOptions,
      choosePasteText: openImportRecipeText,
      chooseManual: openCreateRecipe,
    },
    importRecipe: {
      isOpen: isImportRecipeSheetOpen,
      isSelectingImportDocument,
      isSelectingImportImage,
      openImportRecipeOptions,
      closeImportRecipeOptions,
      chooseDocument: pickImportRecipeDocument,
      choosePhotoFromCamera: (callbacks?: RecipeImportCallbacks) => {
        void pickImportRecipeImage('camera', callbacks);
      },
      choosePhotoFromLibrary: (callbacks?: RecipeImportCallbacks) => {
        void pickImportRecipeImage('library', callbacks);
      },
    },
    importDraft: {
      isOpen: isImportSheetOpen,
      importUrl,
      error: importError,
      isImportingDraft,
      clipboardImportUrl,
      setImportUrl,
      openImportRecipe,
      closeImportRecipe,
      importRecipeDraft,
      importSharedRecipeUrl,
      importSharedRecipeAsset,
      pickImportRecipeDocument,
      pickImportRecipeImageFromCamera: (callbacks?: RecipeImportCallbacks) => {
        void pickImportRecipeImage('camera', callbacks);
      },
      pickImportRecipeImageFromLibrary: (callbacks?: RecipeImportCallbacks) => {
        void pickImportRecipeImage('library', callbacks);
      },
    },
    textImportDraft: {
      isOpen: isTextImportSheetOpen,
      importText,
      error: importTextError,
      isImportingDraft: isImportingTextDraft,
      setImportText,
      openImportRecipeText,
      closeImportRecipeText,
      importRecipeTextDraft,
    },
    recipeDetail: {
      isOpen: isRecipeDetailOpen,
      recipeId,
      recipeTitle,
      recipeSource,
      recipeSourceUrl,
      recipeServings,
      recipeMakeSoonAt,
      recipeShortNote,
      recipeInstructions,
      recipeArchivedAt: recipeLifecycleState === 'archived' ? 'archived' : null,
      recipeDeleteEligible,
      recipeDeleteBlockedReason,
      ingredientRows,
      isRecipeLoading,
      hasExistingRecipe: !!recipeId,
      isImportDraft: detailMode === 'import',
      isArchivedRecipe: recipeLifecycleState === 'archived',
      isReadMode: detailMode === 'saved' && isRecipeReadMode,
      canEnterEditMode: detailMode === 'saved' && isRecipeReadMode && recipeLifecycleState !== 'archived',
      canArchiveRecipe: !!recipeId && detailMode === 'saved' && recipeLifecycleState === 'active',
      canRestoreRecipe: !!recipeId && detailMode === 'saved' && recipeLifecycleState === 'archived',
      canDeleteRecipe: !!recipeId && detailMode === 'saved' && recipeLifecycleState === 'archived' && recipeDeleteEligible,
      showDeleteRecipeAction: !!recipeId && detailMode === 'saved' && recipeLifecycleState === 'archived',
      hasIngredients,
      error: recipeDetailError,
      isSavingRecipe: pendingDetailAction === 'save',
      isArchivingRecipe: pendingDetailAction === 'archive',
      isDeletingRecipe: pendingDetailAction === 'delete',
      isTogglingMakeSoon: pendingDetailAction === 'make-soon',
      isActionPending: pendingDetailAction !== null,
      pendingDuplicateCandidate,
      recipeMemory,
      isRecipeMemoryLoading,
      setRecipeTitle,
      setRecipeSource,
      setRecipeSourceUrl,
      setRecipeServings,
      setRecipeShortNote,
      setRecipeInstructions,
      addIngredientRow,
      removeIngredientRow,
      setIngredientName,
      setIngredientQuantity,
      setIngredientUnit,
      startEditingRecipe,
      toggleRecipeMakeSoon,
      saveRecipe,
      saveDuplicateRecipeAnyway,
      openDuplicateRecipe,
      dismissDuplicateRecipeWarning,
      archiveCurrentRecipe,
      restoreCurrentRecipe,
      deleteCurrentRecipe,
      closeRecipeDetail,
    },
  };
}
