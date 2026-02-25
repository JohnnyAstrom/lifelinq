import { useMemo, useState } from 'react';
import { formatApiError } from '../../../shared/api/client';
import { useAuth } from '../../../shared/auth/AuthContext';
import { useShoppingLists } from '../../shopping/hooks/useShoppingLists';
import { createRecipe, updateRecipe } from '../api/mealsApi';
import { toIngredientsList } from '../utils/mealParsing';
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
  const [ingredientsText, setIngredientsText] = useState('');
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

  function openEditor(day: number, mealType: MealType) {
    setSelectedDay(day);
    setSelectedMealType(mealType);
    const list = mealsByDay.get(day) ?? [];
    const existing = list.find((meal) => meal.mealType === mealType);
    setRecipeTitle(existing?.recipeTitle ?? '');
    setSelectedMealRecipeId(existing?.recipeId ?? null);
    setIngredientsText('');
    setShoppingSyncError(null);
  }

  function closeEditor() {
    setSelectedDay(null);
    setSelectedMealRecipeId(null);
  }

  async function saveMeal() {
    if (!selectedDay || !selectedMealType) {
      return;
    }
    if (!recipeTitle.trim()) {
      return;
    }

    const ingredients = toIngredientsList(ingredientsText).map((name, index) => ({
      name,
      quantity: null,
      unit: null,
      position: index + 1,
    }));

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
      setIngredientsText('');
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
      setIngredientsText('');
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
      ingredientsText,
      pushToShopping,
      selectedListId,
      effectiveListId,
      shoppingSyncError,
      selectedMeal,
      setRecipeTitle,
      setIngredientsText,
      setPushToShopping,
      setSelectedListId,
      setSelectedMealType,
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
