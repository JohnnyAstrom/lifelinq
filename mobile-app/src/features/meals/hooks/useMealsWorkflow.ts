import { useEffect, useMemo, useState } from 'react';
import { formatApiError } from '../../../shared/api/client';
import { useAuth } from '../../../shared/auth/AuthContext';
import { useShoppingLists } from '../../shopping/hooks/useShoppingLists';
import {
  createRecipe,
  getRecipe,
  listRecipes,
  updateRecipe,
  type IngredientRequest,
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
import { useWeekPlan } from './useWeekPlan';

type MealType = 'BREAKFAST' | 'LUNCH' | 'DINNER';

type MealEntry = {
  dayOfWeek: number;
  mealType: MealType;
  recipeId: string;
  recipeTitle: string;
};

type EditorPendingAction = 'save-meal' | 'remove-meal' | 'add-ingredients-to-shopping' | null;
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
  source: string | null;
  shortNote: string | null;
  instructions: string | null;
  ingredients: IngredientRequest[];
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

  const mealsByDay = useMemo(() => {
    const map = new Map<number, MealEntry[]>();
    if (plan.data) {
      for (const meal of plan.data.meals) {
        const entry: MealEntry = {
          dayOfWeek: meal.dayOfWeek,
          mealType: meal.mealType as MealType,
          recipeId: meal.recipeId,
          recipeTitle: meal.recipeTitle,
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
  const [recipeTitle, setRecipeTitle] = useState('');
  const [recipeSource, setRecipeSource] = useState('');
  const [recipeShortNote, setRecipeShortNote] = useState('');
  const [recipeInstructions, setRecipeInstructions] = useState('');
  const [ingredientRows, setIngredientRows] = useState<MealIngredientRow[]>([]);
  const [isShoppingReviewOpen, setIsShoppingReviewOpen] = useState(false);
  const [isRecipeDetailOpen, setIsRecipeDetailOpen] = useState(false);
  const [isRecipePickerOpen, setIsRecipePickerOpen] = useState(false);
  const [isRecipeLoading, setIsRecipeLoading] = useState(false);
  const [isRecipeListLoading, setIsRecipeListLoading] = useState(false);
  const [selectedListId, setSelectedListId] = useState<string | null>(null);
  const [selectedShoppingIngredientRowIds, setSelectedShoppingIngredientRowIds] = useState<string[]>([]);
  const [shoppingSyncError, setShoppingSyncError] = useState<string | null>(null);
  const [recipeListError, setRecipeListError] = useState<string | null>(null);
  const [availableRecipes, setAvailableRecipes] = useState<RecipeResponse[] | null>(null);
  const [loadedRecipeId, setLoadedRecipeId] = useState<string | null>(null);
  const [pickedRecipeSnapshot, setPickedRecipeSnapshot] = useState<RecipeSnapshot | null>(null);
  const [isEditingSavedRecipeDirectly, setIsEditingSavedRecipeDirectly] = useState(false);
  const [pendingEditorAction, setPendingEditorAction] = useState<EditorPendingAction>(null);

  const effectiveListId =
    selectedListId ?? (shopping.lists.length > 0 ? shopping.lists[0].id : null);

  const selectedMeal = useMemo(() => {
    if (!selectedDay || !selectedMealType) {
      return null;
    }
    const list = mealsByDay.get(selectedDay) ?? [];
    return list.find((meal) => meal.mealType === selectedMealType) ?? null;
  }, [selectedDay, selectedMealType, mealsByDay]);

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
      .sort((left, right) => left.name.localeCompare(right.name))
      .map((recipe) => ({
        recipeId: recipe.recipeId,
        name: recipe.name,
        ingredientCount: recipe.ingredients.length,
      }));
  }, [availableRecipes]);
  const currentRecipeDraft = useMemo(() => ({
    name: recipeTitle.trim(),
    source: recipeSource.trim() || null,
    shortNote: recipeShortNote.trim() || null,
    instructions: recipeInstructions.trim() || null,
    ingredients: toIngredientRequests(ingredientRows),
  }), [ingredientRows, recipeInstructions, recipeShortNote, recipeSource, recipeTitle]);
  const hasModifiedPickedRecipe = useMemo(() => {
    if (!pickedRecipeSnapshot) {
      return false;
    }
    if (pickedRecipeSnapshot.name !== currentRecipeDraft.name) {
      return true;
    }
    if (pickedRecipeSnapshot.source !== currentRecipeDraft.source) {
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
    && !hasModifiedPickedRecipe
    && !isEditingSavedRecipeDirectly;

  function syncEditorSelection(day: number, mealType: MealType) {
    setSelectedDay(day);
    setSelectedMealType(mealType);
    const list = mealsByDay.get(day) ?? [];
    const existing = list.find((meal) => meal.mealType === mealType);
    setRecipeTitle(existing?.recipeTitle ?? '');
    setRecipeSource('');
    setRecipeShortNote('');
    setRecipeInstructions('');
    setSelectedMealRecipeId(existing?.recipeId ?? null);
    setIngredientRows([]);
    setIsRecipeDetailOpen(false);
    setIsRecipePickerOpen(false);
    setIsShoppingReviewOpen(false);
    setIsRecipeLoading(false);
    setLoadedRecipeId(null);
    setPickedRecipeSnapshot(null);
    setIsEditingSavedRecipeDirectly(false);
    setSelectedShoppingIngredientRowIds([]);
    setShoppingSyncError(null);
    setRecipeListError(null);
  }

  function toRecipeSnapshot(recipe: RecipeResponse): RecipeSnapshot {
    return {
      recipeId: recipe.recipeId,
      name: recipe.name.trim(),
      source: recipe.source?.trim() || null,
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
    setRecipeSource(recipe.source ?? '');
    setRecipeShortNote(recipe.shortNote ?? '');
    setRecipeInstructions(recipe.instructions ?? '');
    setIngredientRows(ingredientRowsFromResponse(recipe.ingredients));
    setLoadedRecipeId(recipe.recipeId);
    setPickedRecipeSnapshot(toRecipeSnapshot(recipe));
    setIsEditingSavedRecipeDirectly(false);
    setShoppingSyncError(null);
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

    void getRecipe(selectedMealRecipeId, { token })
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
        await handleApiError(err);
        if (cancelled) {
          return;
        }
        setShoppingSyncError(formatApiError(err));
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

  function selectEditorDay(day: number) {
    syncEditorSelection(day, selectedMealType ?? 'DINNER');
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
    setRecipeSource('');
    setRecipeShortNote('');
    setRecipeInstructions('');
    setIngredientRows([]);
    setIsRecipeDetailOpen(false);
    setIsRecipePickerOpen(false);
    setIsShoppingReviewOpen(false);
    setIsRecipeLoading(false);
    setLoadedRecipeId(null);
    setPickedRecipeSnapshot(null);
    setIsEditingSavedRecipeDirectly(false);
    setSelectedShoppingIngredientRowIds([]);
  }

  function closeEditor() {
    if (pendingEditorAction) {
      return;
    }
    resetEditorState();
  }

  function openRecipeDetail() {
    if (isRecipeLoading || pendingEditorAction) {
      return;
    }
    setIsRecipeDetailOpen(true);
  }

  function closeRecipeDetail() {
    if (pendingEditorAction) {
      return;
    }
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

  function openRecipePicker() {
    if (isRecipeLoading || pendingEditorAction) {
      return;
    }
    setIsRecipePickerOpen(true);
    if (!availableRecipes) {
      void loadRecipeOptions();
    }
  }

  function closeRecipePicker() {
    if (pendingEditorAction) {
      return;
    }
    setIsRecipePickerOpen(false);
  }

  function selectExistingRecipe(recipeId: string) {
    const selectedRecipe = availableRecipes?.find((recipe) => recipe.recipeId === recipeId);
    if (!selectedRecipe) {
      return;
    }
    applyRecipeSnapshot(selectedRecipe);
    setIsRecipePickerOpen(false);
  }

  function openShoppingReview() {
    if (!isRecipeLoading && shoppingReviewIngredients.length > 0 && !pendingEditorAction) {
      setShoppingSyncError(null);
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
    options?: { selectedIngredientPositions?: number[] | null }
  ) {
    if (!selectedDay || !selectedMealType) {
      return false;
    }
    if (!recipeTitle.trim()) {
      return false;
    }
    if (isRecipeLoading) {
      return false;
    }

    const ingredients = currentRecipeDraft.ingredients;
    const selectedIngredientPositions = options?.selectedIngredientPositions ?? null;
    const shouldPushToShopping = !!targetShoppingListId
      && (selectedIngredientPositions?.length ?? 0) > 0;

    try {
      let recipeId = selectedMealRecipeId;
      if (recipeId && pickedRecipeSnapshot && !hasModifiedPickedRecipe) {
        // Reusing an existing saved recipe unchanged keeps the original recipe as-is.
      } else if (recipeId && pickedRecipeSnapshot && isEditingSavedRecipeDirectly) {
        const updated = await updateRecipe(
          recipeId,
          {
            name: currentRecipeDraft.name,
            source: currentRecipeDraft.source,
            shortNote: currentRecipeDraft.shortNote,
            instructions: currentRecipeDraft.instructions,
            ingredients,
          },
          { token }
        );
        setAvailableRecipes((current) => {
          const others = (current ?? []).filter((entry) => entry.recipeId !== updated.recipeId);
          return [...others, updated];
        });
      } else if (recipeId && !hasModifiedPickedRecipe) {
        const updated = await updateRecipe(
          recipeId,
          {
            name: currentRecipeDraft.name,
            source: currentRecipeDraft.source,
            shortNote: currentRecipeDraft.shortNote,
            instructions: currentRecipeDraft.instructions,
            ingredients,
          },
          { token }
        );
        setAvailableRecipes((current) => {
          const others = (current ?? []).filter((entry) => entry.recipeId !== updated.recipeId);
          return [...others, updated];
        });
      } else {
        const created = await createRecipe(
          {
            name: currentRecipeDraft.name,
            source: currentRecipeDraft.source,
            shortNote: currentRecipeDraft.shortNote,
            instructions: currentRecipeDraft.instructions,
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
      }

      await plan.addMeal(selectedDay, selectedMealType, {
        recipeId,
        mealType: selectedMealType,
        targetShoppingListId: shouldPushToShopping ? targetShoppingListId : null,
        selectedIngredientPositions: shouldPushToShopping ? selectedIngredientPositions : null,
      });

      if (shouldPushToShopping) {
        await shopping.reload();
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

      setRecipeTitle('');
      setRecipeSource('');
      setRecipeShortNote('');
      setRecipeInstructions('');
      setSelectedMealRecipeId(null);
      setIngredientRows([]);
      setSelectedMealType('DINNER');
      resetEditorState();
    });
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

      setRecipeTitle('');
      setRecipeSource('');
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
        setRecipeTitle('');
        setRecipeSource('');
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
      recipeTitle,
      recipeSource,
      recipeShortNote,
      recipeInstructions,
      ingredientRows,
      isRecipeDetailOpen,
      isRecipePickerOpen,
      isShoppingReviewOpen,
      isRecipeLoading,
      isRecipeListLoading,
      recipeListError,
      recipePickerOptions,
      hasModifiedPickedRecipe,
      canEnterSavedRecipeEditMode,
      isEditingSavedRecipeDirectly,
      hasIngredients,
      shoppingReviewIngredients,
      selectedShoppingIngredientRowIds,
      selectedShoppingIngredientCount: selectedShoppingIngredientPositions.length,
      selectedListId,
      effectiveListId,
      shoppingSyncError,
      pendingAction: pendingEditorAction,
      isActionPending: pendingEditorAction !== null,
      isSavingMeal: pendingEditorAction === 'save-meal',
      isRemovingMeal: pendingEditorAction === 'remove-meal',
      isAddingIngredientsToShopping: pendingEditorAction === 'add-ingredients-to-shopping',
      selectedMeal,
      setRecipeTitle,
      setRecipeSource,
      setRecipeShortNote,
      setRecipeInstructions,
      openRecipeDetail,
      closeRecipeDetail,
      startEditingSavedRecipeDirectly,
      openRecipePicker,
      closeRecipePicker,
      loadRecipeOptions,
      selectExistingRecipe,
      openShoppingReview,
      closeShoppingReview,
      toggleShoppingReviewIngredient,
      addIngredientRow,
      removeIngredientRow,
      setIngredientName,
      setIngredientQuantity,
      setIngredientUnit,
      setSelectedListId,
      setSelectedDay: selectEditorDay,
      setSelectedMealType: selectEditorMealType,
      setSelectedMealRecipeId,
      setShoppingSyncError,
    },
    actions: {
      openEditor,
      closeEditor,
      saveMeal,
      addIngredientsToShopping,
      removeMeal,
    },
  };
}
