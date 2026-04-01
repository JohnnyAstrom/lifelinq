import { useEffect, useMemo, useRef, useState } from 'react';
import { Ionicons } from '@expo/vector-icons';
import {
  Alert,
  Pressable,
  RefreshControl,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { MealDayDetailSheet } from '../components/MealDayDetailSheet';
import { MealEditorSheet } from '../components/MealEditorSheet';
import {
  MealRecipeCaptureSourceSheet,
  type MealRecipeCaptureSourceOption,
} from '../components/MealRecipeCaptureSourceSheet';
import { MealRecipeDetailSheet } from '../components/MealRecipeDetailSheet';
import { MealRecipeImportSheet } from '../components/MealRecipeImportSheet';
import { MealRecipeTextImportSheet } from '../components/MealRecipeTextImportSheet';
import { MealRecipePlanSheet } from '../components/MealRecipePlanSheet';
import { MealRecipePickerSheet } from '../components/MealRecipePickerSheet';
import { MealRecentMealsSheet } from '../components/MealRecentMealsSheet';
import { MealShoppingReviewSheet } from '../components/MealShoppingReviewSheet';
import { WeekShoppingReviewSheet } from '../components/WeekShoppingReviewSheet';
import { MealsRecipesView } from '../components/MealsRecipesView';
import { MealsMonthlyView } from '../components/MealsMonthlyView';
import { MealsWeeklyView } from '../components/MealsWeeklyView';
import { DuplicateRecipeWarningSheet } from '../components/DuplicateRecipeWarningSheet';
import {
  addWeekShoppingReviewLines,
  getWeekPlan,
  getWeekShoppingReview,
  type AggregatedIngredientComparisonResponse,
  type ContributorMealReferenceResponse,
  type WeekPlanResponse,
  type WeekShoppingReviewResponse,
} from '../api/mealsApi';
import {
  addDays,
  buildMonthGridCells,
  getIsoWeekParts,
  getWeekStartDate,
  isTodayDate,
  toDateKey,
} from '../utils/mealDates';
import { useMealsWorkflow } from '../hooks/useMealsWorkflow';
import { useMealsRecipesWorkspace } from '../hooks/useMealsRecipesWorkspace';
import { useMonthMealOverview } from '../hooks/useMonthMealOverview';
import { formatApiError } from '../../../shared/api/client';
import { useAuth } from '../../../shared/auth/AuthContext';
import { useAppBackHandler } from '../../../shared/hooks/useAppBackHandler';
import {
  AppButton,
  AppCard,
  AppSegmentedControl,
  AppScreen,
  BackIconButton,
  Subtle,
  TopBar,
} from '../../../shared/ui/components';
import { iconBackground, textStyles, theme } from '../../../shared/ui/theme';

type Props = {
  token: string;
  recipeCaptureEntryRequest?: {
    key: number;
    sharedUrl?: string | null;
    sharedAsset?: {
      assetKind: 'DOCUMENT' | 'IMAGE';
      referenceId: string;
      sourceLabel?: string | null;
      originalFilename?: string | null;
      mimeType?: string | null;
    } | null;
    failureMessage?: string | null;
  } | null;
  onRecipeCaptureEntryHandled?: () => void;
  onShowToast?: (message: string) => void;
  onDone: () => void;
};

type SurfaceMode = 'week' | 'calendar';
type WorkspaceMode = 'home' | 'plan' | 'recipes';
type MealType = 'BREAKFAST' | 'LUNCH' | 'DINNER';
type RecipePlanBridgeState = {
  recipeId: string;
  recipeTitle: string;
};
type WeekShoppingReviewLine = {
  lineId: string;
  name: string;
  amount: string | null;
  metadataLabels: string[];
  contributorOccurrenceCount: number;
  contributorOccurrenceLabels: string[];
  contributorOccurrenceGroups: {
    title: string;
    occurrenceLabels: string[];
  }[];
  otherListNames: string[];
  hasExpandableDetails: boolean;
};

const DAY_LABELS = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
const MEAL_TYPE_LABELS = {
  BREAKFAST: 'Breakfast',
  LUNCH: 'Lunch',
  DINNER: 'Dinner',
} as const;
const MONTH_LABELS = [
  'Jan',
  'Feb',
  'Mar',
  'Apr',
  'May',
  'Jun',
  'Jul',
  'Aug',
  'Sep',
  'Oct',
  'Nov',
  'Dec',
];

function formatDayLabel(date: Date, dayIndex: number) {
  const dayLabel = DAY_LABELS[dayIndex];
  const day = String(date.getUTCDate()).padStart(2, '0');
  const month = MONTH_LABELS[date.getUTCMonth()];
  return `${dayLabel} ${day} ${month}`;
}

function formatRelativeDailyNavLabel(date: Date) {
  const normalized = new Date(date.getTime());
  normalized.setHours(0, 0, 0, 0);
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const tomorrow = new Date(today.getTime());
  tomorrow.setDate(tomorrow.getDate() + 1);
  const dateLabel = normalized.toLocaleDateString(undefined, { day: 'numeric', month: 'short' });
  if (normalized.getTime() === today.getTime()) {
    return `Today · ${dateLabel}`;
  }
  if (normalized.getTime() === tomorrow.getTime()) {
    return `Tomorrow · ${dateLabel}`;
  }
  const weekday = normalized.toLocaleDateString(undefined, { weekday: 'long' });
  return `${weekday} · ${dateLabel}`;
}

function formatMonthLabel(date: Date) {
  return date.toLocaleDateString(undefined, { month: 'long', year: 'numeric' });
}

function formatWeekRangeLabel(start: Date, end: Date) {
  const sameMonth = start.getUTCMonth() === end.getUTCMonth()
    && start.getUTCFullYear() === end.getUTCFullYear();
  if (sameMonth) {
    return `${MONTH_LABELS[start.getUTCMonth()]} ${start.getUTCDate()}-${end.getUTCDate()}`;
  }
  return `${MONTH_LABELS[start.getUTCMonth()]} ${start.getUTCDate()} - ${MONTH_LABELS[end.getUTCMonth()]} ${end.getUTCDate()}`;
}

function formatRecipeMealAttachment(date: Date, mealType: MealType) {
  const weekday = date.toLocaleDateString(undefined, { weekday: 'short' });
  const day = date.toLocaleDateString(undefined, { day: 'numeric', month: 'short' });
  const mealLabel = MEAL_TYPE_LABELS[mealType].toLowerCase();
  return `${weekday} ${day} · ${mealLabel}`;
}

function formatShoppingAmount(quantity: number | null, unitName: string | null) {
  if (quantity == null || unitName == null) {
    return null;
  }
  const formattedQuantity = Number.isInteger(quantity)
    ? String(quantity)
    : String(quantity).replace(/(\.\d*?[1-9])0+$/u, '$1').replace(/\.0+$/u, '');
  const label = {
    PCS: 'pcs',
    PACK: 'pack',
    KG: 'kg',
    HG: 'hg',
    G: 'g',
    L: 'l',
    DL: 'dl',
    ML: 'ml',
  }[unitName] ?? unitName.toLowerCase();
  return `${formattedQuantity} ${label}`;
}

function formatContributorOccurrenceLabel(contributor: ContributorMealReferenceResponse) {
  const dayLabel = DAY_LABELS[Math.max(0, contributor.dayOfWeek - 1)] ?? 'Day';
  const mealTypeLabel = MEAL_TYPE_LABELS[contributor.mealType].toLowerCase();
  const mealTitle = contributor.mealTitle.trim();
  return `${dayLabel} ${mealTypeLabel} · ${mealTitle}`;
}

function formatContributorSlotLabel(contributor: ContributorMealReferenceResponse) {
  const dayLabel = DAY_LABELS[Math.max(0, contributor.dayOfWeek - 1)] ?? 'Day';
  const mealTypeLabel = MEAL_TYPE_LABELS[contributor.mealType].toLowerCase();
  return `${dayLabel} ${mealTypeLabel}`;
}

function groupContributorOccurrences(contributors: AggregatedIngredientComparisonResponse['need']['contributors']) {
  const groups = new Map<string, { title: string; occurrenceLabels: string[] }>();
  for (const contributor of contributors) {
    const title = contributor.mealTitle.trim();
    const slotLabel = formatContributorSlotLabel(contributor);
    const existing = groups.get(title);
    if (existing) {
      existing.occurrenceLabels.push(slotLabel);
      continue;
    }
    groups.set(title, {
      title,
      occurrenceLabels: [slotLabel],
    });
  }
  return Array.from(groups.values());
}

function formatContributorSummary(contributors: AggregatedIngredientComparisonResponse['need']['contributors']) {
  const contributorCount = contributors.length;
  if (contributorCount === 0) {
    return null;
  }
  if (contributorCount === 1) {
    const title = contributors[0]?.mealTitle.trim() ?? '';
    if (!title) {
      return '1 meal';
    }
    const label = `From ${title}`;
    return label.length <= 22 ? label : '1 meal';
  }
  return `${contributorCount} meals`;
}

function normalizeWeekShoppingName(name: string) {
  return name.trim().toLowerCase();
}

function formatWeekShoppingOtherListLabel(listName: string | null | undefined) {
  if (!listName) {
    return null;
  }
  return `On ${listName}`;
}

function formatRecipeMemorySupport(memory: {
  lastUsedDate: string;
  totalUses: number;
  recentUses: number;
  distinctWeeks: number;
  familiar: boolean;
  frequent: boolean;
  makeSoon: boolean;
  preferenceFit: boolean;
} | null) {
  if (!memory) {
    return null;
  }

  const hasStrongHistorySignal = memory.recentUses > 0
    || memory.familiar
    || memory.frequent
    || memory.preferenceFit
    || memory.totalUses >= 2;

  if (memory.totalUses === 0) {
    return memory.makeSoon ? 'Marked make soon' : null;
  }

  if (!hasStrongHistorySignal && !memory.makeSoon) {
    return null;
  }

  const lastUsedDate = new Date(`${memory.lastUsedDate}T00:00:00`);
  const lastUsedLabel = lastUsedDate.toLocaleDateString(undefined, {
    day: 'numeric',
    month: 'short',
  });
  const lead = memory.familiar
    ? 'Familiar in your plan'
    : memory.recentUses > 0
      ? 'Used recently'
      : memory.frequent
        ? 'Comes up often'
        : 'Used before';
  const trailing = memory.makeSoon
    ? 'Marked make soon'
    : memory.preferenceFit
      ? 'Fits your household'
      : `Last planned ${lastUsedLabel}`;

  return `${lead} · ${trailing}`;
}

export function MealsWeekScreen({
  token,
  recipeCaptureEntryRequest = null,
  onRecipeCaptureEntryHandled,
  onShowToast,
  onDone,
}: Props) {
  const { handleApiError } = useAuth();
  const lastHandledRecipeCaptureEntryRef = useRef<number | null>(null);
  const strings = {
    title: 'Meals',
    homeTitle: 'Meals',
    homeSubtitle: 'Choose where you want to start.',
    homePlanTitle: 'Meal plan',
    homePlanSubtitle: 'Set up the week and keep meals in view.',
    homeRecipesTitle: 'Recipe library',
    homeRecipesSubtitle: 'Browse, save, and return to your recipes.',
    planTitle: 'Meal plan',
    planSubtitle: 'Set up the week and keep meals in view.',
    recipesTitle: 'Recipe library',
    recipesSubtitle: 'Browse, save, and return to your recipes.',
    planWorkspace: 'Plan',
    recipesWorkspace: 'Recipes',
    weeklyPlanner: 'Week',
    calendarOverview: 'Calendar',
    weekLabel: 'Week',
    recipesOverviewTitle: 'Recipes',
    activeRecipesTab: 'Active',
    archivedRecipesTab: 'Archived',
    loadingPlan: 'Loading week plan...',
    loadingMonthOverview: 'Loading calendar...',
    loadingDay: 'Loading day...',
    todayLabel: 'Today',
    planMealTitle: 'Plan a meal',
    editMealTitle: 'Edit meal',
    planMealSlot: 'Plan',
    editMealSlot: 'Edit meal',
    mealsLabel: 'Meals',
    recipeLinkLabel: 'Recipe',
    openRecipeFromDay: 'Open recipe',
    dayLabel: 'Day',
    mealTypeLabel: 'Meal',
    mealTitleLabel: 'What are you eating?',
    mealTitlePlaceholder: 'Meal title',
    recipeLabel: 'Details',
    savedRecipeLabel: 'Saved recipe',
    recipeOptionalLabel: undefined,
    noRecipeAttachedSummary: 'No details yet',
    newRecipeLabel: 'No recipe attached',
    usingRecipeLabel: 'Recipe attached',
    mealSpecificRecipeLabel: 'Recipe for this meal',
    editingSavedRecipeLabel: 'Editing saved recipe',
    useExistingRecipe: 'Attach saved recipe',
    changeRecipe: 'Change recipe',
    openRecipe: 'Open details',
    openSavedRecipe: 'Open recipe',
    addRecipeDetails: 'Add details',
    reuseRecentMeal: 'Choose from your meals',
    recipeSummaryHint: 'Add ingredients, notes, or steps.',
    savedRecipeSummaryHint: 'Saved in Recipe library.',
    loadingRecipe: 'Loading details...',
    planRecipeAction: 'Plan this recipe',
    planRecipeSheetTitle: 'Plan this recipe',
    planRecipeSheetSubtitle: undefined,
    planRecipeWeekLabel: 'Week',
    planRecipeDayLabel: 'Day',
    planRecipeMealLabel: 'Meal',
    planRecipeRecipeLabel: 'Recipe',
    planRecipeReplaceHint: (mealTitle: string) => `Already planned: ${mealTitle}.`,
    confirmPlanRecipe: 'Plan recipe',
    replacePlannedMeal: 'Replace planned meal',
    planningRecipe: 'Planning recipe...',
    recipeSheetEyebrow: 'Recipe',
    mealDetailSheetEyebrow: 'Meal',
    recipeSheetTitle: 'Recipe',
    recipeSheetSubtitle: 'Review and edit the recipe used for this meal.',
    recipeSheetNewRecipeTitle: 'New recipe',
    mealDetailSheetNewTitle: 'Meal details',
    importDraftLabel: 'From link',
    archivedReadOnlyHint: 'This archived recipe stays readable here until you restore it.',
    recipeSheetSavedRecipeContextHint: 'This meal is using a saved recipe.',
    recipeSheetMealSpecificContextHint: 'Your changes are now creating a recipe for this meal.',
    recipeSheetEditingSavedRecipeContextHint: 'You are editing the saved recipe used by this meal.',
    recipeSheetNewRecipeContextHint: 'You are adding recipe details for this meal.',
    recipeMealAttachmentLabel: 'Used for',
    mealDetailSheetHint: undefined,
    mealUsingSavedRecipeHint: 'This meal is using a saved recipe.',
    mealSpecificDetailsHint: 'The saved recipe stays unchanged. Your changes stay with this meal.',
    editSavedRecipeAction: 'Edit saved recipe',
    editRecipeAction: 'Edit recipe',
    mealDetailsTitleLabel: 'Title',
    mealDetailsTitlePlaceholder: 'Optional details title',
    mealDetailsTitleHint: 'Use a different title only if these details need one.',
    mealDetailsContextLabel: 'Extra context',
    mealDetailsSourceLabel: 'Source',
    mealDetailsSourcePlaceholder: 'Optional source or inspiration',
    mealDetailsNoteLabel: 'Note',
    mealDetailsNotePlaceholder: 'Optional note about serving, prep, or reminders',
    mealDetailsInstructionsLabel: 'Cooking guidance',
    mealDetailsInstructionsPlaceholder: 'Add cooking steps or meal-specific guidance',
    recipeNameLabel: 'Recipe name',
    recipeNamePlaceholder: 'Recipe name',
    recipeNameEditHint: undefined,
    recipeContentLabel: 'Recipe details',
    recipeMetadataHint: undefined,
    recipeServingsLabel: 'Servings',
    recipeServingsPlaceholder: 'e.g. 4 servings',
    recipePortionValue: (count: number) => `${count} ${count === 1 ? 'serving' : 'servings'}`,
    recipeSourceLabel: 'Source',
    recipeSourcePlaceholder: 'Book, site, or family note',
    recipeSourceUrlLabel: 'Website',
    recipeSourceUrlPlaceholder: 'Recipe link (optional)',
    recipeShortNoteLabel: 'Short note',
    recipeShortNotePlaceholder: 'Optional note about serving, prep, or reminders',
    recipeShortNoteHint: undefined,
    recipeInstructionsLabel: 'Instructions',
    recipeInstructionsPlaceholder: 'Add the cooking steps or preparation notes',
    recipeInstructionsHint: undefined,
    importInstructionsHint: 'Adjust what needs it.',
    instructionStepCount: (count: number) => `${count} ${count === 1 ? 'step' : 'steps'}`,
    instructionAddNextStep: 'Add next step',
    saveAsNewRecipeHint: 'The saved recipe stays unchanged.',
    editingSavedRecipeHint: 'Changes now update the saved recipe itself.',
    ingredientsLabel: 'Ingredients',
    ingredientsRecipeHint: 'Keep the ingredient list ready with this recipe.',
    savedRecipeIngredientsHint: 'These ingredients stay with this saved recipe.',
    mealDetailsIngredientsHint: undefined,
    importedIngredientsHint: undefined,
    importReviewLabel: 'From link',
    importReviewHint: 'Start where it matters most.',
    recipePickerTitle: 'Attach saved recipe',
    recipePickerHint: 'Saved recipes',
    loadingRecipes: 'Loading recipes...',
    noRecipes: 'No saved recipes yet.',
    makeSoonRecipesTitle: 'Make soon',
    recentlyUsedRecipesTitle: 'Recently used',
    searchRecipesPlaceholder: 'Search recipes',
    noRecipeSearchResults: 'No recipes match this search.',
    noRecipeSearchResultsHint: 'Try a title or source.',
    addRecipeFromRecipes: 'Add recipe',
    addRecipeFromRecipesHint: 'Best results come from a recipe link.',
    addRecipeChooserTitle: 'Add recipe',
    addRecipeChooserSubtitle: 'Best results come from a recipe link.',
    addRecipeFromLink: 'From link',
    addRecipeFromLinkHint: 'Best for recipe websites and shared links.',
    addRecipeCreate: 'Create recipe',
    addRecipeCreateHint: 'Write it yourself or paste recipe text.',
    createRecipeChooserTitle: 'Create recipe',
    createRecipeChooserSubtitle: 'Start from scratch or paste a recipe in.',
    createRecipeWriteYourself: 'Write it yourself',
    createRecipeWriteYourselfHint: 'Start from scratch.',
    createRecipePasteText: 'Paste recipe text',
    createRecipePasteTextHint: 'Start from recipe text you already have.',
    importRecipeTitle: 'From link',
    importRecipeSubtitle: 'Paste a recipe link.',
    importRecipeUrlPlaceholder: 'https://example.com/recipe',
    importRecipeHelpText: 'We’ll bring it into a recipe to review.',
    importRecipeClipboardHint: 'Using link from clipboard',
    importRecipeAction: 'Continue',
    importingRecipeAction: 'Opening recipe...',
    importRecipeTextTitle: 'Paste recipe text',
    importRecipeTextSubtitle: 'Paste the recipe text.',
    importRecipeTextPlaceholder: 'Recipe title\n\nIngredients\n...\n\nInstructions\n...',
    importRecipeTextHelpText: 'We’ll turn it into a recipe to review.',
    importRecipeTextAction: 'Continue',
    importingRecipeTextAction: 'Opening recipe...',
    recipeDestinationSubtitle: undefined,
    savedRecipeContextHint: 'Reusable recipe in Recipes.',
    newSavedRecipeContextHint: undefined,
    importDraftSubtitle: 'Review it before saving.',
    importDraftContextHint: undefined,
    importReviewSourceSummaryTitle: 'Recipe details',
    importReviewSourceSummaryHint: undefined,
    importReviewSourceEmpty: 'No source came through from this import.',
    importReviewSourceUrlEmpty: 'No link came through from this import.',
    saveRecipe: 'Save recipe',
    saveRecipeToLibrary: 'Save to Recipes',
    savingRecipe: 'Saving recipe...',
    saveMealDetails: 'Save meal details',
    savingMealDetails: 'Saving meal details...',
    saveToRecipes: 'Save to Recipes',
    savingToRecipes: 'Saving to Recipes...',
    markMakeSoonAction: 'Make soon',
    clearMakeSoonAction: 'Remove from make soon',
    archiveRecipe: 'Archive recipe',
    archivingRecipe: 'Archiving recipe...',
    restoreRecipe: 'Restore recipe',
    restoringRecipe: 'Restoring recipe...',
    deleteRecipe: 'Delete recipe',
    deletingRecipe: 'Deleting recipe...',
    deleteRecipeBlockedDefault: 'This archived recipe is still used in current or future meal plans.',
    confirmDeleteRecipeTitle: 'Delete recipe?',
    confirmDeleteRecipeMessage: 'This permanently removes the archived recipe from Meals.',
    confirmDeleteRecipeAction: 'Delete',
    cancelDeleteRecipeAction: 'Cancel',
    duplicateRecipeTitle: 'Already in Recipes?',
    duplicateRecipeFromUrlMessage: 'This looks like the same recipe already saved in Recipes.',
    duplicateRecipeFromNameMessage: 'This looks very close to a recipe already saved in Recipes.',
    duplicateRecipeArchivedSuffix: 'The existing recipe is archived.',
    duplicateRecipeOpenExistingAction: 'Open saved recipe',
    duplicateRecipeUseExistingAction: 'Use existing recipe',
    duplicateRecipeSaveCopyAction: 'Save another copy',
    duplicateRecipeCancelAction: 'Cancel',
    createRecipe: 'Create recipe',
    creatingRecipe: 'Creating recipe...',
    archivedRecipeLabel: 'Archived recipe',
    ingredientsEmptyState: 'Optional. Add ingredients when you need them.',
    ingredientsSummarySuffix: 'ingredients',
    ingredientNamePlaceholder: 'Ingredient name',
    quantityPlaceholder: 'Amount',
    addIngredient: 'Add ingredient',
    removeIngredient: 'Remove',
    importedIngredientHint: 'From page:',
    importedIngredientNeedsReviewHint: undefined,
    importedIngredientReviewTag: undefined,
    importedIngredientMarkDone: 'Looks right',
    importedIngredientReviewed: 'Reviewed',
    collapseIngredient: 'Collapse row',
    loadingIngredients: 'Loading ingredients...',
    recentMealsTitle: 'Choose from your meals',
    recentMealsHint: undefined,
    loadingRecentMeals: 'Loading your meals...',
    noRecentMealsToReuse: 'Nothing familiar for this slot yet.',
    recentMealChoicesRecent: 'Recent',
    recentMealChoicesFamiliar: 'Familiar',
    recentMealChoicesMakeSoon: 'Make soon',
    addIngredientsToShoppingAction: 'Add ingredients to shopping',
    reviewShoppingAgainAction: 'Review shopping',
    shoppingReviewTitle: 'Review shopping',
    weekShoppingReviewAction: 'Review week shopping',
    weekShoppingReviewTitle: 'Review week shopping',
    shoppingListLabel: 'Shopping list',
    shoppingReviewNewListLabel: 'New list',
    shoppingReviewNewListPlaceholder: 'List name',
    shoppingReviewCreateListAction: 'Create list',
    shoppingReviewCreatingListAction: 'Creating list...',
    ingredientsAlreadyOnListLabel: 'Already on this list',
    ingredientsToAddLabel: 'Add to this list',
    everythingAlreadyOnListHint: 'Everything from this meal is already on this list.',
    everythingAlreadyOnWeekListHint: 'Everything from this week is already on this list.',
    checkingShoppingListHint: 'Checking this list...',
    loadingWeekShoppingReview: 'Loading week shopping...',
    emptyWeekShoppingReview: 'No recipe ingredients planned for this week yet.',
    loadingShoppingLists: 'Loading shopping lists...',
    weekShoppingSelectionHint: 'Select what you want to add to this list.',
    weekShoppingNothingToAddHint: 'Nothing to add to this list right now.',
    confirmAddIngredientsToShopping: 'Add selected ingredients',
    noShoppingLists: 'No shopping lists yet.',
    shoppingSyncFailed: 'Meal saved, but adding ingredients to shopping failed:',
    saveMeal: 'Save meal',
    savingMeal: 'Saving meal...',
    removeMeal: 'Remove meal',
    removingMeal: 'Removing meal...',
    close: 'Close',
    addingIngredientsToShopping: 'Adding ingredients...',
  };
  const [workspaceMode, setWorkspaceMode] = useState<WorkspaceMode>('home');
  const [surfaceMode, setSurfaceMode] = useState<SurfaceMode>('week');
  const [anchorDate, setAnchorDate] = useState(() => new Date());
  const [selectedDayDetailDate, setSelectedDayDetailDate] = useState<Date | null>(null);
  const [selectedDayDetailMealType, setSelectedDayDetailMealType] = useState<MealType | null>(null);
  const [pendingDayActionOpen, setPendingDayActionOpen] = useState<{
    date: Date;
    dayOfWeek: number;
    mealType: MealType;
    target: 'edit' | 'shopping';
  } | null>(null);
  const [recipePlanBridge, setRecipePlanBridge] = useState<RecipePlanBridgeState | null>(null);
  const [isPlanningRecipe, setIsPlanningRecipe] = useState(false);
  const [recipePlanError, setRecipePlanError] = useState<string | null>(null);
  const [isWeekShoppingReviewOpen, setIsWeekShoppingReviewOpen] = useState(false);
  const [weekShoppingSelectedListId, setWeekShoppingSelectedListId] = useState<string | null>(null);
  const [weekShoppingSelectedLineIds, setWeekShoppingSelectedLineIds] = useState<string[]>([]);
  const [weekShoppingReview, setWeekShoppingReview] = useState<WeekShoppingReviewResponse | null>(null);
  const [isWeekShoppingReviewLoading, setIsWeekShoppingReviewLoading] = useState(false);
  const [weekShoppingReviewError, setWeekShoppingReviewError] = useState<string | null>(null);
  const [isWeekShoppingListCreateOpen, setIsWeekShoppingListCreateOpen] = useState(false);
  const [weekShoppingNewListName, setWeekShoppingNewListName] = useState('');
  const [isAddingWeekShoppingLines, setIsAddingWeekShoppingLines] = useState(false);
  const [weekShoppingPostState, setWeekShoppingPostState] = useState<{
    scopeKey: string;
    listName: string;
  } | null>(null);
  const [selectedDayPlan, setSelectedDayPlan] = useState<WeekPlanResponse | null>(null);
  const [isSelectedDayPlanLoading, setIsSelectedDayPlanLoading] = useState(false);
  const [selectedDayPlanError, setSelectedDayPlanError] = useState<string | null>(null);
  const { year, isoWeek } = useMemo(
    () => getIsoWeekParts(anchorDate),
    [anchorDate]
  );
  const currentWeekScopeKey = `${year}:${isoWeek}`;
  const weekStart = useMemo(
    () => getWeekStartDate(year, isoWeek),
    [year, isoWeek]
  );
  const workflow = useMealsWorkflow({ token, year, isoWeek });
  const { editor, actions } = workflow;
  const plan = workflow.plan;
  const monthOverview = useMonthMealOverview({
    token,
    anchorDate,
    enabled: workspaceMode === 'plan' && surfaceMode === 'calendar',
    currentWeekPlan: plan.data,
  });
  const recipesWorkspace = useMealsRecipesWorkspace({
    token,
    enabled: workspaceMode === 'recipes',
  });
  const addRecipeOptions: MealRecipeCaptureSourceOption[] = [
    {
      icon: 'link-outline',
      title: strings.addRecipeFromLink,
      hint: strings.addRecipeFromLinkHint,
      onPress: recipesWorkspace.addRecipe.chooseLink,
    },
    {
      icon: 'create-outline',
      title: strings.addRecipeCreate,
      hint: strings.addRecipeCreateHint,
      onPress: recipesWorkspace.addRecipe.chooseCreate,
    },
  ];
  const createRecipeOptions: MealRecipeCaptureSourceOption[] = [
    {
      icon: 'create-outline',
      title: strings.createRecipeWriteYourself,
      hint: strings.createRecipeWriteYourselfHint,
      onPress: recipesWorkspace.createRecipe.chooseManual,
    },
    {
      icon: 'document-text-outline',
      title: strings.createRecipePasteText,
      hint: strings.createRecipePasteTextHint,
      onPress: recipesWorkspace.createRecipe.choosePasteText,
    },
  ];
  const recipeMemorySupport = useMemo(
    () => formatRecipeMemorySupport(recipesWorkspace.recipeDetail.recipeMemory),
    [recipesWorkspace.recipeDetail.recipeMemory]
  );

  useEffect(() => {
    if (workspaceMode !== 'recipes' || !recipesWorkspace.recipes.hasLoaded) {
      return;
    }
    void recipesWorkspace.recipes.reload();
  }, [workspaceMode]);
  useEffect(() => {
    if (!recipeCaptureEntryRequest) {
      return;
    }
    if (lastHandledRecipeCaptureEntryRef.current === recipeCaptureEntryRequest.key) {
      return;
    }

    lastHandledRecipeCaptureEntryRef.current = recipeCaptureEntryRequest.key;
    setWorkspaceMode('recipes');

    if (recipeCaptureEntryRequest.failureMessage) {
      onShowToast?.(recipeCaptureEntryRequest.failureMessage);
      onRecipeCaptureEntryHandled?.();
      return;
    }

    const sharedUrl = recipeCaptureEntryRequest.sharedUrl?.trim();
    if (sharedUrl) {
      void recipesWorkspace.importDraft.importSharedRecipeUrl(sharedUrl, {
        onError: (message) => {
          onShowToast?.(message);
        },
        onComplete: () => {
          onRecipeCaptureEntryHandled?.();
        },
      });
      return;
    }

    if (recipeCaptureEntryRequest.sharedAsset?.referenceId?.trim()) {
      onShowToast?.(
        'Shared files and photos are not supported for recipe import right now. Try sharing a recipe link or recipe text instead.'
      );
      onRecipeCaptureEntryHandled?.();
      return;
    }

    onShowToast?.('We could not use that shared item. Try sharing a recipe link or recipe text instead.');
    onRecipeCaptureEntryHandled?.();
  }, [
    onRecipeCaptureEntryHandled,
    onShowToast,
    recipeCaptureEntryRequest,
    recipesWorkspace.importDraft,
  ]);
  useEffect(() => {
    if (recipesWorkspace.recipeDetail.isOpen || !recipePlanBridge) {
      return;
    }
    setRecipePlanBridge(null);
    setRecipePlanError(null);
  }, [recipePlanBridge, recipesWorkspace.recipeDetail.isOpen]);
  const shopping = workflow.shopping;
  const mealsByDay = workflow.mealsByDay;
  const lists = shopping.lists;
  const effectiveListId = editor.effectiveListId;
  const activeWeekShoppingListId = weekShoppingSelectedListId ?? weekShoppingReview?.assessedShoppingListId ?? null;
  const activeWeekShoppingListName = lists.find((list) => list.id === activeWeekShoppingListId)?.name
    ?? weekShoppingReview?.assessedShoppingListName
    ?? null;
  const pendingRecipesDuplicateCandidate = recipesWorkspace.recipeDetail.pendingDuplicateCandidate;
  const pendingMealDuplicateCandidate = editor.pendingDuplicateCandidate;
  const weekShoppingDisplay = useMemo(() => {
    const representedLines: WeekShoppingReviewLine[] = [];
    const addableLines: WeekShoppingReviewLine[] = [];
    const effectiveWeekShoppingList = activeWeekShoppingListId == null
      ? null
      : shopping.lists.find((list) => list.id === activeWeekShoppingListId) ?? null;
    const activeListItems = effectiveWeekShoppingList?.items ?? [];
    const activeListItemsByName = new Map(
      activeListItems.map((item) => [normalizeWeekShoppingName(item.name), item])
    );
    const otherListNamesByIngredient = new Map<string, string[]>();
    for (const list of shopping.lists) {
      if (list.id === activeWeekShoppingListId) {
        continue;
      }
      for (const item of list.items) {
        const normalizedName = normalizeWeekShoppingName(item.name);
        const currentNames = otherListNamesByIngredient.get(normalizedName) ?? [];
        if (!currentNames.includes(list.name)) {
          currentNames.push(list.name);
          otherListNamesByIngredient.set(normalizedName, currentNames);
        }
      }
    }

    for (const ingredient of weekShoppingReview?.ingredients ?? []) {
      const normalizedName = normalizeWeekShoppingName(ingredient.need.normalizedShoppingName);
      const matchingActiveItem = activeListItemsByName.get(normalizedName);
      const contributorOccurrenceLabels = ingredient.need.contributors
        .map(formatContributorOccurrenceLabel)
        .filter((label) => label.trim().length > 0);
      const contributorOccurrenceGroups = groupContributorOccurrences(ingredient.need.contributors);
      const contributorOccurrenceCount = ingredient.need.contributors.length;
      const contributorSummary = formatContributorSummary(ingredient.need.contributors);
      const otherListNames = otherListNamesByIngredient.get(normalizedName) ?? [];
      const crossListSummary = otherListNames.length > 1
        ? `On ${otherListNames.length} lists`
        : formatWeekShoppingOtherListLabel(otherListNames[0]);
      const metadataLabels = matchingActiveItem
        ? [contributorSummary].filter((value): value is string => value != null).slice(0, 2)
        : [crossListSummary, contributorSummary].filter((value): value is string => value != null).slice(0, 2);
      const hasExpandableDetails = otherListNames.length > 1
        || contributorOccurrenceCount > 1
        || (contributorOccurrenceCount === 1 && contributorSummary === '1 meal');
      const line: WeekShoppingReviewLine = {
        lineId: ingredient.need.lineId,
        name: ingredient.need.ingredientName,
        amount: ingredient.comparisonState === 'add_to_list'
          ? formatShoppingAmount(
              ingredient.remainingQuantity ?? ingredient.need.totalQuantity,
              ingredient.need.unitName
            )
          : formatShoppingAmount(ingredient.need.totalQuantity, ingredient.need.unitName),
        metadataLabels,
        contributorOccurrenceCount,
        contributorOccurrenceLabels,
        contributorOccurrenceGroups,
        otherListNames,
        hasExpandableDetails,
      };
      if (matchingActiveItem) {
        representedLines.push(line);
      } else {
        addableLines.push(line);
      }
    }
    representedLines.sort((left, right) => left.name.localeCompare(right.name));
    addableLines.sort((left, right) => left.name.localeCompare(right.name));
    return {
      representedLines,
      addableLines,
    };
  }, [activeWeekShoppingListId, shopping.lists, weekShoppingReview]);
  const currentWeekAddableLineIds = useMemo(
    () => new Set(weekShoppingDisplay.addableLines.map((line) => line.lineId)),
    [weekShoppingDisplay.addableLines]
  );

  async function handleSave() {
    await actions.saveMeal();
  }

  async function handleRemove() {
    await actions.removeMeal();
  }

  async function handleAddIngredientsToShopping() {
    await actions.addIngredientsToShopping();
  }

  function openWeekShoppingReview() {
    setIsWeekShoppingReviewOpen(true);
    setIsWeekShoppingReviewLoading(true);
    setWeekShoppingReviewError(null);
    setIsWeekShoppingListCreateOpen(false);
    setWeekShoppingNewListName('');
  }

  function closeWeekShoppingReview() {
    if (isAddingWeekShoppingLines) {
      return;
    }
    setIsWeekShoppingReviewOpen(false);
    setIsWeekShoppingReviewLoading(false);
    setIsWeekShoppingListCreateOpen(false);
    setWeekShoppingNewListName('');
    setWeekShoppingSelectedLineIds([]);
  }

  function toggleWeekShoppingLine(lineId: string) {
    setWeekShoppingSelectedLineIds((current) => (
      current.includes(lineId)
        ? current.filter((id) => id !== lineId)
        : [...current, lineId]
    ));
  }

  function openWeekShoppingListCreate() {
    setIsWeekShoppingListCreateOpen(true);
  }

  function closeWeekShoppingListCreate() {
    if (shopping.pendingMutation?.kind === 'create-list') {
      return;
    }
    setIsWeekShoppingListCreateOpen(false);
    setWeekShoppingNewListName('');
  }

  async function createWeekShoppingList() {
    const normalizedName = weekShoppingNewListName.trim();
    if (!normalizedName) {
      return;
    }
    const created = await shopping.createList(normalizedName, 'grocery');
    if (!created) {
      return;
    }
    setIsWeekShoppingReviewLoading(true);
    setWeekShoppingReviewError(null);
    setWeekShoppingReview(null);
    setWeekShoppingSelectedListId(created.listId);
    setWeekShoppingSelectedLineIds([]);
    setIsWeekShoppingListCreateOpen(false);
    setWeekShoppingNewListName('');
  }

  async function confirmWeekShoppingAdd() {
    const selectedLineIds = weekShoppingSelectedLineIds.filter((lineId) => currentWeekAddableLineIds.has(lineId));
    if (!activeWeekShoppingListId || selectedLineIds.length === 0 || isAddingWeekShoppingLines) {
      if (selectedLineIds.length !== weekShoppingSelectedLineIds.length) {
        setWeekShoppingSelectedLineIds(selectedLineIds);
      }
      return;
    }
    try {
      setIsAddingWeekShoppingLines(true);
      const updated = await addWeekShoppingReviewLines(
        year,
        isoWeek,
        {
          shoppingListId: activeWeekShoppingListId,
          selectedLineIds,
        },
        { token }
      );
      setWeekShoppingReview(updated);
      setWeekShoppingSelectedListId(updated.assessedShoppingListId);
      setWeekShoppingSelectedLineIds([]);
      if (activeWeekShoppingListName) {
        setWeekShoppingPostState({
          scopeKey: currentWeekScopeKey,
          listName: activeWeekShoppingListName,
        });
      }
      await shopping.reload();
      closeWeekShoppingReview();
    } catch (err) {
      await handleApiError(err);
      setWeekShoppingReviewError(formatApiError(err));
    } finally {
      setIsAddingWeekShoppingLines(false);
    }
  }

  function confirmDeleteArchivedRecipe() {
    if (!recipesWorkspace.recipeDetail.canDeleteRecipe) {
      return;
    }
    Alert.alert(
      strings.confirmDeleteRecipeTitle,
      strings.confirmDeleteRecipeMessage,
      [
        {
          text: strings.cancelDeleteRecipeAction,
          style: 'cancel',
        },
        {
          text: strings.confirmDeleteRecipeAction,
          style: 'destructive',
          onPress: () => {
            void recipesWorkspace.recipeDetail.deleteCurrentRecipe();
          },
        },
      ]
    );
  }

  const weekEnd = useMemo(() => {
    const end = new Date(weekStart.getTime());
    end.setUTCDate(weekStart.getUTCDate() + 6);
    return end;
  }, [weekStart]);

  const isAnchorWeekLoaded = plan.data?.year === year && plan.data?.isoWeek === isoWeek;
  const hasReviewableWeekShopping = isAnchorWeekLoaded && !!plan.data?.hasReviewableWeekShopping;
  const showInitialPlanLoading = workspaceMode === 'plan'
    && surfaceMode === 'week'
    && plan.isInitialLoading
    && !isAnchorWeekLoaded;
  const showInitialMonthOverviewLoading = workspaceMode === 'plan'
    && surfaceMode === 'calendar'
    && monthOverview.isInitialLoading
    && !monthOverview.hasLoaded;
  const currentSurfaceError = workspaceMode === 'plan'
    ? (surfaceMode === 'week' ? plan.error : monthOverview.error)
    : recipesWorkspace.recipes.error;
  const topBarTitle = workspaceMode === 'home'
    ? strings.homeTitle
    : workspaceMode === 'plan'
      ? strings.planTitle
      : strings.recipesTitle;
  const topBarSubtitle = workspaceMode === 'home'
    ? strings.homeSubtitle
    : workspaceMode === 'plan'
      ? strings.planSubtitle
      : strings.recipesSubtitle;
  const topBarIconName = workspaceMode === 'home'
    ? 'restaurant-outline'
    : workspaceMode === 'plan'
      ? 'calendar-outline'
      : 'book-outline';
  const shouldRenderPlanViews = surfaceMode === 'week'
    ? !showInitialPlanLoading && (isAnchorWeekLoaded || !plan.error)
    : !showInitialMonthOverviewLoading && monthOverview.hasLoaded;

  const selectedEditorDate = useMemo(() => {
    if (!editor.selectedDay) {
      return anchorDate;
    }
    const date = new Date(weekStart.getTime());
    date.setUTCDate(weekStart.getUTCDate() + editor.selectedDay - 1);
    return new Date(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate());
  }, [anchorDate, editor.selectedDay, weekStart]);

  const monthGridCells = useMemo(() => buildMonthGridCells(anchorDate), [anchorDate]);
  const recipeMealAttachmentValue = useMemo(() => {
    if (!editor.selectedMealType) {
      return '';
    }
    return formatRecipeMealAttachment(selectedEditorDate, editor.selectedMealType);
  }, [editor.selectedMealType, selectedEditorDate]);
  const isViewingAttachedSavedRecipe = editor.isSelectedRecipeSavedInRecipes
    && !editor.hasModifiedPickedRecipe
    && !editor.isEditingSavedRecipeDirectly;
  const isMealVariationFromSavedRecipe = editor.hasModifiedPickedRecipe
    && !editor.isEditingSavedRecipeDirectly;
  const currentWeekDayOptions = useMemo(() => (
    Array.from({ length: 7 }, (_, index) => {
      const utcDate = new Date(weekStart.getTime());
      utcDate.setUTCDate(weekStart.getUTCDate() + index);
      const localDate = new Date(utcDate.getUTCFullYear(), utcDate.getUTCMonth(), utcDate.getUTCDate());
      return {
        dayOfWeek: index + 1,
        date: localDate,
        label: formatDayLabel(localDate, index),
        shortLabel: localDate.toLocaleDateString(undefined, { weekday: 'short', day: 'numeric' }),
      };
    })
  ), [weekStart]);
  const currentWeekMealEntries = useMemo(() => (
    Array.from(mealsByDay.entries()).flatMap(([dayOfWeek, meals]) => (
      meals.map((meal) => ({
        dayOfWeek,
        mealType: meal.mealType,
        mealTitle: meal.mealTitle,
      }))
    ))
  ), [mealsByDay]);
  const defaultRecipePlanDayOfWeek = useMemo(() => {
    const today = new Date();
    const todayWeek = getIsoWeekParts(today);
    if (todayWeek.year === year && todayWeek.isoWeek === isoWeek) {
      const dayOfWeek = today.getDay();
      return dayOfWeek === 0 ? 7 : dayOfWeek;
    }
    return 1;
  }, [isoWeek, year]);

  const periodSummary = useMemo(() => {
    if (surfaceMode === 'week') {
      return {
        primary: formatWeekRangeLabel(weekStart, weekEnd),
        secondary: `${strings.weekLabel} ${isoWeek}`,
      };
    }
    return {
      primary: formatMonthLabel(anchorDate),
      secondary: null,
    };
  }, [
    anchorDate,
    isoWeek,
    strings.weekLabel,
    surfaceMode,
    weekEnd,
    weekStart,
  ]);

  const selectedDayDetail = useMemo(() => {
    if (!selectedDayDetailDate) {
      return null;
    }
    const normalized = new Date(selectedDayDetailDate.getTime());
    const { year: selectedYear, isoWeek: selectedIsoWeek } = getIsoWeekParts(normalized);
    const dayOfWeek = normalized.getDay() || 7;
    return {
      date: normalized,
      dayOfWeek,
      year: selectedYear,
      isoWeek: selectedIsoWeek,
      title: formatRelativeDailyNavLabel(normalized),
      subtitle: `${strings.weekLabel} ${selectedIsoWeek} · ${selectedYear}`,
    };
  }, [selectedDayDetailDate, strings.weekLabel]);

  const isSelectedDayInAnchorWeek = selectedDayDetail
    ? plan.data?.year === selectedDayDetail.year && plan.data?.isoWeek === selectedDayDetail.isoWeek
    : false;
  const effectiveSelectedDayPlan = isSelectedDayInAnchorWeek ? plan.data : selectedDayPlan;

  useEffect(() => {
    let cancelled = false;

    if (!selectedDayDetail || isSelectedDayInAnchorWeek) {
      setSelectedDayPlan(null);
      setSelectedDayPlanError(null);
      setIsSelectedDayPlanLoading(false);
      return () => {
        cancelled = true;
      };
    }

    if (!token) {
      setSelectedDayPlan(null);
      setSelectedDayPlanError('Missing token');
      setIsSelectedDayPlanLoading(false);
      return () => {
        cancelled = true;
      };
    }

    setSelectedDayPlan(null);
    setSelectedDayPlanError(null);
    setIsSelectedDayPlanLoading(true);

    void getWeekPlan(selectedDayDetail.year, selectedDayDetail.isoWeek, { token })
      .then((data) => {
        if (cancelled) {
          return;
        }
        setSelectedDayPlan(data);
      })
      .catch(async (err) => {
        if (cancelled) {
          return;
        }
        await handleApiError(err);
        if (cancelled) {
          return;
        }
        setSelectedDayPlanError(formatApiError(err));
      })
      .finally(() => {
        if (!cancelled) {
          setIsSelectedDayPlanLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [handleApiError, isSelectedDayInAnchorWeek, selectedDayDetail, token]);

  useEffect(() => {
    if (weekShoppingPostState?.scopeKey === currentWeekScopeKey) {
      return;
    }
    setWeekShoppingPostState(null);
  }, [currentWeekScopeKey, weekShoppingPostState]);

  useEffect(() => {
    setWeekShoppingSelectedListId(null);
    setWeekShoppingSelectedLineIds([]);
    setWeekShoppingReview(null);
    setWeekShoppingReviewError(null);
    setIsWeekShoppingListCreateOpen(false);
    setWeekShoppingNewListName('');
  }, [currentWeekScopeKey]);

  useEffect(() => {
    setWeekShoppingSelectedLineIds([]);
  }, [activeWeekShoppingListId]);

  useEffect(() => {
    if (weekShoppingSelectedLineIds.length === 0) {
      return;
    }
    setWeekShoppingSelectedLineIds((current) => current.filter((lineId) => currentWeekAddableLineIds.has(lineId)));
  }, [currentWeekAddableLineIds, weekShoppingSelectedLineIds.length]);

  useEffect(() => {
    let cancelled = false;

    if (!isWeekShoppingReviewOpen) {
      return () => {
        cancelled = true;
      };
    }

    setIsWeekShoppingReviewLoading(true);
    setWeekShoppingReviewError(null);
    setWeekShoppingReview(null);
    setWeekShoppingSelectedLineIds([]);

    void getWeekShoppingReview(
      year,
      isoWeek,
      weekShoppingSelectedListId ? { shoppingListId: weekShoppingSelectedListId } : {},
      { token }
    )
      .then((review) => {
        if (cancelled) {
          return;
        }
        setWeekShoppingReview(review);
        setWeekShoppingSelectedLineIds([]);
      })
      .catch(async (err) => {
        if (cancelled) {
          return;
        }
        await handleApiError(err);
        if (cancelled) {
          return;
        }
        setWeekShoppingReview(null);
        setWeekShoppingReviewError(formatApiError(err));
      })
      .finally(() => {
        if (!cancelled) {
          setIsWeekShoppingReviewLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [handleApiError, isWeekShoppingReviewOpen, isoWeek, token, weekShoppingSelectedListId, year]);

  useEffect(() => {
    if (!isWeekShoppingReviewOpen) {
      return;
    }
    if (isWeekShoppingReviewLoading) {
      return;
    }
    if (weekShoppingSelectedListId != null) {
      return;
    }
    if (weekShoppingReview?.assessedShoppingListId) {
      return;
    }
    if (!shopping.hasLoaded || shopping.lists.length === 0) {
      return;
    }
    setWeekShoppingSelectedListId(shopping.lists[0].id);
  }, [
    isWeekShoppingReviewOpen,
    isWeekShoppingReviewLoading,
    shopping.hasLoaded,
    shopping.lists,
    weekShoppingReview?.assessedShoppingListId,
    weekShoppingSelectedListId,
  ]);

  const selectedDayMeals = useMemo(() => {
    if (!selectedDayDetail || !effectiveSelectedDayPlan) {
      return [];
    }
    return effectiveSelectedDayPlan.meals
      .filter((meal) => meal.dayOfWeek === selectedDayDetail.dayOfWeek)
      .map((meal) => ({
        dayOfWeek: meal.dayOfWeek,
        mealType: meal.mealType as MealType,
        mealTitle: meal.mealTitle,
        recipeId: meal.recipeId,
        recipeTitle: meal.recipeTitle,
        shoppingListName: meal.shoppingListId
          ? (shopping.lists.find((list) => list.id === meal.shoppingListId)?.name ?? null)
          : null,
      }))
      .sort((a, b) => {
        const order = { BREAKFAST: 0, LUNCH: 1, DINNER: 2 } as const;
        return order[a.mealType] - order[b.mealType];
      });
  }, [effectiveSelectedDayPlan, selectedDayDetail, shopping.lists]);
  const dayDetailError = selectedDayDetail
    ? (isSelectedDayInAnchorWeek ? plan.error : selectedDayPlanError)
    : null;
  const isDayDetailLoading = !!selectedDayDetail
    && !effectiveSelectedDayPlan
    && !dayDetailError
    && (isSelectedDayInAnchorWeek ? plan.isInitialLoading : isSelectedDayPlanLoading);

  useEffect(() => {
    if (!pendingDayActionOpen) {
      return;
    }
    const { year: pendingYear, isoWeek: pendingIsoWeek } = getIsoWeekParts(pendingDayActionOpen.date);
    if (plan.data?.year !== pendingYear || plan.data?.isoWeek !== pendingIsoWeek) {
      return;
    }
    if (pendingDayActionOpen.target === 'edit') {
      actions.openEditor(pendingDayActionOpen.dayOfWeek, pendingDayActionOpen.mealType);
    } else {
      actions.openShoppingReviewForMeal(pendingDayActionOpen.dayOfWeek, pendingDayActionOpen.mealType);
    }
    setPendingDayActionOpen(null);
  }, [actions, pendingDayActionOpen, plan.data]);

  function openDayDetail(date: Date, focusedMealType: MealType | null = null) {
    const nextDate = new Date(date.getTime());
    setSelectedDayDetailDate(nextDate);
    setSelectedDayDetailMealType(focusedMealType);
  }

  function closeDayDetail() {
    if (editor.isActionPending) {
      return;
    }
    setSelectedDayDetailDate(null);
    setSelectedDayDetailMealType(null);
  }

  function openMealFromDayDetail(mealType: MealType) {
    if (!selectedDayDetail) {
      return;
    }
    setPendingDayActionOpen({
      date: selectedDayDetail.date,
      dayOfWeek: selectedDayDetail.dayOfWeek,
      mealType,
      target: 'edit',
    });
    setAnchorDate(selectedDayDetail.date);
    setSelectedDayDetailMealType(mealType);
  }

  function openRecipeFromDayDetail(mealType: MealType) {
    if (!selectedDayDetail) {
      return;
    }
    const meal = selectedDayMeals.find((entry) => entry.mealType === mealType);
    if (!meal?.recipeId) {
      return;
    }
    setSelectedDayDetailMealType(mealType);
    void recipesWorkspace.recipes.openRecipe(meal.recipeId);
  }

  function openShoppingFromDayDetail(mealType: MealType) {
    if (!selectedDayDetail) {
      return;
    }
    setPendingDayActionOpen({
      date: selectedDayDetail.date,
      dayOfWeek: selectedDayDetail.dayOfWeek,
      mealType,
      target: 'shopping',
    });
    setAnchorDate(selectedDayDetail.date);
    setSelectedDayDetailMealType(mealType);
  }

  function openRecipePlanBridge() {
    const recipeId = recipesWorkspace.recipeDetail.recipeId;
    if (!recipeId || !recipesWorkspace.recipeDetail.isReadMode || recipesWorkspace.recipeDetail.isArchivedRecipe) {
      return;
    }
    setRecipePlanError(null);
    setRecipePlanBridge({
      recipeId,
      recipeTitle: recipesWorkspace.recipeDetail.recipeTitle.trim() || strings.recipeSheetTitle,
    });
  }

  function closeRecipePlanBridge() {
    if (isPlanningRecipe) {
      return;
    }
    setRecipePlanBridge(null);
    setRecipePlanError(null);
  }

  async function confirmRecipePlanBridge(selection: {
    dayOfWeek: number;
    mealType: MealType;
    date: Date;
  }) {
    if (!recipePlanBridge) {
      return;
    }

    setIsPlanningRecipe(true);
    setRecipePlanError(null);
    try {
      const saved = await plan.addMeal(selection.dayOfWeek, selection.mealType, {
        mealTitle: recipePlanBridge.recipeTitle,
        recipeId: recipePlanBridge.recipeId,
        mealType: selection.mealType,
        targetShoppingListId: null,
        selectedIngredientPositions: null,
      });
      if (!saved) {
        return;
      }
      recipesWorkspace.recipeDetail.closeRecipeDetail();
      setRecipePlanBridge(null);
      setWorkspaceMode('plan');
      setSurfaceMode('week');
      setAnchorDate(selection.date);
      setSelectedDayDetailDate(selection.date);
    } catch (err) {
      setRecipePlanError(formatApiError(err));
    } finally {
      setIsPlanningRecipe(false);
    }
  }

  function shiftCurrentPeriod(direction: -1 | 1) {
    if (surfaceMode === 'week') {
      setAnchorDate(addDays(anchorDate, direction * 7));
      return;
    }
    setAnchorDate(addDays(anchorDate, direction * 30));
  }

  function closeEditor() {
    actions.closeEditor();
  }

  function handleMealsBack() {
    if (workspaceMode === 'recipes' && recipesWorkspace.recipes.listMode === 'archived') {
      recipesWorkspace.recipes.showActiveRecipes();
      return;
    }
    if (workspaceMode !== 'home') {
      setWorkspaceMode('home');
      return;
    }
    onDone();
  }

  useAppBackHandler({
    canGoBack: true,
    onGoBack: handleMealsBack,
    isOverlayOpen:
      !!selectedDayDetailDate
      || isWeekShoppingReviewOpen
      || !!recipePlanBridge
      || editor.isOpen
      || editor.isRecipeDetailOpen
      || editor.isRecipePickerOpen
      || editor.isRecentMealsOpen
      || editor.isShoppingReviewOpen
      || recipesWorkspace.addRecipe.isOpen
      || recipesWorkspace.importDraft.isOpen
      || recipesWorkspace.textImportDraft.isOpen
      || recipesWorkspace.recipeDetail.isOpen,
    onCloseOverlay: recipePlanBridge
        ? closeRecipePlanBridge
      : isWeekShoppingReviewOpen
        ? closeWeekShoppingReview
      : editor.isRecipeDetailOpen
        ? editor.closeRecipeDetail
      : editor.isRecipePickerOpen
        ? editor.closeRecipePicker
      : editor.isRecentMealsOpen
        ? editor.closeRecentMeals
      : editor.isShoppingReviewOpen
        ? editor.closeShoppingReview
        : recipesWorkspace.textImportDraft.isOpen
          ? recipesWorkspace.textImportDraft.closeImportRecipeText
        : recipesWorkspace.addRecipe.isOpen
          ? recipesWorkspace.addRecipe.closeAddRecipe
        : recipesWorkspace.importDraft.isOpen
          ? recipesWorkspace.importDraft.closeImportRecipe
        : recipesWorkspace.recipeDetail.isOpen
          ? recipesWorkspace.recipeDetail.closeRecipeDetail
        : editor.isOpen
          ? closeEditor
          : closeDayDetail,
  });

  return (
    <AppScreen
      scroll={false}
      contentStyle={styles.screenContent}
      header={(
        <TopBar
          title={topBarTitle}
          subtitle={topBarSubtitle}
          icon={<Ionicons name={topBarIconName} />}
          accentKey="meals"
          right={<BackIconButton onPress={handleMealsBack} />}
        />
      )}
    >
      <View style={styles.contentOffset}>
        <View style={styles.mainLayout}>
          <ScrollView
            style={styles.mainScroll}
            contentContainerStyle={styles.mainScrollContent}
            refreshControl={(
              <RefreshControl
                refreshing={workspaceMode === 'plan'
                  ? (surfaceMode === 'week' ? plan.isRefreshing : monthOverview.isRefreshing)
                  : workspaceMode === 'recipes'
                    ? recipesWorkspace.recipes.isRefreshing
                    : false}
                onRefresh={() => {
                  if (workspaceMode === 'plan') {
                    void (surfaceMode === 'week' ? plan.reload() : monthOverview.reload());
                    return;
                  }
                  if (workspaceMode === 'recipes') {
                    void recipesWorkspace.recipes.reload();
                  }
                }}
              />
            )}
          >
            <View style={styles.contentInner}>
              {workspaceMode === 'home' ? (
                <View style={styles.homeLayout}>
                  <View style={styles.homeDestinations}>
                    <Pressable
                      onPress={() => setWorkspaceMode('plan')}
                      style={({ pressed }) => [
                        styles.homeDestination,
                        pressed ? styles.homeDestinationPressed : null,
                      ]}
                    >
                      <View style={styles.homeDestinationIcon}>
                        <Ionicons name="calendar-outline" size={20} color={theme.colors.feature.meals} />
                      </View>
                      <View style={styles.homeDestinationCopy}>
                        <Text style={styles.homeDestinationTitle}>{strings.homePlanTitle}</Text>
                        <Text style={styles.homeDestinationSubtitle}>{strings.homePlanSubtitle}</Text>
                      </View>
                      <Ionicons name="chevron-forward" size={18} color={theme.colors.textSecondary} />
                    </Pressable>

                    <Pressable
                      onPress={() => setWorkspaceMode('recipes')}
                      style={({ pressed }) => [
                        styles.homeDestination,
                        pressed ? styles.homeDestinationPressed : null,
                      ]}
                    >
                      <View style={styles.homeDestinationIcon}>
                        <Ionicons name="book-outline" size={20} color={theme.colors.feature.meals} />
                      </View>
                      <View style={styles.homeDestinationCopy}>
                        <Text style={styles.homeDestinationTitle}>{strings.homeRecipesTitle}</Text>
                        <Text style={styles.homeDestinationSubtitle}>{strings.homeRecipesSubtitle}</Text>
                      </View>
                      <Ionicons name="chevron-forward" size={18} color={theme.colors.textSecondary} />
                    </Pressable>
                  </View>
                </View>
              ) : workspaceMode === 'plan' ? (
                <>
                  <AppCard style={styles.controlsCard}>
                    <View style={styles.planSurfaceRow}>
                      <AppSegmentedControl
                        options={[
                          { value: 'week', label: strings.weeklyPlanner },
                          { value: 'calendar', label: strings.calendarOverview },
                        ]}
                        value={surfaceMode}
                        onChange={setSurfaceMode}
                        accentKey="meals"
                      />
                    </View>
                    <View style={styles.periodControlsRow}>
                      <Pressable
                        onPress={() => shiftCurrentPeriod(-1)}
                        style={({ pressed }) => [
                          styles.periodNavButton,
                          pressed ? styles.periodNavButtonPressed : null,
                        ]}
                      >
                        <Ionicons name="chevron-back" size={18} color={theme.colors.text} />
                      </Pressable>
                      <View style={styles.periodSummary}>
                        <Text style={styles.periodSummaryPrimary}>
                          {periodSummary.primary}
                        </Text>
                        {periodSummary.secondary ? (
                          <Subtle style={styles.periodSummarySecondary}>
                            {periodSummary.secondary}
                          </Subtle>
                        ) : null}
                      </View>
                      <Pressable
                        onPress={() => shiftCurrentPeriod(1)}
                        style={({ pressed }) => [
                          styles.periodNavButton,
                          pressed ? styles.periodNavButtonPressed : null,
                        ]}
                      >
                        <Ionicons name="chevron-forward" size={18} color={theme.colors.text} />
                      </Pressable>
                    </View>
                    {surfaceMode === 'week' && hasReviewableWeekShopping ? (
                      <View style={styles.weekShoppingActionRow}>
                        <AppButton
                          title={strings.weekShoppingReviewAction}
                          onPress={openWeekShoppingReview}
                          variant="ghost"
                        />
                        {weekShoppingPostState?.scopeKey === currentWeekScopeKey ? (
                          <Subtle style={styles.weekShoppingPostState}>
                            {`Added to ${weekShoppingPostState.listName}`}
                          </Subtle>
                        ) : null}
                      </View>
                    ) : null}
                  </AppCard>

                  {showInitialPlanLoading ? <Subtle>{strings.loadingPlan}</Subtle> : null}
                  {showInitialMonthOverviewLoading ? <Subtle>{strings.loadingMonthOverview}</Subtle> : null}
                  {currentSurfaceError ? <Text style={styles.error}>{currentSurfaceError}</Text> : null}

                  {shouldRenderPlanViews ? (
                    surfaceMode === 'week' ? (
                      <MealsWeeklyView
                        weekStart={weekStart}
                        mealsByDay={mealsByDay}
                        onOpenDay={openDayDetail}
                        formatDayLabel={formatDayLabel}
                        DAY_LABELS={DAY_LABELS}
                        MEAL_TYPE_LABELS={MEAL_TYPE_LABELS}
                        styles={styles}
                        todayLabel={strings.todayLabel}
                      />
                    ) : (
                      <AppCard style={styles.calendarCard}>
                        <MealsMonthlyView
                          monthGridCells={monthGridCells}
                          monthMealCountByDateKey={monthOverview.mealCountByDateKey}
                          toDateKey={toDateKey}
                          isTodayDate={isTodayDate}
                          styles={styles}
                          onPressDay={(date) => {
                            openDayDetail(new Date(date.getTime()));
                          }}
                          weekdayLabels={DAY_LABELS}
                        />
                      </AppCard>
                    )
                  ) : null}
                </>
              ) : (
                <MealsRecipesView
                  recipes={recipesWorkspace.recipes.items}
                  searchQuery={recipesWorkspace.recipes.searchQuery}
                  listMode={recipesWorkspace.recipes.listMode}
                  browseMode={recipesWorkspace.recipes.browseMode}
                  activeCount={recipesWorkspace.recipes.activeCount}
                  archivedCount={recipesWorkspace.recipes.archivedCount}
                  makeSoonCount={recipesWorkspace.recipes.makeSoonCount}
                  recentCount={recipesWorkspace.recipes.recentCount}
                  isLoading={recipesWorkspace.recipes.isInitialLoading}
                  error={recipesWorkspace.recipes.error}
                  onShowArchived={recipesWorkspace.recipes.showArchivedRecipes}
                  onShowAllRecipes={recipesWorkspace.recipes.showAllBrowseRecipes}
                  onShowMakeSoonRecipes={recipesWorkspace.recipes.showMakeSoonBrowseRecipes}
                  onShowRecentRecipes={recipesWorkspace.recipes.showRecentBrowseRecipes}
                  onChangeSearchQuery={recipesWorkspace.recipes.setSearchQuery}
                  onOpenRecipe={recipesWorkspace.recipes.openRecipe}
                  onAddRecipe={recipesWorkspace.addRecipe.openAddRecipe}
                  strings={{
                    title: strings.recipesOverviewTitle,
                    addRecipe: strings.addRecipeFromRecipes,
                    addRecipeHint: strings.addRecipeFromRecipesHint,
                    archivedAction: 'Archived recipes',
                    archivedTitle: 'Archived recipes',
                    savedRecipesLabel: 'Saved recipes',
                    makeSoonTitle: strings.makeSoonRecipesTitle,
                    recentlyUsedTitle: strings.recentlyUsedRecipesTitle,
                    browseAllLabel: 'All',
                    browseMakeSoonLabel: 'Make soon',
                    browseRecentLabel: 'Recent',
                    searchPlaceholder: strings.searchRecipesPlaceholder,
                    loadingRecipes: strings.loadingRecipes,
                    noRecipes: strings.noRecipes,
                    noRecipesHint: 'Add a recipe from a link, pasted text, or a clean draft to start the library.',
                    noMakeSoonRecipes: 'Nothing marked for make soon yet.',
                    noMakeSoonRecipesHint: 'Use make soon on saved recipes you want to come back to quickly.',
                    noRecentRecipes: 'No recent recipes yet.',
                    noRecentRecipesHint: 'Recipes you actually plan will show up here for quick re-finding.',
                    noArchivedRecipes: 'No archived recipes yet.',
                    noArchivedRecipesHint: 'Archived recipes stay here until you restore them.',
                    noSearchResults: strings.noRecipeSearchResults,
                    noSearchResultsHint: strings.noRecipeSearchResultsHint,
                    duplicateNameHint: (count) => `${count} recipes share this name`,
                    similarNameHint: 'Similar title nearby',
                    recipeCountLabel: (count) => count === 1 ? '1 saved recipe' : `${count} saved recipes`,
                    archivedCountLabel: (count) => count === 1 ? '1 archived recipe' : `${count} archived recipes`,
                    matchingRecipesLabel: (count) => count === 1 ? '1 matching recipe' : `${count} matching recipes`,
                  }}
                />
              )}
            </View>
          </ScrollView>
        </View>
      </View>

      {selectedDayDetail ? (
        <MealDayDetailSheet
          title={selectedDayDetail.title}
          subtitle={selectedDayDetail.subtitle}
          meals={selectedDayMeals}
          mealTypeLabels={MEAL_TYPE_LABELS}
          focusedMealType={selectedDayDetailMealType}
          isLoading={isDayDetailLoading}
          error={dayDetailError}
          onOpenMeal={openMealFromDayDetail}
          onOpenRecipe={openRecipeFromDayDetail}
          onOpenShopping={openShoppingFromDayDetail}
          onClose={closeDayDetail}
          strings={{
            title: strings.title,
            close: strings.close,
            loadingDay: strings.loadingDay,
            emptyDayTitle: 'Nothing planned yet',
            emptyDayHint: 'Start with the meal you want to add for this day.',
            addMeal: strings.planMealSlot,
            editMeal: strings.editMealSlot,
            planMealForSlot: (mealLabel: string) => `${strings.planMealSlot} ${mealLabel.toLowerCase()}`,
            emptySlotTitle: (mealLabel: string) => `Nothing planned for ${mealLabel.toLowerCase()}`,
            titleOnlyMealHint: undefined,
            recipeBackedMealHint: (mealTitle: string, recipeTitle: string) => {
              const trimmedMealTitle = mealTitle.trim().toLocaleLowerCase();
              const trimmedRecipeTitle = recipeTitle.trim();
              if (trimmedRecipeTitle.length === 0) {
                return null;
              }
              if (trimmedRecipeTitle.toLocaleLowerCase() === trimmedMealTitle) {
                return null;
              }
              return trimmedRecipeTitle;
            },
            openRecipe: strings.openRecipeFromDay,
            openShopping: strings.addIngredientsToShoppingAction,
            reviewShopping: strings.reviewShoppingAgainAction,
            addedToShopping: (listName?: string | null) => listName ? `Added to ${listName}` : 'Added to shopping',
          }}
        />
      ) : null}

      {editor.isOpen ? (
        <MealEditorSheet
          initialDate={selectedEditorDate}
          onClose={closeEditor}
          onSave={handleSave}
          onRemove={handleRemove}
          selectedMealType={editor.selectedMealType}
          onSelectMealType={editor.setSelectedMealType}
          mealTypeLabels={MEAL_TYPE_LABELS}
          mealTitle={editor.mealTitle}
          onChangeMealTitle={editor.setMealTitle}
          recipeTitle={editor.recipeTitle}
          ingredientRows={editor.ingredientRows}
          isRecipeLoading={editor.isRecipeLoading}
          onOpenRecipeDetail={editor.openRecipeDetail}
          onOpenRecipePicker={editor.openRecipePicker}
          onOpenRecentMeals={editor.openRecentMeals}
          showRecipePickerAction={editor.showRecipePickerAction}
          hasIngredients={editor.hasIngredients}
          hasRecipeDraftContent={editor.hasRecipeDraftContent}
          hasExistingMeal={!!editor.selectedMeal?.mealTitle}
          hasExistingRecipe={editor.isSelectedRecipeSavedInRecipes}
          isSavingMeal={editor.isSavingMeal}
          isRemovingMeal={editor.isRemovingMeal}
          isActionPending={editor.isActionPending}
          strings={{
            planMealTitle: strings.planMealTitle,
            editMealTitle: strings.editMealTitle,
            mealTitleLabel: strings.mealTitleLabel,
            mealTitlePlaceholder: strings.mealTitlePlaceholder,
            mealTypeLabel: strings.mealTypeLabel,
            recipeLabel: strings.recipeLabel,
            savedRecipeLabel: strings.savedRecipeLabel,
            recipeOptionalLabel: strings.recipeOptionalLabel,
            noRecipeAttached: strings.noRecipeAttachedSummary,
            useExistingRecipe: strings.useExistingRecipe,
            changeRecipe: strings.changeRecipe,
            openRecipe: strings.openRecipe,
            openSavedRecipe: strings.openSavedRecipe,
            addRecipeDetails: strings.addRecipeDetails,
            reuseRecentMeal: strings.reuseRecentMeal,
            recipeSummaryHint: strings.recipeSummaryHint,
            savedRecipeSummaryHint: strings.savedRecipeSummaryHint,
            loadingRecipe: strings.loadingRecipe,
            ingredientsSummarySuffix: strings.ingredientsSummarySuffix,
            saveMeal: strings.saveMeal,
            savingMeal: strings.savingMeal,
            removeMeal: strings.removeMeal,
            removingMeal: strings.removingMeal,
            close: strings.close,
          }}
        />
      ) : null}

      {editor.isRecipeDetailOpen ? (
        <MealRecipeDetailSheet
          recipeTitle={editor.recipeTitle}
          onChangeRecipeTitle={editor.setRecipeTitle}
          recipeSource={editor.recipeSource}
          onChangeRecipeSource={editor.setRecipeSource}
          recipeServings={editor.recipeServings}
          onChangeRecipeServings={editor.setRecipeServings}
          recipeShortNote={editor.recipeShortNote}
          onChangeRecipeShortNote={editor.setRecipeShortNote}
          recipeInstructions={editor.recipeInstructions}
          onChangeRecipeInstructions={editor.setRecipeInstructions}
          ingredientRows={editor.ingredientRows}
          isRecipeLoading={editor.isRecipeLoading}
          hasExistingRecipe={!!editor.selectedMealRecipeId}
          hasIngredients={editor.hasIngredients}
          showSaveAsNewRecipeHint={editor.hasModifiedPickedRecipe}
          canEnterSavedRecipeEditMode={editor.canEnterSavedRecipeEditMode}
          isEditingSavedRecipeDirectly={editor.isEditingSavedRecipeDirectly}
          isActionPending={editor.isActionPending}
          showTitleField={editor.isSelectedRecipeSavedInRecipes}
          showMetadataSection={editor.isSelectedRecipeSavedInRecipes}
          onAddIngredientRow={editor.addIngredientRow}
          onRemoveIngredientRow={editor.removeIngredientRow}
          onChangeIngredientName={editor.setIngredientName}
          onChangeIngredientQuantity={editor.setIngredientQuantity}
          onToggleIngredientUnit={editor.setIngredientUnit}
          onStartEditingSavedRecipeDirectly={editor.startEditingSavedRecipeDirectly}
          onSave={isViewingAttachedSavedRecipe ? undefined : handleSave}
          onSaveToRecipes={editor.canSaveToRecipes ? actions.saveToRecipes : undefined}
          onClose={editor.closeRecipeDetail}
          isSaving={editor.isSavingMeal}
          isSavingToRecipes={editor.isSavingToRecipes}
          error={editor.recipeLoadError}
          strings={{
            eyebrow: strings.mealDetailSheetEyebrow,
            title: strings.recipeSheetTitle,
            subtitle: undefined,
            newRecipeLabel: 'Meal details',
            usingRecipeLabel: editor.isSelectedRecipeSavedInRecipes ? 'Saved recipe' : 'Meal details',
            mealSpecificRecipeLabel: 'Meal variation',
            editingSavedRecipeLabel: 'Saved recipe',
            importDraftLabel: strings.importDraftLabel,
            newRecipeTitle: strings.mealDetailSheetNewTitle,
            recipeContextHint: editor.isEditingSavedRecipeDirectly
              ? undefined
              : editor.hasModifiedPickedRecipe
                ? strings.mealSpecificDetailsHint
                : editor.isSelectedRecipeSavedInRecipes
                  ? strings.mealUsingSavedRecipeHint
                  : strings.mealDetailSheetHint,
            mealAttachmentLabel: strings.recipeMealAttachmentLabel,
            mealAttachmentValue: recipeMealAttachmentValue,
            editSavedRecipeAction: strings.editSavedRecipeAction,
            editingSavedRecipeHint: strings.editingSavedRecipeHint,
            recipeNameLabel: isViewingAttachedSavedRecipe
              ? 'Saved recipe'
              : strings.mealDetailsTitleLabel,
            recipeNamePlaceholder: strings.mealDetailsTitlePlaceholder,
            recipeNameEditHint: isViewingAttachedSavedRecipe
              ? strings.recipeNameEditHint
              : strings.mealDetailsTitleHint,
            recipeContentLabel: strings.mealDetailsContextLabel,
            recipeMetadataHint: undefined,
            recipeServingsLabel: strings.recipeServingsLabel,
            recipeServingsPlaceholder: strings.recipeServingsPlaceholder,
            recipePortionValue: strings.recipePortionValue,
            recipeSourceLabel: strings.mealDetailsSourceLabel,
            recipeSourcePlaceholder: strings.mealDetailsSourcePlaceholder,
            recipeShortNoteLabel: strings.mealDetailsNoteLabel,
            recipeShortNotePlaceholder: strings.mealDetailsNotePlaceholder,
            recipeShortNoteHint: undefined,
            recipeInstructionsLabel: strings.mealDetailsInstructionsLabel,
            recipeInstructionsPlaceholder: strings.mealDetailsInstructionsPlaceholder,
            recipeInstructionsHint: undefined,
            importInstructionsHint: undefined,
            ingredientsLabel: isMealVariationFromSavedRecipe ? 'Variation ingredients' : strings.ingredientsLabel,
            ingredientsRecipeHint: isViewingAttachedSavedRecipe
              ? strings.savedRecipeIngredientsHint
              : strings.mealDetailsIngredientsHint,
            importedIngredientsHint: undefined,
            importReviewLabel: undefined,
            importReviewHint: undefined,
            importReviewSummary: undefined,
            importReviewSourceSummaryTitle: undefined,
            importReviewSourceSummaryHint: undefined,
            importReviewSourceEmpty: undefined,
            importReviewSourceUrlEmpty: undefined,
            ingredientsEmptyState: strings.ingredientsEmptyState,
            loadingIngredients: strings.loadingIngredients,
            saveAsNewRecipeHint: strings.saveAsNewRecipeHint,
            saveRecipe: editor.isEditingSavedRecipeDirectly ? strings.saveRecipe : strings.saveMealDetails,
            savingRecipe: editor.isEditingSavedRecipeDirectly ? strings.savingRecipe : strings.savingMealDetails,
            saveToRecipes: strings.saveToRecipes,
            savingToRecipes: strings.savingToRecipes,
            ingredientNamePlaceholder: strings.ingredientNamePlaceholder,
            quantityPlaceholder: strings.quantityPlaceholder,
            addIngredient: strings.addIngredient,
            removeIngredient: strings.removeIngredient,
            importedIngredientHint: undefined,
            importedIngredientNeedsReviewHint: undefined,
            collapseIngredient: undefined,
            close: isViewingAttachedSavedRecipe || editor.isEditingSavedRecipeDirectly
              ? strings.close
              : 'Back to meal',
          }}
        />
      ) : null}

      {recipesWorkspace.addRecipe.isOpen ? (
        <MealRecipeCaptureSourceSheet
          options={addRecipeOptions}
          onClose={recipesWorkspace.addRecipe.closeAddRecipe}
          strings={{
            title: strings.addRecipeChooserTitle,
            subtitle: strings.addRecipeChooserSubtitle,
            close: strings.close,
          }}
        />
      ) : null}

      {recipesWorkspace.createRecipe.isOpen ? (
        <MealRecipeCaptureSourceSheet
          options={createRecipeOptions}
          onClose={recipesWorkspace.createRecipe.closeCreateRecipeOptions}
          strings={{
            title: strings.createRecipeChooserTitle,
            subtitle: strings.createRecipeChooserSubtitle,
            close: 'Back',
          }}
        />
      ) : null}

      {recipesWorkspace.importDraft.isOpen ? (
        <MealRecipeImportSheet
          importUrl={recipesWorkspace.importDraft.importUrl}
          onChangeImportUrl={recipesWorkspace.importDraft.setImportUrl}
          onImport={recipesWorkspace.importDraft.importRecipeDraft}
          onClose={recipesWorkspace.importDraft.closeImportRecipe}
          isImporting={recipesWorkspace.importDraft.isImportingDraft}
          error={recipesWorkspace.importDraft.error}
          clipboardImportUrl={recipesWorkspace.importDraft.clipboardImportUrl}
          strings={{
            title: strings.importRecipeTitle,
            subtitle: strings.importRecipeSubtitle,
            urlPlaceholder: strings.importRecipeUrlPlaceholder,
            helpText: strings.importRecipeHelpText,
            clipboardHint: strings.importRecipeClipboardHint,
            importAction: strings.importRecipeAction,
            importingAction: strings.importingRecipeAction,
            close: 'Back',
          }}
        />
      ) : null}

      {recipesWorkspace.textImportDraft.isOpen ? (
        <MealRecipeTextImportSheet
          importText={recipesWorkspace.textImportDraft.importText}
          onChangeImportText={recipesWorkspace.textImportDraft.setImportText}
          onImport={recipesWorkspace.textImportDraft.importRecipeTextDraft}
          onClose={recipesWorkspace.textImportDraft.closeImportRecipeText}
          isImporting={recipesWorkspace.textImportDraft.isImportingDraft}
          error={recipesWorkspace.textImportDraft.error}
          strings={{
            title: strings.importRecipeTextTitle,
            subtitle: strings.importRecipeTextSubtitle,
            textPlaceholder: strings.importRecipeTextPlaceholder,
            helpText: strings.importRecipeTextHelpText,
            importAction: strings.importRecipeTextAction,
            importingAction: strings.importingRecipeTextAction,
            close: 'Back',
          }}
        />
      ) : null}

      <DuplicateRecipeWarningSheet
        visible={!!pendingRecipesDuplicateCandidate}
        body={
          pendingRecipesDuplicateCandidate?.matchType === 'source-url'
            ? strings.duplicateRecipeFromUrlMessage
            : strings.duplicateRecipeFromNameMessage
        }
        recipeName={pendingRecipesDuplicateCandidate?.name ?? ''}
        archivedHint={pendingRecipesDuplicateCandidate?.archivedAt ? strings.duplicateRecipeArchivedSuffix : undefined}
        onPrimaryAction={() => {
          void recipesWorkspace.recipeDetail.openDuplicateRecipe();
        }}
        onSecondaryAction={() => {
          void recipesWorkspace.recipeDetail.saveDuplicateRecipeAnyway();
        }}
        onClose={recipesWorkspace.recipeDetail.dismissDuplicateRecipeWarning}
        strings={{
          title: strings.duplicateRecipeTitle,
          primaryAction: strings.duplicateRecipeOpenExistingAction,
          secondaryAction: strings.duplicateRecipeSaveCopyAction,
          cancelAction: strings.duplicateRecipeCancelAction,
        }}
      />

      <DuplicateRecipeWarningSheet
        visible={!!pendingMealDuplicateCandidate}
        body={
          pendingMealDuplicateCandidate?.matchType === 'source-url'
            ? strings.duplicateRecipeFromUrlMessage
            : strings.duplicateRecipeFromNameMessage
        }
        recipeName={pendingMealDuplicateCandidate?.name ?? ''}
        archivedHint={pendingMealDuplicateCandidate?.archivedAt ? strings.duplicateRecipeArchivedSuffix : undefined}
        onPrimaryAction={() => {
          if (pendingMealDuplicateCandidate) {
            actions.useDuplicateRecipe(pendingMealDuplicateCandidate.recipeId);
          }
        }}
        onSecondaryAction={() => {
          void actions.saveDuplicateRecipeAnyway();
        }}
        onClose={actions.dismissDuplicateRecipeWarning}
        strings={{
          title: strings.duplicateRecipeTitle,
          primaryAction: strings.duplicateRecipeUseExistingAction,
          secondaryAction: strings.duplicateRecipeSaveCopyAction,
          cancelAction: strings.duplicateRecipeCancelAction,
        }}
      />

      {recipesWorkspace.recipeDetail.isOpen ? (
        <MealRecipeDetailSheet
          recipeTitle={recipesWorkspace.recipeDetail.recipeTitle}
          onChangeRecipeTitle={recipesWorkspace.recipeDetail.setRecipeTitle}
          recipeSource={recipesWorkspace.recipeDetail.recipeSource}
          onChangeRecipeSource={recipesWorkspace.recipeDetail.setRecipeSource}
          recipeSourceUrl={recipesWorkspace.recipeDetail.recipeSourceUrl}
          onChangeRecipeSourceUrl={recipesWorkspace.recipeDetail.setRecipeSourceUrl}
          recipeServings={recipesWorkspace.recipeDetail.recipeServings}
          onChangeRecipeServings={recipesWorkspace.recipeDetail.setRecipeServings}
          recipeShortNote={recipesWorkspace.recipeDetail.recipeShortNote}
          onChangeRecipeShortNote={recipesWorkspace.recipeDetail.setRecipeShortNote}
          recipeInstructions={recipesWorkspace.recipeDetail.recipeInstructions}
          onChangeRecipeInstructions={recipesWorkspace.recipeDetail.setRecipeInstructions}
          ingredientRows={recipesWorkspace.recipeDetail.ingredientRows}
          isRecipeLoading={recipesWorkspace.recipeDetail.isRecipeLoading}
          hasExistingRecipe={recipesWorkspace.recipeDetail.hasExistingRecipe}
          isImportDraft={recipesWorkspace.recipeDetail.isImportDraft}
          isArchivedRecipe={recipesWorkspace.recipeDetail.isArchivedRecipe}
          hasIngredients={recipesWorkspace.recipeDetail.hasIngredients}
          showSaveAsNewRecipeHint={false}
          canEnterSavedRecipeEditMode={false}
          isEditingSavedRecipeDirectly={false}
          isActionPending={recipesWorkspace.recipeDetail.isActionPending}
          onAddIngredientRow={recipesWorkspace.recipeDetail.addIngredientRow}
          onRemoveIngredientRow={recipesWorkspace.recipeDetail.removeIngredientRow}
          onChangeIngredientName={recipesWorkspace.recipeDetail.setIngredientName}
          onChangeIngredientQuantity={recipesWorkspace.recipeDetail.setIngredientQuantity}
          onToggleIngredientUnit={recipesWorkspace.recipeDetail.setIngredientUnit}
          onStartEditingSavedRecipeDirectly={() => {}}
          onEnterEditMode={recipesWorkspace.recipeDetail.startEditingRecipe}
          onPlanRecipe={recipesWorkspace.recipeDetail.isReadMode && !recipesWorkspace.recipeDetail.isArchivedRecipe
            ? openRecipePlanBridge
            : undefined}
          onToggleMakeSoon={recipesWorkspace.recipeDetail.toggleRecipeMakeSoon}
          onSave={recipesWorkspace.recipeDetail.saveRecipe}
          onClose={recipesWorkspace.recipeDetail.closeRecipeDetail}
          isSaving={recipesWorkspace.recipeDetail.isSavingRecipe}
          isTogglingMakeSoon={recipesWorkspace.recipeDetail.isTogglingMakeSoon}
          isMarkedMakeSoon={!!recipesWorkspace.recipeDetail.recipeMakeSoonAt}
          onArchive={recipesWorkspace.recipeDetail.canArchiveRecipe
            ? recipesWorkspace.recipeDetail.archiveCurrentRecipe
            : undefined}
          onRestore={recipesWorkspace.recipeDetail.canRestoreRecipe
            ? recipesWorkspace.recipeDetail.restoreCurrentRecipe
            : undefined}
          onDelete={recipesWorkspace.recipeDetail.showDeleteRecipeAction
            ? confirmDeleteArchivedRecipe
            : undefined}
          isArchiving={recipesWorkspace.recipeDetail.isArchivingRecipe}
          isDeleting={recipesWorkspace.recipeDetail.isDeletingRecipe}
          canDelete={recipesWorkspace.recipeDetail.canDeleteRecipe}
          deleteBlockedHint={recipesWorkspace.recipeDetail.showDeleteRecipeAction
            && !recipesWorkspace.recipeDetail.canDeleteRecipe
            ? recipesWorkspace.recipeDetail.recipeDeleteBlockedReason ?? strings.deleteRecipeBlockedDefault
            : null}
          error={recipesWorkspace.recipeDetail.error}
          showTitleField={!recipesWorkspace.recipeDetail.isReadMode && !recipesWorkspace.recipeDetail.isArchivedRecipe}
          isReadOnlyMode={recipesWorkspace.recipeDetail.isReadMode}
          canEnterEditMode={recipesWorkspace.recipeDetail.canEnterEditMode}
          useContentFirstEditor
          showHeaderIdentityBadge={false}
          suppressInitialSeedIngredientAutofocus={!recipesWorkspace.recipeDetail.hasExistingRecipe && !recipesWorkspace.recipeDetail.isImportDraft}
          strings={{
            eyebrow: recipesWorkspace.recipeDetail.isImportDraft
              ? 'Add recipe'
              : recipesWorkspace.recipeDetail.isArchivedRecipe
                ? 'Archived recipe'
                : recipesWorkspace.recipeDetail.hasExistingRecipe
                  ? (recipesWorkspace.recipeDetail.isReadMode ? 'Saved recipe' : 'Edit recipe')
                  : 'Add recipe',
            title: strings.recipeSheetTitle,
            subtitle: recipesWorkspace.recipeDetail.isImportDraft
              ? strings.importDraftSubtitle
              : !recipesWorkspace.recipeDetail.hasExistingRecipe
                ? strings.recipeDestinationSubtitle
                : undefined,
            newRecipeLabel: strings.recipeSheetNewRecipeTitle,
            usingRecipeLabel: strings.savedRecipeLabel,
            mealSpecificRecipeLabel: strings.mealSpecificRecipeLabel,
            editingSavedRecipeLabel: strings.editingSavedRecipeLabel,
            importDraftLabel: strings.importDraftLabel,
            archivedRecipeLabel: strings.archivedRecipeLabel,
            newRecipeTitle: strings.recipeSheetNewRecipeTitle,
            recipeContextHint: recipesWorkspace.recipeDetail.isImportDraft
              ? strings.importDraftContextHint
              : !recipesWorkspace.recipeDetail.hasExistingRecipe
                ? strings.newSavedRecipeContextHint
              : undefined,
            archivedReadOnlyHint: strings.archivedReadOnlyHint,
            recipeNameLabel: strings.recipeNameLabel,
            recipeNamePlaceholder: strings.recipeNamePlaceholder,
            recipeNameEditHint: recipesWorkspace.recipeDetail.hasExistingRecipe
              || recipesWorkspace.recipeDetail.isImportDraft
              ? undefined
              : strings.recipeNameEditHint,
            editRecipeAction: strings.editRecipeAction,
            planRecipeAction: recipesWorkspace.recipeDetail.isReadMode && !recipesWorkspace.recipeDetail.isArchivedRecipe
              ? strings.planRecipeAction
              : undefined,
            markMakeSoonAction: strings.markMakeSoonAction,
            clearMakeSoonAction: strings.clearMakeSoonAction,
            recipeContentLabel: recipesWorkspace.recipeDetail.isImportDraft
              ? strings.importReviewSourceSummaryTitle
              : strings.recipeContentLabel,
            recipeMetadataHint: recipesWorkspace.recipeDetail.isImportDraft
              ? strings.importReviewSourceSummaryHint
              : strings.recipeMetadataHint,
            recipeServingsLabel: strings.recipeServingsLabel,
            recipeServingsPlaceholder: strings.recipeServingsPlaceholder,
            recipePortionValue: strings.recipePortionValue,
            recipeSourceLabel: recipesWorkspace.recipeDetail.isImportDraft
              ? 'Source'
              : strings.recipeSourceLabel,
            recipeSourcePlaceholder: recipesWorkspace.recipeDetail.isImportDraft
              ? 'Recipe site'
              : strings.recipeSourcePlaceholder,
            recipeSourceUrlLabel: recipesWorkspace.recipeDetail.isImportDraft
              ? 'Website'
              : strings.recipeSourceUrlLabel,
            recipeSourceUrlPlaceholder: recipesWorkspace.recipeDetail.isImportDraft
              ? 'Website link'
              : strings.recipeSourceUrlPlaceholder,
            recipeShortNoteLabel: strings.recipeShortNoteLabel,
            recipeShortNotePlaceholder: strings.recipeShortNotePlaceholder,
            recipeShortNoteHint: strings.recipeShortNoteHint,
            recipeInstructionsLabel: strings.recipeInstructionsLabel,
            recipeInstructionsPlaceholder: strings.recipeInstructionsPlaceholder,
            recipeInstructionsHint: strings.recipeInstructionsHint,
            importInstructionsHint: recipesWorkspace.recipeDetail.isImportDraft
              ? strings.importInstructionsHint
              : undefined,
            ingredientsLabel: strings.ingredientsLabel,
            ingredientsRecipeHint: recipesWorkspace.recipeDetail.isImportDraft
              ? strings.importedIngredientsHint
              : undefined,
            importedIngredientsHint: undefined,
            importReviewLabel: undefined,
            importReviewHint: undefined,
            importReviewSummary: recipesWorkspace.recipeDetail.isImportDraft
              ? (count: number) => count === 0
                ? 'Ready to save'
                : count === 1
                  ? '1 ingredient to review'
                  : `${count} ingredients to review`
              : undefined,
            importReviewSourceSummaryTitle: recipesWorkspace.recipeDetail.isImportDraft
              ? strings.importReviewSourceSummaryTitle
              : undefined,
            importReviewSourceSummaryHint: recipesWorkspace.recipeDetail.isImportDraft
              ? strings.importReviewSourceSummaryHint
              : undefined,
            importReviewSourceEmpty: recipesWorkspace.recipeDetail.isImportDraft
              ? strings.importReviewSourceEmpty
              : undefined,
            importReviewSourceUrlEmpty: recipesWorkspace.recipeDetail.isImportDraft
              ? strings.importReviewSourceUrlEmpty
              : undefined,
            ingredientsEmptyState: strings.ingredientsEmptyState,
            loadingIngredients: strings.loadingIngredients,
            ingredientNamePlaceholder: strings.ingredientNamePlaceholder,
            quantityPlaceholder: strings.quantityPlaceholder,
            addIngredient: strings.addIngredient,
            removeIngredient: strings.removeIngredient,
            importedIngredientHint: recipesWorkspace.recipeDetail.isImportDraft
              ? strings.importedIngredientHint
              : undefined,
            importedIngredientNeedsReviewHint: recipesWorkspace.recipeDetail.isImportDraft
              ? strings.importedIngredientNeedsReviewHint
              : undefined,
            importedIngredientReviewTag: recipesWorkspace.recipeDetail.isImportDraft
              ? strings.importedIngredientReviewTag
              : undefined,
            importedIngredientMarkDone: recipesWorkspace.recipeDetail.isImportDraft
              ? strings.importedIngredientMarkDone
              : undefined,
            importedIngredientReviewed: recipesWorkspace.recipeDetail.isImportDraft
              ? strings.importedIngredientReviewed
              : undefined,
            collapseIngredient: recipesWorkspace.recipeDetail.isImportDraft
              ? strings.collapseIngredient
              : undefined,
            saveRecipe: recipesWorkspace.recipeDetail.hasExistingRecipe
              ? strings.saveRecipe
              : recipesWorkspace.recipeDetail.isImportDraft
                ? strings.saveRecipeToLibrary
                : strings.saveRecipeToLibrary,
            savingRecipe: recipesWorkspace.recipeDetail.hasExistingRecipe
              ? strings.savingRecipe
              : recipesWorkspace.recipeDetail.isImportDraft
                ? strings.savingRecipe
                : strings.creatingRecipe,
            archiveRecipe: recipesWorkspace.recipeDetail.canArchiveRecipe
              ? strings.archiveRecipe
              : undefined,
            archivingRecipe: strings.archivingRecipe,
            restoreRecipe: recipesWorkspace.recipeDetail.canRestoreRecipe
              ? strings.restoreRecipe
              : undefined,
            restoringRecipe: strings.restoringRecipe,
            restoreRecipeHint: undefined,
            deleteRecipe: recipesWorkspace.recipeDetail.showDeleteRecipeAction
              ? strings.deleteRecipe
              : undefined,
            deletingRecipe: strings.deletingRecipe,
            deleteRecipeHint: undefined,
            close: strings.close,
          }}
        />
      ) : null}

      {recipePlanBridge ? (
        <MealRecipePlanSheet
          recipeTitle={recipePlanBridge.recipeTitle}
          weekSummary={formatWeekRangeLabel(weekStart, weekEnd)}
          memorySummary={recipeMemorySupport ?? undefined}
          days={currentWeekDayOptions}
          mealTypeLabels={MEAL_TYPE_LABELS}
          existingMeals={currentWeekMealEntries}
          defaultDayOfWeek={defaultRecipePlanDayOfWeek}
          defaultMealType="DINNER"
          isSubmitting={isPlanningRecipe}
          error={recipePlanError}
          onConfirm={confirmRecipePlanBridge}
          onClose={closeRecipePlanBridge}
          strings={{
            title: strings.planRecipeSheetTitle,
            subtitle: strings.planRecipeSheetSubtitle,
            recipeLabel: strings.planRecipeRecipeLabel,
            weekLabel: strings.planRecipeWeekLabel,
            dayLabel: strings.planRecipeDayLabel,
            mealLabel: strings.planRecipeMealLabel,
            slotOccupiedHint: strings.planRecipeReplaceHint,
            planAction: strings.confirmPlanRecipe,
            replaceAction: strings.replacePlannedMeal,
            planningAction: strings.planningRecipe,
            close: strings.close,
          }}
        />
      ) : null}

      {editor.isOpen && editor.isRecipePickerOpen ? (
        <MealRecipePickerSheet
          recipes={editor.recipePickerOptions}
          selectedRecipeId={editor.selectedMealRecipeId}
          isLoading={editor.isRecipeListLoading}
          error={editor.recipeListError}
          onSelectRecipe={editor.selectExistingRecipe}
          onClose={editor.closeRecipePicker}
          strings={{
            title: editor.isSelectedRecipeSavedInRecipes ? strings.changeRecipe : strings.recipePickerTitle,
            hint: undefined,
            loadingRecipes: strings.loadingRecipes,
            noRecipes: strings.noRecipes,
            close: strings.close,
          }}
        />
      ) : null}

      {editor.isOpen && editor.isRecentMealsOpen ? (
        <MealRecentMealsSheet
          sections={(editor.mealChoiceSections ?? []).map((section) => ({
            ...section,
            title: section.id === 'recent'
              ? strings.recentMealChoicesRecent
              : section.id === 'familiar'
                ? strings.recentMealChoicesFamiliar
                : strings.recentMealChoicesMakeSoon,
          }))}
          isLoading={editor.isMealChoicesLoading}
          error={editor.mealChoicesError}
          onSelectMeal={(mealId) => {
            void editor.selectRecentMeal(mealId);
          }}
          onClose={editor.closeRecentMeals}
          strings={{
            title: strings.recentMealsTitle,
            hint: strings.recentMealsHint,
            loadingMeals: strings.loadingRecentMeals,
            noMeals: strings.noRecentMealsToReuse,
            close: strings.close,
          }}
        />
      ) : null}

      {editor.isShoppingReviewOpen ? (
        <MealShoppingReviewSheet
          ingredients={editor.shoppingReviewAddableIngredients}
          representedIngredients={editor.shoppingReviewRepresentedIngredients}
          selectedIngredientRowIds={editor.selectedShoppingIngredientRowIds}
          lists={lists}
          effectiveListId={effectiveListId}
          hasProjection={editor.hasShoppingReviewProjection}
          isLoadingProjection={editor.isShoppingProjectionLoading}
          projectionError={editor.shoppingProjectionError}
          showCreateListForm={editor.isShoppingListCreateOpen}
          newListName={editor.newShoppingListName}
          isCreatingList={editor.isCreatingShoppingList}
          onSelectListId={editor.setSelectedListId}
          onToggleIngredient={editor.toggleShoppingReviewIngredient}
          onOpenCreateList={editor.openShoppingListCreate}
          onCloseCreateList={editor.closeShoppingListCreate}
          onChangeNewListName={editor.setNewShoppingListName}
          onCreateList={() => {
            void editor.createShoppingListFromReview();
          }}
          shoppingSyncError={editor.shoppingSyncError}
          isSubmitting={editor.isAddingIngredientsToShopping}
          onConfirm={handleAddIngredientsToShopping}
          onClose={editor.closeShoppingReview}
          strings={{
            title: strings.shoppingReviewTitle,
            subtitle: (editor.selectedMeal?.mealTitle ?? editor.mealTitle.trim()) || undefined,
            emptyIngredientsHint: 'No ingredients saved for this meal yet.',
            selectedListLabel: strings.shoppingListLabel,
            newListLabel: strings.shoppingReviewNewListLabel,
            newListPlaceholder: strings.shoppingReviewNewListPlaceholder,
            createListAction: strings.shoppingReviewCreateListAction,
            creatingListAction: strings.shoppingReviewCreatingListAction,
            alreadyOnListLabel: strings.ingredientsAlreadyOnListLabel,
            addNowLabel: strings.ingredientsToAddLabel,
            onAnotherListLabel: 'Already on another list',
            everythingAlreadyOnListHint: strings.everythingAlreadyOnListHint,
            checkingListHint: strings.checkingShoppingListHint,
            checkingListFailedHint: 'Couldn\'t check this list right now.',
            noShoppingLists: strings.noShoppingLists,
            shoppingSyncFailed: strings.shoppingSyncFailed,
            confirm: strings.confirmAddIngredientsToShopping,
            confirming: strings.addingIngredientsToShopping,
            close: strings.close,
          }}
        />
      ) : null}

      {isWeekShoppingReviewOpen ? (
        <WeekShoppingReviewSheet
          representedLines={weekShoppingDisplay.representedLines}
          addableLines={weekShoppingDisplay.addableLines}
          selectedLineIds={weekShoppingSelectedLineIds}
          lists={lists.map((list) => ({ id: list.id, name: list.name }))}
          effectiveListId={activeWeekShoppingListId}
          isLoadingLists={shopping.loading && lists.length === 0}
          hasReview={weekShoppingReview !== null}
          isLoadingReview={isWeekShoppingReviewLoading}
          reviewError={weekShoppingReviewError}
          showCreateListForm={isWeekShoppingListCreateOpen || lists.length === 0}
          newListName={weekShoppingNewListName}
          isCreatingList={shopping.pendingMutation?.kind === 'create-list'}
          isSubmitting={isAddingWeekShoppingLines}
          onSelectListId={(listId) => {
            setIsWeekShoppingReviewLoading(true);
            setWeekShoppingReviewError(null);
            setWeekShoppingReview(null);
            setWeekShoppingSelectedListId(listId);
            setWeekShoppingSelectedLineIds([]);
          }}
          onToggleLine={toggleWeekShoppingLine}
          onOpenCreateList={openWeekShoppingListCreate}
          onCloseCreateList={closeWeekShoppingListCreate}
          onChangeNewListName={setWeekShoppingNewListName}
          onCreateList={() => {
            void createWeekShoppingList();
          }}
          onConfirm={() => {
            void confirmWeekShoppingAdd();
          }}
          onClose={closeWeekShoppingReview}
          strings={{
            title: strings.weekShoppingReviewTitle,
            subtitle: `${periodSummary.primary}${periodSummary.secondary ? ` · ${periodSummary.secondary}` : ''}`,
            selectedListLabel: strings.shoppingListLabel,
            loadingListsHint: strings.loadingShoppingLists,
            noShoppingLists: strings.noShoppingLists,
            newListLabel: strings.shoppingReviewNewListLabel,
            newListPlaceholder: strings.shoppingReviewNewListPlaceholder,
            createListAction: strings.shoppingReviewCreateListAction,
            creatingListAction: strings.shoppingReviewCreatingListAction,
            alreadyOnListLabel: strings.ingredientsAlreadyOnListLabel,
            addToListLabel: strings.ingredientsToAddLabel,
            emptyWeekHint: strings.emptyWeekShoppingReview,
            everythingAlreadyOnListHint: strings.everythingAlreadyOnWeekListHint,
            loadingReviewHint: strings.loadingWeekShoppingReview,
            loadingReviewFailedHint: 'Couldn\'t load week shopping right now.',
            selectionHint: strings.weekShoppingSelectionHint,
            nothingToAddHint: strings.weekShoppingNothingToAddHint,
            confirm: strings.confirmAddIngredientsToShopping,
            confirming: strings.addingIngredientsToShopping,
            close: strings.close,
          }}
        />
      ) : null}

    </AppScreen>
  );
}

const styles = StyleSheet.create({
  screenContent: {
    flex: 1,
  },
  contentInner: {
    gap: theme.spacing.xs,
  },
  homeLayout: {
    gap: theme.spacing.lg,
  },
  homeDestinations: {
    gap: theme.spacing.md,
  },
  homeDestination: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: theme.spacing.md,
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.lg,
    backgroundColor: theme.colors.surface,
    paddingHorizontal: theme.spacing.md,
    paddingVertical: theme.spacing.md,
  },
  homeDestinationPressed: {
    opacity: 0.82,
  },
  homeDestinationIcon: {
    width: 40,
    height: 40,
    borderRadius: theme.radius.circle,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: iconBackground(theme.colors.feature.meals),
  },
  homeDestinationCopy: {
    flex: 1,
    minWidth: 0,
    gap: 4,
  },
  homeDestinationTitle: {
    ...textStyles.body,
    color: theme.colors.text,
    fontWeight: '700',
  },
  homeDestinationSubtitle: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
  },
  controlsCard: {
    gap: theme.spacing.xs,
    paddingTop: theme.spacing.xs,
    paddingBottom: theme.spacing.xs,
  },
  workspaceSwitchRow: {
    flexDirection: 'row',
    alignSelf: 'flex-start',
    gap: 4,
    padding: 3,
    borderRadius: theme.radius.pill,
    borderWidth: 1,
    borderColor: theme.colors.border,
    backgroundColor: theme.colors.surfaceSubtle,
  },
  workspaceSwitchTab: {
    borderRadius: theme.radius.pill,
    paddingHorizontal: theme.spacing.sm,
    paddingVertical: 5,
  },
  workspaceSwitchTabActive: {
    backgroundColor: theme.colors.surface,
  },
  workspaceSwitchTabPressed: {
    opacity: 0.78,
  },
  workspaceSwitchText: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
    fontWeight: '600',
  },
  workspaceSwitchTextActive: {
    color: theme.colors.textPrimary,
  },
  contentOffset: {
    flex: 1,
    paddingTop: theme.layout.topBarOffset + theme.spacing.xs,
  },
  mainLayout: {
    flex: 1,
  },
  mainScroll: {
    flex: 1,
  },
  mainScrollContent: {
    paddingBottom: theme.spacing.md,
  },
  planSurfaceRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'flex-start',
  },
  periodControlsRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: theme.spacing.xs,
    paddingTop: theme.spacing.xs,
    borderTopWidth: 1,
    borderTopColor: theme.colors.border,
  },
  weekShoppingActionRow: {
    gap: theme.spacing.xs,
    paddingTop: theme.spacing.sm,
    borderTopWidth: 1,
    borderTopColor: theme.colors.border,
  },
  weekShoppingPostState: {
    color: theme.colors.textSecondary,
  },
  periodNavButton: {
    width: 36,
    height: 36,
    borderRadius: theme.radius.circle,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
    borderColor: theme.colors.border,
    backgroundColor: theme.colors.surface,
  },
  periodNavButtonPressed: {
    opacity: 0.72,
  },
  periodSummary: {
    flex: 1,
    minWidth: 0,
    alignItems: 'center',
    gap: 1,
    paddingHorizontal: theme.spacing.xs,
  },
  periodSummaryPrimary: {
    ...textStyles.h2,
    textAlign: 'center',
  },
  periodSummarySecondary: {
    textAlign: 'center',
    fontSize: 12,
  },
  dayList: {
    gap: theme.spacing.sm,
  },
  weekPlannerCard: {
    paddingVertical: theme.spacing.xs,
  },
  weekPlannerRow: {
    paddingVertical: theme.spacing.sm,
  },
  weekPlannerRowBorder: {
    borderTopWidth: 1,
    borderTopColor: theme.colors.border,
  },
  weekPlannerRowPressed: {
    opacity: 0.76,
  },
  dayRowInner: {
    gap: theme.spacing.xs,
  },
  dayHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'baseline',
    gap: theme.spacing.sm,
  },
  dayLabel: {
    ...textStyles.body,
    flex: 1,
    fontWeight: '700',
  },
  dayLabelToday: {
    color: theme.colors.feature.meals,
  },
  daySummaryText: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
    fontSize: 12,
  },
  mealList: {
    gap: theme.spacing.xs,
    marginTop: 2,
  },
  mealRow: {
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    backgroundColor: theme.colors.surface,
    minHeight: 48,
    justifyContent: 'center',
  },
  mealRowPressed: {
    opacity: 0.76,
  },
  mealRowContent: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: theme.spacing.sm,
    paddingVertical: theme.spacing.xs,
    paddingHorizontal: theme.spacing.sm,
  },
  mealTypeBadge: {
    minWidth: 78,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: theme.spacing.xs,
    paddingVertical: 6,
    borderRadius: theme.radius.pill,
  },
  mealTypeText: {
    fontSize: 11,
    fontWeight: '700',
    color: theme.colors.text,
  },
  mealType_BREAKFAST: {
    backgroundColor: theme.colors.accentSoft,
  },
  mealType_LUNCH: {
    backgroundColor: theme.colors.primarySoft,
  },
  mealType_DINNER: {
    backgroundColor: '#efe7ff',
  },
  mealTitle: {
    ...textStyles.body,
    flex: 1,
    fontWeight: '500',
  },
  weeklyEmptyText: {
    ...textStyles.subtle,
    fontSize: 14,
    color: theme.colors.textSecondary,
  },
  calendarCard: {
    gap: theme.spacing.sm,
  },
  monthGridHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    gap: theme.spacing.xs,
  },
  monthGridWeekday: {
    ...textStyles.subtle,
    flex: 1,
    textAlign: 'center',
  },
  monthGrid: {
    gap: theme.spacing.xs,
  },
  monthGridRow: {
    flexDirection: 'row',
    gap: theme.spacing.xs,
  },
  monthGridCellBase: {
    flex: 1,
    aspectRatio: 1,
  },
  monthCell: {
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.sm,
    backgroundColor: theme.colors.surfaceAlt,
    padding: theme.spacing.xs,
    justifyContent: 'space-between',
  },
  monthCellPlaceholder: {
    opacity: 0.45,
  },
  monthCellGhostText: {
    ...textStyles.subtle,
    color: 'transparent',
  },
  monthCellGhostBadge: {
    alignSelf: 'flex-end',
    minWidth: 18,
    height: 18,
    paddingHorizontal: 4,
  },
  monthCellToday: {
    backgroundColor: iconBackground(theme.colors.feature.meals),
    borderColor: theme.colors.feature.meals,
  },
  monthCellText: {
    ...textStyles.subtle,
    color: theme.colors.text,
  },
  monthCellTextToday: {
    color: theme.colors.feature.meals,
    fontWeight: '700',
  },
  monthCountBadge: {
    alignSelf: 'flex-end',
    minWidth: 18,
    height: 18,
    borderRadius: theme.radius.pill,
    paddingHorizontal: 4,
    backgroundColor: theme.colors.feature.meals,
    alignItems: 'center',
    justifyContent: 'center',
  },
  monthCountBadgeText: {
    color: '#fff',
    fontSize: 11,
    fontWeight: '700',
  },
  error: {
    color: theme.colors.danger,
    fontFamily: theme.typography.body,
  },
});
