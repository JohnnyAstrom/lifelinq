import { useEffect, useMemo, useState } from 'react';
import { ApiError, formatApiError } from '../../../shared/api/client';
import { useAuth } from '../../../shared/auth/AuthContext';
import { useShoppingLists } from '../../shopping/hooks/useShoppingLists';
import {
  createRecipe,
  getRecipe,
  getSlotPlanningChoiceSupport,
  listRecipes,
  updateRecipe,
  type IngredientRequest,
  type MealChoiceCandidateResponse,
  type RecipeResponse,
} from '../api/mealsApi';
import {
  MEAL_INGREDIENT_UNIT_OPTIONS,
  createEmptyIngredientRow,
  ingredientRowsFromResponse,
  sanitizeIngredientQuantityInput,
  toIngredientRequests,
  type MealIngredientRow,
  type MealIngredientUnit,
} from '../utils/ingredientRows';
import {
  findLikelyRecipeDuplicate,
  type RecipeDuplicateCandidate,
} from '../utils/recipeDuplicateGuard';
import { useWeekPlan } from './useWeekPlan';

type MealType = 'BREAKFAST' | 'LUNCH' | 'DINNER';

type MealEntry = {
  dayOfWeek: number;
  mealType: MealType;
  mealTitle: string;
  recipeId: string | null;
  recipeTitle: string | null;
  shoppingHandledAt: string | null;
  shoppingListId: string | null;
};

type EditorPendingAction = 'save-meal' | 'save-to-recipes' | 'remove-meal' | 'add-ingredients-to-shopping' | null;
type ShoppingReviewIngredient = {
  rowId: string;
  position: number;
  name: string;
  amount: string | null;
};
type RecipePickerOption = {
  recipeId: string;
  name: string;
  ingredientCount: number;
};
type RecipeSnapshot = {
  recipeId: string;
  name: string;
  sourceName: string | null;
  sourceUrl: string | null;
  originKind: string;
  savedInRecipes: boolean;
  servings: string | null;
  shortNote: string | null;
  instructions: string | null;
  ingredients: IngredientRequest[];
};
type MealChoiceOption = {
  id: string;
  mealTitle: string;
  recipeId: string | null;
  contextLabel: string;
};

type MealChoiceSection = {
  id: 'recent' | 'familiar' | 'make_soon';
  meals: MealChoiceOption[];
};

const MEAL_ORDER = new Map<MealType, number>([
  ['BREAKFAST', 0],
  ['LUNCH', 1],
  ['DINNER', 2],
]);

type Params = {
  token: string;
  year: number;
  isoWeek: number;
};

export function useMealsWorkflow({ token, year, isoWeek }: Params) {
  const { handleApiError } = useAuth();
  const plan = useWeekPlan(token, year, isoWeek);
  const shopping = useShoppingLists(token);

  function formatCandidateContext(candidate: MealChoiceCandidateResponse) {
    const hasHistoricalUse = candidate.totalOccurrences > 0 && candidate.family !== 'make_soon';
    if (!hasHistoricalUse) {
      return candidate.surfacedBecause;
    }
    const plannedDate = new Date(`${candidate.lastPlannedDate}T00:00:00`);
    const dateLabel = plannedDate.toLocaleDateString(undefined, {
      weekday: 'short',
      day: 'numeric',
      month: 'short',
    });
    return `${candidate.surfacedBecause} · ${dateLabel}`;
  }

  function toMealChoiceOption(candidate: MealChoiceCandidateResponse): MealChoiceOption {
    return {
      id: `${candidate.family}:${candidate.mealIdentityKey}`,
      mealTitle: candidate.title,
      recipeId: candidate.recipeId,
      contextLabel: formatCandidateContext(candidate),
    };
  }

  function toSlotChoiceSections(choiceSupport: {
    recentCandidates: MealChoiceCandidateResponse[];
    familiarCandidates: MealChoiceCandidateResponse[];
    fallbackCandidates: MealChoiceCandidateResponse[];
    makeSoonCandidates: MealChoiceCandidateResponse[];
  }): MealChoiceSection[] {
    const seenIdentityKeys = new Set<string>();
    const sections: MealChoiceSection[] = [];

    const recentMeals = choiceSupport.recentCandidates
      .filter((candidate) => seenIdentityKeys.has(candidate.mealIdentityKey) === false)
      .map((candidate) => {
        seenIdentityKeys.add(candidate.mealIdentityKey);
        return toMealChoiceOption(candidate);
      });
    if (recentMeals.length > 0) {
      sections.push({ id: 'recent', meals: recentMeals });
    }

    const familiarMeals = [...choiceSupport.familiarCandidates, ...choiceSupport.fallbackCandidates]
      .filter((candidate) => seenIdentityKeys.has(candidate.mealIdentityKey) === false)
      .map((candidate) => {
        seenIdentityKeys.add(candidate.mealIdentityKey);
        return toMealChoiceOption(candidate);
      });
    if (familiarMeals.length > 0) {
      sections.push({ id: 'familiar', meals: familiarMeals });
    }

    const makeSoonMeals = choiceSupport.makeSoonCandidates
      .filter((candidate) => seenIdentityKeys.has(candidate.mealIdentityKey) === false)
      .map((candidate) => {
        seenIdentityKeys.add(candidate.mealIdentityKey);
        return toMealChoiceOption(candidate);
      });
    if (makeSoonMeals.length > 0) {
      sections.push({ id: 'make_soon', meals: makeSoonMeals });
    }

    return sections;
  }

  const mealsByDay = useMemo(() => {
    const map = new Map<number, MealEntry[]>();
    if (plan.data) {
      for (const meal of plan.data.meals) {
        const entry: MealEntry = {
          dayOfWeek: meal.dayOfWeek,
          mealType: meal.mealType as MealType,
          mealTitle: meal.mealTitle,
          recipeId: meal.recipeId,
          recipeTitle: meal.recipeTitle,
          shoppingHandledAt: meal.shoppingHandledAt,
          shoppingListId: meal.shoppingListId,
        };
        const list = map.get(meal.dayOfWeek) ?? [];
        list.push(entry);
        map.set(meal.dayOfWeek, list);
      }
    }
    for (const list of map.values()) {
      list.sort((a, b) => (MEAL_ORDER.get(a.mealType) ?? 0) - (MEAL_ORDER.get(b.mealType) ?? 0));
    }
    return map;
  }, [plan.data]);

  const [selectedDay, setSelectedDay] = useState<number | null>(null);
  const [selectedMealType, setSelectedMealType] = useState<MealType | null>('DINNER');
  const [selectedMealRecipeId, setSelectedMealRecipeId] = useState<string | null>(null);
  const [mealTitle, setMealTitle] = useState('');
  const [recipeTitle, setRecipeTitle] = useState('');
  const [recipeSource, setRecipeSource] = useState('');
  const [recipeSourceUrl, setRecipeSourceUrl] = useState<string | null>(null);
  const [recipeOriginKind, setRecipeOriginKind] = useState('MANUAL');
  const [recipeServings, setRecipeServings] = useState('');
  const [recipeShortNote, setRecipeShortNote] = useState('');
  const [recipeInstructions, setRecipeInstructions] = useState('');
  const [ingredientRows, setIngredientRows] = useState<MealIngredientRow[]>([]);
  const [isShoppingReviewOpen, setIsShoppingReviewOpen] = useState(false);
  const [isRecipeDetailOpen, setIsRecipeDetailOpen] = useState(false);
  const [isRecipePickerOpen, setIsRecipePickerOpen] = useState(false);
  const [isRecentMealsOpen, setIsRecentMealsOpen] = useState(false);
  const [isRecipeLoading, setIsRecipeLoading] = useState(false);
  const [isRecipeListLoading, setIsRecipeListLoading] = useState(false);
  const [isMealChoicesLoading, setIsMealChoicesLoading] = useState(false);
  const [selectedListId, setSelectedListId] = useState<string | null>(null);
  const [lastUsedShoppingListId, setLastUsedShoppingListId] = useState<string | null>(null);
  const [selectedShoppingIngredientRowIds, setSelectedShoppingIngredientRowIds] = useState<string[]>([]);
  const [shoppingSyncError, setShoppingSyncError] = useState<string | null>(null);
  const [recipeLoadError, setRecipeLoadError] = useState<string | null>(null);
  const [recipeListError, setRecipeListError] = useState<string | null>(null);
  const [mealChoicesError, setMealChoicesError] = useState<string | null>(null);
  const [availableRecipes, setAvailableRecipes] = useState<RecipeResponse[] | null>(null);
  const [mealChoiceSections, setMealChoiceSections] = useState<MealChoiceSection[] | null>(null);
  const [mealChoicesContextKey, setMealChoicesContextKey] = useState<string | null>(null);
  const [loadedRecipeId, setLoadedRecipeId] = useState<string | null>(null);
  const [pickedRecipeSnapshot, setPickedRecipeSnapshot] = useState<RecipeSnapshot | null>(null);
  const [isSelectedRecipeSavedInRecipes, setIsSelectedRecipeSavedInRecipes] = useState(false);
  const [isEditingSavedRecipeDirectly, setIsEditingSavedRecipeDirectly] = useState(false);
  const [pendingEditorAction, setPendingEditorAction] = useState<EditorPendingAction>(null);
  const [pendingDuplicateCandidate, setPendingDuplicateCandidate] = useState<RecipeDuplicateCandidate | null>(null);

  const selectedMeal = useMemo(() => {
    if (!selectedDay || !selectedMealType) {
      return null;
    }
    const list = mealsByDay.get(selectedDay) ?? [];
    return list.find((meal) => meal.mealType === selectedMealType) ?? null;
  }, [selectedDay, selectedMealType, mealsByDay]);
  const currentMealChoicesContextKey = useMemo(() => {
    if (!selectedDay || !selectedMealType) {
      return null;
    }
    return `${year}-${isoWeek}-${selectedDay}-${selectedMealType}`;
  }, [isoWeek, selectedDay, selectedMealType, year]);
  const visibleMealChoiceSections = useMemo(
    () => mealChoicesContextKey === currentMealChoicesContextKey
      ? (mealChoiceSections ?? [])
      : [],
    [currentMealChoicesContextKey, mealChoiceSections, mealChoicesContextKey]
  );
  const selectedMealHandledListId = selectedMeal?.shoppingListId ?? null;
  const selectedMealHandledAt = selectedMeal?.shoppingHandledAt ?? null;
  const effectiveListId =
    selectedListId
    ?? selectedMealHandledListId
    ?? lastUsedShoppingListId
    ?? (shopping.lists.length > 0 ? shopping.lists[0].id : null);
  const effectiveListName = shopping.lists.find((list) => list.id === effectiveListId)?.name ?? null;

  const hasIngredients = useMemo(
    () => toIngredientRequests(ingredientRows).length > 0,
    [ingredientRows]
  );
  const shoppingReviewIngredients = useMemo<ShoppingReviewIngredient[]>(() => {
    return ingredientRows
      .map((row, index) => ({
        rowId: row.id,
        position: index + 1,
        name: row.name.trim(),
        amount: row.quantityText && row.unit
          ? `${row.quantityText} ${MEAL_INGREDIENT_UNIT_OPTIONS.find((option) => option.value === row.unit)?.label ?? row.unit}`
          : null,
      }))
      .filter((row) => row.name.length > 0);
  }, [ingredientRows]);
  const selectedShoppingIngredientPositions = useMemo(() => {
    const selectedIds = new Set(selectedShoppingIngredientRowIds);
    return shoppingReviewIngredients
      .filter((ingredient) => selectedIds.has(ingredient.rowId))
      .map((ingredient) => ingredient.position);
  }, [selectedShoppingIngredientRowIds, shoppingReviewIngredients]);
  const recipePickerOptions = useMemo<RecipePickerOption[]>(() => {
    return [...(availableRecipes ?? [])]
      .filter((recipe) => recipe.archivedAt == null && recipe.savedInRecipes)
      .sort((left, right) => left.name.localeCompare(right.name))
      .map((recipe) => ({
        recipeId: recipe.recipeId,
        name: recipe.name,
        ingredientCount: recipe.ingredients.length,
      }));
  }, [availableRecipes]);
  const hasAlternativeSavedRecipeOptions = useMemo(() => {
    if (!selectedMealRecipeId) {
      return recipePickerOptions.length > 0;
    }
    return recipePickerOptions.some((option) => option.recipeId !== selectedMealRecipeId);
  }, [recipePickerOptions, selectedMealRecipeId]);
  const currentRecipeDraft = useMemo(() => ({
    name: recipeTitle.trim() || mealTitle.trim(),
    sourceName: recipeSource.trim() || null,
    sourceUrl: recipeSourceUrl,
    originKind: recipeOriginKind,
    servings: recipeServings.trim() || null,
    shortNote: recipeShortNote.trim() || null,
    instructions: recipeInstructions.trim() || null,
    ingredients: toIngredientRequests(ingredientRows),
  }), [ingredientRows, mealTitle, recipeInstructions, recipeOriginKind, recipeServings, recipeShortNote, recipeSource, recipeSourceUrl, recipeTitle]);
  const hasRecipeDraftContent = useMemo(() => (
    !!selectedMealRecipeId
      || currentRecipeDraft.ingredients.length > 0
      || currentRecipeDraft.sourceName != null
      || currentRecipeDraft.sourceUrl != null
      || currentRecipeDraft.shortNote != null
      || currentRecipeDraft.instructions != null
      || (recipeTitle.trim().length > 0 && recipeTitle.trim() !== mealTitle.trim())
  ), [currentRecipeDraft, mealTitle, recipeTitle, selectedMealRecipeId]);
  const hasMeaningfulMealDetails = useMemo(() => (
    currentRecipeDraft.ingredients.length > 0
      || currentRecipeDraft.shortNote != null
      || currentRecipeDraft.instructions != null
  ), [currentRecipeDraft]);
  const hasModifiedPickedRecipe = useMemo(() => {
    if (!pickedRecipeSnapshot) {
      return false;
    }
    if (pickedRecipeSnapshot.name !== currentRecipeDraft.name) {
      return true;
    }
    if (pickedRecipeSnapshot.sourceName !== currentRecipeDraft.sourceName) {
      return true;
    }
    if (pickedRecipeSnapshot.sourceUrl !== currentRecipeDraft.sourceUrl) {
      return true;
    }
    if (pickedRecipeSnapshot.originKind !== currentRecipeDraft.originKind) {
      return true;
    }
    if (pickedRecipeSnapshot.servings !== currentRecipeDraft.servings) {
      return true;
    }
    if (pickedRecipeSnapshot.shortNote !== currentRecipeDraft.shortNote) {
      return true;
    }
    if (pickedRecipeSnapshot.instructions !== currentRecipeDraft.instructions) {
      return true;
    }
    if (pickedRecipeSnapshot.ingredients.length !== currentRecipeDraft.ingredients.length) {
      return true;
    }
    return pickedRecipeSnapshot.ingredients.some((ingredient, index) => {
      const current = currentRecipeDraft.ingredients[index];
      return ingredient.name !== current.name
        || ingredient.quantity !== current.quantity
        || ingredient.unit !== current.unit
        || ingredient.position !== current.position;
    });
  }, [currentRecipeDraft, pickedRecipeSnapshot]);
  const canEnterSavedRecipeEditMode = !!selectedMealRecipeId
    && !!pickedRecipeSnapshot
    && isSelectedRecipeSavedInRecipes
    && !hasModifiedPickedRecipe
    && !isEditingSavedRecipeDirectly;
  const canSaveToRecipes = hasMeaningfulMealDetails
    && !isEditingSavedRecipeDirectly
    && (
      !selectedMealRecipeId
      || !isSelectedRecipeSavedInRecipes
      || hasModifiedPickedRecipe
    );
  const hasShoppingHandled = !!selectedMealHandledAt && !!selectedMealHandledListId;
  const needsShoppingReviewAgain = hasShoppingHandled && (
    mealTitle.trim() !== (selectedMeal?.mealTitle ?? '').trim()
      || selectedMealRecipeId !== (selectedMeal?.recipeId ?? null)
      || hasModifiedPickedRecipe
      || (selectedMeal?.recipeId == null && hasRecipeDraftContent)
  );

  function syncEditorSelection(day: number, mealType: MealType) {
    setSelectedDay(day);
    setSelectedMealType(mealType);
    const list = mealsByDay.get(day) ?? [];
    const existing = list.find((meal) => meal.mealType === mealType);
    setMealTitle(existing?.mealTitle ?? '');
    setRecipeTitle(existing?.recipeId ? existing.recipeTitle ?? '' : '');
    setRecipeSource('');
    setRecipeSourceUrl(null);
    setRecipeOriginKind('MANUAL');
    setRecipeServings('');
    setRecipeShortNote('');
    setRecipeInstructions('');
    setSelectedMealRecipeId(existing?.recipeId ?? null);
    setIngredientRows([]);
    setIsRecipeDetailOpen(false);
    setIsRecipePickerOpen(false);
    setIsRecentMealsOpen(false);
    setIsShoppingReviewOpen(false);
    setIsRecipeLoading(false);
    setLoadedRecipeId(null);
    setPickedRecipeSnapshot(null);
    setIsSelectedRecipeSavedInRecipes(false);
    setIsEditingSavedRecipeDirectly(false);
    setSelectedListId(existing?.shoppingListId ?? lastUsedShoppingListId);
    setSelectedShoppingIngredientRowIds([]);
    setShoppingSyncError(null);
    setRecipeLoadError(null);
    setRecipeListError(null);
    setMealChoicesError(null);
    setMealChoiceSections(null);
    setMealChoicesContextKey(null);
  }

  function toRecipeSnapshot(recipe: RecipeResponse): RecipeSnapshot {
    return {
      recipeId: recipe.recipeId,
      name: recipe.name.trim(),
      sourceName: recipe.sourceName?.trim() || null,
      sourceUrl: recipe.sourceUrl?.trim() || null,
      originKind: recipe.originKind,
      savedInRecipes: recipe.savedInRecipes,
      servings: recipe.servings?.trim() || null,
      shortNote: recipe.shortNote?.trim() || null,
      instructions: recipe.instructions?.trim() || null,
      ingredients: recipe.ingredients
        .map((ingredient) => ({
          name: ingredient.name.trim(),
          quantity: ingredient.quantity,
          unit: ingredient.unit,
          position: ingredient.position,
        }))
        .sort((left, right) => left.position - right.position),
    };
  }

  function applyRecipeSnapshot(recipe: RecipeResponse) {
    setSelectedMealRecipeId(recipe.recipeId);
    setRecipeTitle(recipe.name);
    setMealTitle((current) => (current.trim().length > 0 ? current : recipe.name));
    setRecipeSource(recipe.sourceName ?? '');
    setRecipeSourceUrl(recipe.sourceUrl ?? null);
    setRecipeOriginKind(recipe.originKind);
    setRecipeServings(recipe.servings ?? '');
    setRecipeShortNote(recipe.shortNote ?? '');
    setRecipeInstructions(recipe.instructions ?? '');
    setIngredientRows(ingredientRowsFromResponse(recipe.ingredients));
    setLoadedRecipeId(recipe.recipeId);
    setPickedRecipeSnapshot(toRecipeSnapshot(recipe));
    setIsSelectedRecipeSavedInRecipes(recipe.savedInRecipes);
    setIsEditingSavedRecipeDirectly(false);
    setShoppingSyncError(null);
    setRecipeLoadError(null);
  }

  useEffect(() => {
    let cancelled = false;

    if (!selectedMealRecipeId) {
      return () => {
        cancelled = true;
      };
    }

    if (selectedMealRecipeId === loadedRecipeId) {
      return () => {
        cancelled = true;
      };
    }

    setIsRecipeLoading(true);

    void getRecipe(selectedMealRecipeId, {
      token,
      suppressErrorLoggingStatuses: [404],
    })
      .then((recipe) => {
        if (cancelled) {
          return;
        }
        applyRecipeSnapshot(recipe);
        setAvailableRecipes((current) => {
          const others = (current ?? []).filter((entry) => entry.recipeId !== recipe.recipeId);
          return [...others, recipe];
        });
      })
      .catch(async (err) => {
        if (cancelled) {
          return;
        }
        if (err instanceof ApiError && err.status === 404) {
          setSelectedMealRecipeId(null);
          setLoadedRecipeId(null);
          setPickedRecipeSnapshot(null);
          setIsSelectedRecipeSavedInRecipes(false);
          setRecipeTitle('');
          setRecipeSource('');
          setRecipeSourceUrl(null);
          setRecipeOriginKind('MANUAL');
          setRecipeServings('');
          setRecipeShortNote('');
          setRecipeInstructions('');
          setIngredientRows([]);
          setRecipeLoadError('This recipe was deleted from Recipes. The meal keeps its saved title for history.');
          return;
        }
        await handleApiError(err);
        if (cancelled) {
          return;
        }
        setRecipeLoadError(formatApiError(err));
      })
      .finally(() => {
        if (!cancelled) {
          setIsRecipeLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [loadedRecipeId, selectedMealRecipeId, token, handleApiError]);

  function openEditor(day: number, mealType: MealType) {
    syncEditorSelection(day, mealType);
  }

  function selectEditorMealType(mealType: MealType) {
    if (!selectedDay) {
      return;
    }
    syncEditorSelection(selectedDay, mealType);
  }

  function resetEditorState() {
    setSelectedDay(null);
    setSelectedMealRecipeId(null);
    setMealTitle('');
    setRecipeTitle('');
    setRecipeSource('');
    setRecipeSourceUrl(null);
    setRecipeOriginKind('MANUAL');
    setRecipeServings('');
    setRecipeShortNote('');
    setRecipeInstructions('');
    setIngredientRows([]);
    setIsRecipeDetailOpen(false);
    setIsRecipePickerOpen(false);
    setIsRecentMealsOpen(false);
    setIsShoppingReviewOpen(false);
    setIsRecipeLoading(false);
    setLoadedRecipeId(null);
    setPickedRecipeSnapshot(null);
    setIsSelectedRecipeSavedInRecipes(false);
    setIsEditingSavedRecipeDirectly(false);
    setSelectedShoppingIngredientRowIds([]);
    setRecipeLoadError(null);
    setMealChoicesError(null);
    setMealChoiceSections(null);
    setMealChoicesContextKey(null);
  }

  function closeEditor() {
    if (pendingEditorAction) {
      return;
    }
    setPendingDuplicateCandidate(null);
    resetEditorState();
  }

  function openRecipeDetail() {
    if (isRecipeLoading || pendingEditorAction) {
      return;
    }
    if (!selectedMealRecipeId && recipeTitle.trim().length === 0 && mealTitle.trim().length > 0) {
      setRecipeTitle(mealTitle.trim());
    }
    setIsRecipeDetailOpen(true);
  }

  function closeRecipeDetail() {
    if (pendingEditorAction) {
      return;
    }
    setPendingDuplicateCandidate(null);
    setIsRecipeDetailOpen(false);
  }

  function startEditingSavedRecipeDirectly() {
    if (!canEnterSavedRecipeEditMode || pendingEditorAction) {
      return;
    }
    setIsEditingSavedRecipeDirectly(true);
  }

  async function loadRecipeOptions(force = false) {
    if (!token) {
      return;
    }
    if (isRecipeListLoading) {
      return;
    }
    if (!force && availableRecipes) {
      return;
    }

    setIsRecipeListLoading(true);
    setRecipeListError(null);
    try {
      const recipes = await listRecipes({ token });
      setAvailableRecipes(recipes);
    } catch (err) {
      await handleApiError(err);
      setRecipeListError(formatApiError(err));
    } finally {
      setIsRecipeListLoading(false);
    }
  }

  async function loadRecentMealOptions(force = false) {
    if (!token || !selectedDay || !selectedMealType || !currentMealChoicesContextKey) {
      return;
    }
    if (isMealChoicesLoading) {
      return;
    }
    if (!force && mealChoiceSections && mealChoicesContextKey === currentMealChoicesContextKey) {
      return;
    }

    setIsMealChoicesLoading(true);
    setMealChoicesError(null);
    try {
      const choiceSupport = await getSlotPlanningChoiceSupport(
        {
          year,
          isoWeek,
          dayOfWeek: selectedDay,
          mealType: selectedMealType,
        },
        { token }
      );
      setMealChoiceSections(toSlotChoiceSections(choiceSupport));
      setMealChoicesContextKey(currentMealChoicesContextKey);
    } catch (err) {
      await handleApiError(err);
      setMealChoicesError(formatApiError(err));
    } finally {
      setIsMealChoicesLoading(false);
    }
  }

  function openRecipePicker() {
    if (isRecipeLoading || pendingEditorAction) {
      return;
    }
    setIsRecipePickerOpen(true);
    if (!availableRecipes) {
      void loadRecipeOptions();
      return;
    }
    if (availableRecipes.length === 0) {
      void loadRecipeOptions(true);
    }
  }

  function openRecentMeals() {
    if (pendingEditorAction || !selectedDay || !selectedMealType || !currentMealChoicesContextKey) {
      return;
    }
    setIsRecentMealsOpen(true);
    setMealChoicesError(null);
    if (!mealChoiceSections || mealChoicesContextKey !== currentMealChoicesContextKey) {
      void loadRecentMealOptions();
    }
  }

  function closeRecipePicker() {
    if (pendingEditorAction) {
      return;
    }
    setPendingDuplicateCandidate(null);
    setIsRecipePickerOpen(false);
  }

  function closeRecentMeals() {
    if (pendingEditorAction) {
      return;
    }
    setIsRecentMealsOpen(false);
    setMealChoicesError(null);
  }

  function selectExistingRecipe(recipeId: string) {
    const selectedRecipe = availableRecipes?.find((recipe) => recipe.recipeId === recipeId);
    if (!selectedRecipe) {
      return;
    }
    applyRecipeSnapshot(selectedRecipe);
    setMealTitle((current) => (current.trim().length > 0 ? current : selectedRecipe.name));
    setIsRecipePickerOpen(false);
  }

  async function selectRecentMeal(recentMealId: string) {
    const meal = mealChoiceSections
      ?.flatMap((section) => section.meals)
      .find((entry) => entry.id === recentMealId);
    if (!meal) {
      return;
    }

    setMealTitle(meal.mealTitle);
    setShoppingSyncError(null);
    setRecipeLoadError(null);
    setMealChoicesError(null);
    setIsRecentMealsOpen(false);

    if (meal.recipeId) {
      setSelectedMealRecipeId(meal.recipeId);
      setRecipeTitle(meal.mealTitle);
      setRecipeSource('');
      setRecipeSourceUrl(null);
      setRecipeOriginKind('MANUAL');
      setRecipeServings('');
      setRecipeShortNote('');
      setRecipeInstructions('');
      setIngredientRows([]);
      setLoadedRecipeId(null);
      setPickedRecipeSnapshot(null);
      setIsSelectedRecipeSavedInRecipes(false);
      setIsEditingSavedRecipeDirectly(false);
      return;
    }

    setSelectedMealRecipeId(null);
    setRecipeTitle('');
    setRecipeSource('');
    setRecipeSourceUrl(null);
    setRecipeOriginKind('MANUAL');
    setRecipeServings('');
    setRecipeShortNote('');
    setRecipeInstructions('');
    setIngredientRows([]);
    setLoadedRecipeId(null);
    setPickedRecipeSnapshot(null);
    setIsSelectedRecipeSavedInRecipes(false);
    setIsEditingSavedRecipeDirectly(false);
  }

  function openShoppingReview() {
    if (!isRecipeLoading && shoppingReviewIngredients.length > 0 && !pendingEditorAction) {
      setShoppingSyncError(null);
      if (!selectedListId) {
        setSelectedListId(selectedMealHandledListId ?? lastUsedShoppingListId ?? null);
      }
      setSelectedShoppingIngredientRowIds(shoppingReviewIngredients.map((ingredient) => ingredient.rowId));
      setIsShoppingReviewOpen(true);
    }
  }

  function closeShoppingReview() {
    if (pendingEditorAction) {
      return;
    }
    setSelectedShoppingIngredientRowIds([]);
    setIsShoppingReviewOpen(false);
  }

  function toggleShoppingReviewIngredient(rowId: string) {
    setSelectedShoppingIngredientRowIds((current) => (
      current.includes(rowId)
        ? current.filter((value) => value !== rowId)
        : [...current, rowId]
    ));
  }

  function addIngredientRow() {
    setIngredientRows((current) => {
      const next = [...current, createEmptyIngredientRow()];
      return next;
    });
    setIsRecipeDetailOpen(true);
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

  async function persistMeal(
    targetShoppingListId: string | null,
    options?: { selectedIngredientPositions?: number[] | null; saveInRecipes?: boolean }
  ) {
    if (!selectedDay || !selectedMealType) {
      return false;
    }
    if (!mealTitle.trim()) {
      return false;
    }
    if (isRecipeLoading) {
      return false;
    }

    const ingredients = currentRecipeDraft.ingredients;
    const selectedIngredientPositions = options?.selectedIngredientPositions ?? null;
    const saveInRecipes = options?.saveInRecipes ?? false;
    const shouldPushToShopping = !!targetShoppingListId
      && (selectedIngredientPositions?.length ?? 0) > 0;

    try {
      let recipeId = selectedMealRecipeId;
      if (!hasRecipeDraftContent) {
        recipeId = null;
      } else if (recipeId && pickedRecipeSnapshot && !hasModifiedPickedRecipe) {
        if (!isSelectedRecipeSavedInRecipes && saveInRecipes) {
          const promoted = await updateRecipe(
            recipeId,
            {
              name: currentRecipeDraft.name,
              sourceName: currentRecipeDraft.sourceName,
              sourceUrl: currentRecipeDraft.sourceUrl,
              originKind: currentRecipeDraft.originKind,
              servings: currentRecipeDraft.servings,
              shortNote: currentRecipeDraft.shortNote,
              instructions: currentRecipeDraft.instructions,
              savedInRecipes: true,
              ingredients,
            },
            { token }
          );
          setAvailableRecipes((current) => (
            current
              ? [...current.filter((entry) => entry.recipeId !== promoted.recipeId), promoted]
              : [promoted]
          ));
          setIsSelectedRecipeSavedInRecipes(true);
          setPickedRecipeSnapshot(toRecipeSnapshot(promoted));
        }
        // Reusing an existing saved recipe unchanged keeps the original recipe as-is.
      } else if (recipeId && pickedRecipeSnapshot && isEditingSavedRecipeDirectly) {
        const updated = await updateRecipe(
          recipeId,
          {
            name: currentRecipeDraft.name,
            sourceName: currentRecipeDraft.sourceName,
            sourceUrl: currentRecipeDraft.sourceUrl,
            originKind: currentRecipeDraft.originKind,
            servings: currentRecipeDraft.servings,
            shortNote: currentRecipeDraft.shortNote,
            instructions: currentRecipeDraft.instructions,
            savedInRecipes: true,
            ingredients,
          },
          { token }
        );
        setAvailableRecipes((current) => {
          const others = (current ?? []).filter((entry) => entry.recipeId !== updated.recipeId);
          return [...others, updated];
        });
        setIsSelectedRecipeSavedInRecipes(true);
        setPickedRecipeSnapshot(toRecipeSnapshot(updated));
      } else if (recipeId && pickedRecipeSnapshot && !isSelectedRecipeSavedInRecipes) {
        const updated = await updateRecipe(
          recipeId,
          {
            name: currentRecipeDraft.name,
            sourceName: currentRecipeDraft.sourceName,
            sourceUrl: currentRecipeDraft.sourceUrl,
            originKind: currentRecipeDraft.originKind,
            servings: currentRecipeDraft.servings,
            shortNote: currentRecipeDraft.shortNote,
            instructions: currentRecipeDraft.instructions,
            savedInRecipes: saveInRecipes,
            ingredients,
          },
          { token }
        );
        setAvailableRecipes((current) => (
          current
            ? [...current.filter((entry) => entry.recipeId !== updated.recipeId), updated]
            : [updated]
        ));
        setIsSelectedRecipeSavedInRecipes(updated.savedInRecipes);
        setPickedRecipeSnapshot(toRecipeSnapshot(updated));
      } else if (recipeId && !hasModifiedPickedRecipe) {
        const updated = await updateRecipe(
          recipeId,
          {
            name: currentRecipeDraft.name,
            sourceName: currentRecipeDraft.sourceName,
            sourceUrl: currentRecipeDraft.sourceUrl,
            originKind: currentRecipeDraft.originKind,
            servings: currentRecipeDraft.servings,
            shortNote: currentRecipeDraft.shortNote,
            instructions: currentRecipeDraft.instructions,
            savedInRecipes: true,
            ingredients,
          },
          { token }
        );
        setAvailableRecipes((current) => {
          const others = (current ?? []).filter((entry) => entry.recipeId !== updated.recipeId);
          return [...others, updated];
        });
        setIsSelectedRecipeSavedInRecipes(true);
        setPickedRecipeSnapshot(toRecipeSnapshot(updated));
      } else {
        const created = await createRecipe(
          {
            name: currentRecipeDraft.name,
            sourceName: currentRecipeDraft.sourceName,
            sourceUrl: currentRecipeDraft.sourceUrl,
            originKind: currentRecipeDraft.originKind,
            servings: currentRecipeDraft.servings,
            shortNote: currentRecipeDraft.shortNote,
            instructions: currentRecipeDraft.instructions,
            savedInRecipes: saveInRecipes,
            ingredients,
          },
          { token }
        );
        recipeId = created.recipeId;
        setAvailableRecipes((current) => (
          current
            ? [...current.filter((entry) => entry.recipeId !== created.recipeId), created]
            : [created]
        ));
        setSelectedMealRecipeId(created.recipeId);
        setLoadedRecipeId(created.recipeId);
        setPickedRecipeSnapshot(toRecipeSnapshot(created));
        setIsSelectedRecipeSavedInRecipes(created.savedInRecipes);
      }

      await plan.addMeal(selectedDay, selectedMealType, {
        mealTitle: mealTitle.trim(),
        recipeId,
        mealType: selectedMealType,
        targetShoppingListId: shouldPushToShopping ? targetShoppingListId : null,
        selectedIngredientPositions: shouldPushToShopping ? selectedIngredientPositions : null,
      });

      if (shouldPushToShopping) {
        await shopping.reload();
        setLastUsedShoppingListId(targetShoppingListId);
        setSelectedListId(targetShoppingListId);
      }

      setShoppingSyncError(null);
      return true;
    } catch (err) {
      await handleApiError(err);
      setShoppingSyncError(formatApiError(err));
      return false;
    }
  }

  async function runEditorAction(
    action: Exclude<EditorPendingAction, null>,
    callback: () => Promise<void>
  ) {
    if (pendingEditorAction) {
      return;
    }

    setPendingEditorAction(action);
    try {
      await callback();
    } finally {
      setPendingEditorAction(null);
    }
  }

  async function saveMeal() {
    await runEditorAction('save-meal', async () => {
      const saved = await persistMeal(null);
      if (!saved) {
        return;
      }

      setMealTitle('');
      setRecipeTitle('');
      setRecipeSource('');
      setRecipeSourceUrl(null);
      setRecipeOriginKind('MANUAL');
      setRecipeServings('');
      setRecipeShortNote('');
      setRecipeInstructions('');
      setSelectedMealRecipeId(null);
      setIngredientRows([]);
      setSelectedMealType('DINNER');
      resetEditorState();
    });
  }

  async function saveToRecipes() {
    if (pendingEditorAction) {
      return;
    }

    const willCreateNewSavedRecipe = canSaveToRecipes
      && hasRecipeDraftContent
      && (
        !selectedMealRecipeId
        || (!pickedRecipeSnapshot && selectedMealRecipeId != null)
        || (pickedRecipeSnapshot != null && hasModifiedPickedRecipe)
      );

    if (willCreateNewSavedRecipe) {
      let recipes = availableRecipes;
      if (!recipes) {
        try {
          recipes = await listRecipes({ token });
          setAvailableRecipes(recipes);
        } catch (err) {
          await handleApiError(err);
          setRecipeListError(formatApiError(err));
          return;
        }
      }
      const duplicateCandidate = findLikelyRecipeDuplicate({
        recipes: recipes.filter((recipe) => recipe.archivedAt == null && recipe.savedInRecipes),
        name: currentRecipeDraft.name,
        sourceName: currentRecipeDraft.sourceName,
        sourceUrl: currentRecipeDraft.sourceUrl,
      });
      if (duplicateCandidate) {
        setPendingDuplicateCandidate(duplicateCandidate);
        return;
      }
    }

    await runEditorAction('save-to-recipes', async () => {
      const saved = await persistMeal(null, { saveInRecipes: true });
      if (!saved) {
        return;
      }
      setIsRecipeDetailOpen(false);
      setIsRecipePickerOpen(false);
      setIsShoppingReviewOpen(false);
    });
  }

  async function saveDuplicateRecipeAnyway() {
    if (pendingEditorAction) {
      return;
    }
    setPendingDuplicateCandidate(null);
    await runEditorAction('save-to-recipes', async () => {
      const saved = await persistMeal(null, { saveInRecipes: true });
      if (!saved) {
        return;
      }
      setIsRecipeDetailOpen(false);
      setIsRecipePickerOpen(false);
      setIsShoppingReviewOpen(false);
    });
  }

  function dismissDuplicateRecipeWarning() {
    setPendingDuplicateCandidate(null);
  }

  function useDuplicateRecipe(recipeId: string) {
    const selectedRecipe = availableRecipes?.find((recipe) => recipe.recipeId === recipeId);
    if (!selectedRecipe) {
      return;
    }
    setPendingDuplicateCandidate(null);
    applyRecipeSnapshot(selectedRecipe);
    setMealTitle((current) => (current.trim().length > 0 ? current : selectedRecipe.name));
    setIsRecipePickerOpen(false);
    setIsRecipeDetailOpen(false);
  }

  async function addIngredientsToShopping() {
    const targetShoppingListId = effectiveListId;
    if (!targetShoppingListId || selectedShoppingIngredientPositions.length === 0) {
      return;
    }

    await runEditorAction('add-ingredients-to-shopping', async () => {
      const saved = await persistMeal(targetShoppingListId, {
        selectedIngredientPositions: selectedShoppingIngredientPositions,
      });
      if (!saved) {
        return;
      }

      setMealTitle('');
      setRecipeTitle('');
      setRecipeSource('');
      setRecipeSourceUrl(null);
      setRecipeOriginKind('MANUAL');
      setRecipeServings('');
      setRecipeShortNote('');
      setRecipeInstructions('');
      setSelectedMealRecipeId(null);
      setIngredientRows([]);
      setSelectedMealType('DINNER');
      resetEditorState();
    });
  }

  async function removeMeal() {
    if (!selectedDay || !selectedMealType) {
      return;
    }
    await runEditorAction('remove-meal', async () => {
      try {
        await plan.removeMeal(selectedDay, selectedMealType);
        resetEditorState();
        setSelectedMealType('DINNER');
        setSelectedMealRecipeId(null);
        setMealTitle('');
        setRecipeTitle('');
        setRecipeSource('');
        setRecipeSourceUrl(null);
        setRecipeOriginKind('MANUAL');
        setRecipeServings('');
        setRecipeShortNote('');
        setRecipeInstructions('');
        setIngredientRows([]);
        setShoppingSyncError(null);
      } catch (err) {
        await handleApiError(err);
        setShoppingSyncError(formatApiError(err));
      }
    });
  }

  return {
    plan,
    shopping,
    mealsByDay,
    editor: {
      isOpen: !!selectedDay && !!selectedMealType,
      selectedDay,
      selectedMealType,
      selectedMealRecipeId,
      mealTitle,
      recipeTitle,
      recipeSource,
      recipeServings,
      recipeShortNote,
      recipeInstructions,
      ingredientRows,
      isRecipeDetailOpen,
      isRecipePickerOpen,
      isRecentMealsOpen,
      isShoppingReviewOpen,
      isRecipeLoading,
      isRecipeListLoading,
      isMealChoicesLoading,
      recipeListError,
      recipePickerOptions,
      showRecipePickerAction: !isSelectedRecipeSavedInRecipes || hasAlternativeSavedRecipeOptions,
      mealChoiceSections: visibleMealChoiceSections,
      mealChoicesError,
      hasModifiedPickedRecipe,
      isSelectedRecipeSavedInRecipes,
      canEnterSavedRecipeEditMode,
      canSaveToRecipes,
      isEditingSavedRecipeDirectly,
      hasIngredients,
      hasRecipeDraftContent,
      hasMeaningfulMealDetails,
      shoppingReviewIngredients,
      selectedShoppingIngredientRowIds,
      selectedShoppingIngredientCount: selectedShoppingIngredientPositions.length,
      selectedListId,
      effectiveListId,
      effectiveListName,
      shoppingHandledAt: selectedMealHandledAt,
      hasShoppingHandled,
      needsShoppingReviewAgain,
      shoppingSyncError,
      recipeLoadError,
      pendingDuplicateCandidate,
      pendingAction: pendingEditorAction,
      isActionPending: pendingEditorAction !== null,
      isSavingMeal: pendingEditorAction === 'save-meal',
      isSavingToRecipes: pendingEditorAction === 'save-to-recipes',
      isRemovingMeal: pendingEditorAction === 'remove-meal',
      isAddingIngredientsToShopping: pendingEditorAction === 'add-ingredients-to-shopping',
      selectedMeal,
      setMealTitle,
      setRecipeTitle,
      setRecipeSource,
      setRecipeServings,
      setRecipeShortNote,
      setRecipeInstructions,
      openRecipeDetail,
      closeRecipeDetail,
      startEditingSavedRecipeDirectly,
      openRecipePicker,
      closeRecipePicker,
      loadRecipeOptions,
      selectExistingRecipe,
      openRecentMeals,
      closeRecentMeals,
      loadRecentMealOptions,
      selectRecentMeal,
      openShoppingReview,
      closeShoppingReview,
      toggleShoppingReviewIngredient,
      addIngredientRow,
      removeIngredientRow,
      setIngredientName,
      setIngredientQuantity,
      setIngredientUnit,
      setSelectedListId,
      setSelectedMealType: selectEditorMealType,
      setSelectedMealRecipeId,
      setShoppingSyncError,
    },
    actions: {
      openEditor,
      closeEditor,
      saveMeal,
      saveToRecipes,
      saveDuplicateRecipeAnyway,
      dismissDuplicateRecipeWarning,
      useDuplicateRecipe,
      addIngredientsToShopping,
      removeMeal,
    },
  };
}
