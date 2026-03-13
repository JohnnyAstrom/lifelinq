import { useEffect, useMemo, useState } from 'react';
import { formatApiError } from '../../../shared/api/client';
import { useAuth } from '../../../shared/auth/AuthContext';
import { useShoppingLists } from '../../shopping/hooks/useShoppingLists';
import { createRecipe, getRecipe, updateRecipe } from '../api/mealsApi';
import {
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
  const [ingredientRows, setIngredientRows] = useState<MealIngredientRow[]>([createEmptyIngredientRow()]);
  const [isRecipeLoading, setIsRecipeLoading] = useState(false);
  const [pushToShopping, setPushToShopping] = useState(true);
  const [selectedListId, setSelectedListId] = useState<string | null>(null);
  const [shoppingSyncError, setShoppingSyncError] = useState<string | null>(null);

  const effectiveListId =
    selectedListId ?? (shopping.lists.length > 0 ? shopping.lists[0].id : null);

  const selectedMeal = useMemo(() => {
    if (!selectedDay || !selectedMealType) {
      return null;
    }
    const list = mealsByDay.get(selectedDay) ?? [];
    return list.find((meal) => meal.mealType === selectedMealType) ?? null;
  }, [selectedDay, selectedMealType, mealsByDay]);

  function syncEditorSelection(day: number, mealType: MealType) {
    setSelectedDay(day);
    setSelectedMealType(mealType);
    const list = mealsByDay.get(day) ?? [];
    const existing = list.find((meal) => meal.mealType === mealType);
    setRecipeTitle(existing?.recipeTitle ?? '');
    setSelectedMealRecipeId(existing?.recipeId ?? null);
    setIngredientRows([createEmptyIngredientRow()]);
    setIsRecipeLoading(false);
    setShoppingSyncError(null);
  }

  useEffect(() => {
    let cancelled = false;

    if (!selectedMealRecipeId) {
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
        setRecipeTitle(recipe.name);
        setIngredientRows(ingredientRowsFromResponse(recipe.ingredients));
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
  }, [selectedMealRecipeId, token, handleApiError]);

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

  function closeEditor() {
    setSelectedDay(null);
    setSelectedMealRecipeId(null);
    setIngredientRows([createEmptyIngredientRow()]);
    setIsRecipeLoading(false);
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
        return [createEmptyIngredientRow()];
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

  async function saveMeal() {
    if (!selectedDay || !selectedMealType) {
      return;
    }
    if (!recipeTitle.trim()) {
      return;
    }
    if (isRecipeLoading) {
      return;
    }

    const ingredients = toIngredientRequests(ingredientRows);

    try {
      let recipeId = selectedMealRecipeId;
      if (recipeId) {
        await updateRecipe(
          recipeId,
          {
            name: recipeTitle.trim(),
            ingredients,
          },
          { token }
        );
      } else {
        const created = await createRecipe(
          {
            name: recipeTitle.trim(),
            ingredients,
          },
          { token }
        );
        recipeId = created.recipeId;
      }

      await plan.addMeal(selectedDay, selectedMealType, {
        recipeId,
        mealType: selectedMealType,
        targetShoppingListId: pushToShopping && effectiveListId ? effectiveListId : null,
      });

      if (pushToShopping && ingredients.length > 0 && effectiveListId) {
        await shopping.reload();
      }

      setRecipeTitle('');
      setSelectedMealRecipeId(null);
      setIngredientRows([createEmptyIngredientRow()]);
      setSelectedMealType('DINNER');
      setShoppingSyncError(null);
      closeEditor();
    } catch (err) {
      await handleApiError(err);
      setShoppingSyncError(formatApiError(err));
    }
  }

  async function removeMeal() {
    if (!selectedDay || !selectedMealType) {
      return;
    }
    try {
      await plan.removeMeal(selectedDay, selectedMealType);
      closeEditor();
      setSelectedMealType('DINNER');
      setSelectedMealRecipeId(null);
      setRecipeTitle('');
      setIngredientRows([createEmptyIngredientRow()]);
      setShoppingSyncError(null);
    } catch (err) {
      await handleApiError(err);
      setShoppingSyncError(formatApiError(err));
    }
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
      ingredientRows,
      isRecipeLoading,
      pushToShopping,
      selectedListId,
      effectiveListId,
      shoppingSyncError,
      selectedMeal,
      setRecipeTitle,
      addIngredientRow,
      removeIngredientRow,
      setIngredientName,
      setIngredientQuantity,
      setIngredientUnit,
      setPushToShopping,
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
      removeMeal,
    },
  };
}
