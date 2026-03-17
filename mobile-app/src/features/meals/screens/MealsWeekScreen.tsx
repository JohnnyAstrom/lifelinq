import { useEffect, useMemo, useState } from 'react';
import { Ionicons } from '@expo/vector-icons';
import {
  Pressable,
  RefreshControl,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { MealDayDetailSheet } from '../components/MealDayDetailSheet';
import { MealEditorSheet } from '../components/MealEditorSheet';
import { MealIngredientsSheet } from '../components/MealIngredientsSheet';
import { MealShoppingReviewSheet } from '../components/MealShoppingReviewSheet';
import { MealsMonthlyView } from '../components/MealsMonthlyView';
import { MealsWeeklyView } from '../components/MealsWeeklyView';
import { getWeekPlan, type WeekPlanResponse } from '../api/mealsApi';
import {
  addDays,
  buildMonthGridCells,
  getIsoWeekParts,
  getWeekStartDate,
  isTodayDate,
  toDateKey,
} from '../utils/mealDates';
import { useMealsWorkflow } from '../hooks/useMealsWorkflow';
import { useMonthMealOverview } from '../hooks/useMonthMealOverview';
import { formatApiError } from '../../../shared/api/client';
import { useAuth } from '../../../shared/auth/AuthContext';
import { useAppBackHandler } from '../../../shared/hooks/useAppBackHandler';
import {
  AppButton,
  AppCard,
  AppScreen,
  BackIconButton,
  Subtle,
  TopBar,
} from '../../../shared/ui/components';
import { iconBackground, textStyles, theme } from '../../../shared/ui/theme';

type Props = {
  token: string;
  onDone: () => void;
};

type SurfaceMode = 'week' | 'calendar';
type MealType = 'BREAKFAST' | 'LUNCH' | 'DINNER';

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

export function MealsWeekScreen({ token, onDone }: Props) {
  const { handleApiError } = useAuth();
  const strings = {
    title: 'Meals',
    subtitle: 'Plan your week and add ingredients to shopping when needed.',
    weeklyPlanner: 'Week',
    calendarOverview: 'Calendar',
    openCalendar: 'Calendar',
    backToWeek: 'Back to week',
    weekLabel: 'Week',
    monthlyOverview: 'See your month at a glance and open any day.',
    loadingPlan: 'Loading week plan...',
    loadingMonthOverview: 'Loading calendar...',
    noMeals: 'No meals planned yet.',
    noMealsThisDay: 'No meals planned for this day.',
    loadingDay: 'Loading day...',
    todayLabel: 'Today',
    planMealTitle: 'Plan a meal',
    planMealSlot: 'Plan',
    mealsLabel: 'Meals',
    mealHint: 'Tap to edit this meal.',
    mealActionHint: 'Tap to plan this meal.',
    dayLabel: 'Day',
    mealTitlePlaceholder: 'Meal title',
    ingredientsLabel: 'Ingredients',
    ingredientsEmptyState: 'Optional. Add ingredients when you need them.',
    ingredientsSummarySuffix: 'ingredients',
    ingredientsSheetTitle: 'Ingredients',
    ingredientNamePlaceholder: 'Ingredient name',
    quantityPlaceholder: 'Amount',
    addIngredient: 'Add ingredient',
    addIngredients: 'Add ingredients',
    editIngredients: 'Edit ingredients',
    removeIngredient: 'Remove',
    loadingIngredients: 'Loading ingredients...',
    shoppingLabel: 'Shopping',
    addIngredientsToShoppingAction: 'Add ingredients to shopping',
    shoppingReviewTitle: 'Add ingredients to shopping',
    shoppingListLabel: 'Shopping list',
    ingredientsToAddLabel: 'Ingredients to add',
    confirmAddIngredientsToShopping: 'Add all ingredients',
    noShoppingLists: 'No shopping lists yet.',
    shoppingSyncFailed: 'Meal saved, but adding ingredients to shopping failed:',
    saveMeal: 'Save meal',
    savingMeal: 'Saving meal...',
    removeMeal: 'Remove meal',
    removingMeal: 'Removing meal...',
    close: 'Close',
    addingIngredientsToShopping: 'Adding ingredients...',
  };
  const [surfaceMode, setSurfaceMode] = useState<SurfaceMode>('week');
  const [anchorDate, setAnchorDate] = useState(() => new Date());
  const [selectedDayDetailDate, setSelectedDayDetailDate] = useState<Date | null>(null);
  const [pendingDayEditorOpen, setPendingDayEditorOpen] = useState<{
    date: Date;
    dayOfWeek: number;
    mealType: MealType;
  } | null>(null);
  const [selectedDayPlan, setSelectedDayPlan] = useState<WeekPlanResponse | null>(null);
  const [isSelectedDayPlanLoading, setIsSelectedDayPlanLoading] = useState(false);
  const [selectedDayPlanError, setSelectedDayPlanError] = useState<string | null>(null);
  const { year, isoWeek } = useMemo(
    () => getIsoWeekParts(anchorDate),
    [anchorDate]
  );
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
    enabled: surfaceMode === 'calendar',
    currentWeekPlan: plan.data,
  });
  const shopping = workflow.shopping;
  const mealsByDay = workflow.mealsByDay;
  const lists = shopping.lists;
  const effectiveListId = editor.effectiveListId;

  async function handleSave() {
    await actions.saveMeal();
  }

  async function handleRemove() {
    await actions.removeMeal();
  }

  async function handleAddIngredientsToShopping() {
    await actions.addIngredientsToShopping();
  }

  const weekEnd = useMemo(() => {
    const end = new Date(weekStart.getTime());
    end.setUTCDate(weekStart.getUTCDate() + 6);
    return end;
  }, [weekStart]);

  const isAnchorWeekLoaded = plan.data?.year === year && plan.data?.isoWeek === isoWeek;
  const showInitialPlanLoading = surfaceMode === 'week' && plan.isInitialLoading && !isAnchorWeekLoaded;
  const showInitialMonthOverviewLoading = surfaceMode === 'calendar'
    && monthOverview.isInitialLoading
    && !monthOverview.hasLoaded;
  const currentSurfaceError = surfaceMode === 'week' ? plan.error : monthOverview.error;
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

  const editorDayOptions = useMemo(() => {
    return DAY_LABELS.map((_, index) => {
      const date = new Date(weekStart.getTime());
      date.setUTCDate(weekStart.getUTCDate() + index);
      const localDate = new Date(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate());
      return {
        dayNumber: index + 1,
        date: localDate,
        label: formatDayLabel(date, index),
      };
    });
  }, [weekStart]);

  const monthGridCells = useMemo(() => buildMonthGridCells(anchorDate), [anchorDate]);

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

  const selectedDayMeals = useMemo(() => {
    if (!selectedDayDetail || !effectiveSelectedDayPlan) {
      return [];
    }
    return effectiveSelectedDayPlan.meals
      .filter((meal) => meal.dayOfWeek === selectedDayDetail.dayOfWeek)
      .map((meal) => ({
        dayOfWeek: meal.dayOfWeek,
        mealType: meal.mealType as MealType,
        recipeId: meal.recipeId,
        recipeTitle: meal.recipeTitle,
      }))
      .sort((a, b) => {
        const order = { BREAKFAST: 0, LUNCH: 1, DINNER: 2 } as const;
        return order[a.mealType] - order[b.mealType];
      });
  }, [effectiveSelectedDayPlan, selectedDayDetail]);
  const dayDetailError = selectedDayDetail
    ? (isSelectedDayInAnchorWeek ? plan.error : selectedDayPlanError)
    : null;
  const isDayDetailLoading = !!selectedDayDetail
    && !effectiveSelectedDayPlan
    && !dayDetailError
    && (isSelectedDayInAnchorWeek ? plan.isInitialLoading : isSelectedDayPlanLoading);

  useEffect(() => {
    if (!pendingDayEditorOpen) {
      return;
    }
    const { year: pendingYear, isoWeek: pendingIsoWeek } = getIsoWeekParts(pendingDayEditorOpen.date);
    if (plan.data?.year !== pendingYear || plan.data?.isoWeek !== pendingIsoWeek) {
      return;
    }
    actions.openEditor(pendingDayEditorOpen.dayOfWeek, pendingDayEditorOpen.mealType);
    setPendingDayEditorOpen(null);
  }, [actions, pendingDayEditorOpen, plan.data]);

  function openDayDetail(date: Date) {
    const nextDate = new Date(date.getTime());
    setSelectedDayDetailDate(nextDate);
  }

  function closeDayDetail() {
    if (editor.isActionPending) {
      return;
    }
    setSelectedDayDetailDate(null);
  }

  function openMealFromDayDetail(mealType: MealType) {
    if (!selectedDayDetail) {
      return;
    }
    setPendingDayEditorOpen({
      date: selectedDayDetail.date,
      dayOfWeek: selectedDayDetail.dayOfWeek,
      mealType,
    });
    setAnchorDate(selectedDayDetail.date);
    setSelectedDayDetailDate(null);
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

  useAppBackHandler({
    canGoBack: true,
    onGoBack: onDone,
    isOverlayOpen:
      !!selectedDayDetailDate
      || editor.isOpen
      || editor.isIngredientEditorOpen
      || editor.isShoppingReviewOpen,
    onCloseOverlay: editor.isIngredientEditorOpen
      ? editor.closeIngredientEditor
      : editor.isShoppingReviewOpen
        ? editor.closeShoppingReview
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
          title={strings.title}
          subtitle={strings.subtitle}
          icon={<Ionicons name="restaurant-outline" />}
          accentKey="meals"
          right={<BackIconButton onPress={onDone} />}
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
                refreshing={surfaceMode === 'week' ? plan.isRefreshing : monthOverview.isRefreshing}
                onRefresh={() => {
                  void (surfaceMode === 'week' ? plan.reload() : monthOverview.reload());
                }}
              />
            )}
          >
            <View style={styles.contentInner}>
              <AppCard style={styles.controlsCard}>
                <View style={styles.controlsHeaderRow}>
                  <View style={styles.controlsHeaderCopy}>
                    <Text style={styles.controlsEyebrow}>
                      {surfaceMode === 'week' ? strings.weeklyPlanner : strings.calendarOverview}
                    </Text>
                  </View>
                  <AppButton
                    title={surfaceMode === 'week' ? strings.openCalendar : strings.backToWeek}
                    onPress={() => setSurfaceMode(surfaceMode === 'week' ? 'calendar' : 'week')}
                    variant="ghost"
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
                    onOpenEditor={actions.openEditor}
                    formatDayLabel={formatDayLabel}
                    DAY_LABELS={DAY_LABELS}
                    MEAL_TYPE_LABELS={MEAL_TYPE_LABELS}
                    styles={styles}
                    emptyText={strings.noMeals}
                    todayLabel={strings.todayLabel}
                  />
                ) : (
                  <AppCard style={styles.calendarCard}>
                    <Subtle>{strings.monthlyOverview}</Subtle>
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
          isLoading={isDayDetailLoading}
          error={dayDetailError}
          onOpenMeal={openMealFromDayDetail}
          onClose={closeDayDetail}
          strings={{
            title: strings.title,
            close: strings.close,
            loadingDay: strings.loadingDay,
            emptyDay: strings.noMealsThisDay,
            addMeal: strings.planMealSlot,
            mealsLabel: strings.mealsLabel,
            mealHint: strings.mealHint,
            mealActionHint: strings.mealActionHint,
          }}
        />
      ) : null}

      {editor.isOpen ? (
        <MealEditorSheet
          initialDate={selectedEditorDate}
          dayOptions={editorDayOptions}
          onSelectDay={editor.setSelectedDay}
          onClose={closeEditor}
          onSave={handleSave}
          onRemove={handleRemove}
          selectedMealType={editor.selectedMealType}
          onSelectMealType={editor.setSelectedMealType}
          mealTypeLabels={MEAL_TYPE_LABELS}
          recipeTitle={editor.recipeTitle}
          onChangeRecipeTitle={editor.setRecipeTitle}
          ingredientRows={editor.ingredientRows}
          isRecipeLoading={editor.isRecipeLoading}
          onOpenIngredients={editor.openIngredientEditor}
          hasIngredients={editor.hasIngredients}
          onOpenShoppingReview={editor.openShoppingReview}
          hasExistingMeal={!!editor.selectedMeal?.recipeTitle}
          isSavingMeal={editor.isSavingMeal}
          isRemovingMeal={editor.isRemovingMeal}
          isActionPending={editor.isActionPending}
          strings={{
            planMealTitle: strings.planMealTitle,
            dayLabel: strings.dayLabel,
            mealTitlePlaceholder: strings.mealTitlePlaceholder,
            ingredientsLabel: strings.ingredientsLabel,
            addIngredients: strings.addIngredients,
            editIngredients: strings.editIngredients,
            ingredientsEmptyState: strings.ingredientsEmptyState,
            ingredientsSummarySuffix: strings.ingredientsSummarySuffix,
            loadingIngredients: strings.loadingIngredients,
            shoppingLabel: strings.shoppingLabel,
            addIngredientsToShoppingAction: strings.addIngredientsToShoppingAction,
            saveMeal: strings.saveMeal,
            savingMeal: strings.savingMeal,
            removeMeal: strings.removeMeal,
            removingMeal: strings.removingMeal,
            close: strings.close,
          }}
        />
      ) : null}

      {editor.isOpen && editor.isShoppingReviewOpen ? (
        <MealShoppingReviewSheet
          ingredientRows={editor.ingredientRows}
          lists={lists}
          effectiveListId={effectiveListId}
          onSelectListId={editor.setSelectedListId}
          shoppingSyncError={editor.shoppingSyncError}
          isSubmitting={editor.isAddingIngredientsToShopping}
          onConfirm={handleAddIngredientsToShopping}
          onClose={editor.closeShoppingReview}
          strings={{
            title: strings.shoppingReviewTitle,
            ingredientsLabel: strings.ingredientsToAddLabel,
            selectedListLabel: strings.shoppingListLabel,
            noShoppingLists: strings.noShoppingLists,
            shoppingSyncFailed: strings.shoppingSyncFailed,
            confirm: strings.confirmAddIngredientsToShopping,
            confirming: strings.addingIngredientsToShopping,
            close: strings.close,
          }}
        />
      ) : null}

      {editor.isOpen && editor.isIngredientEditorOpen ? (
        <MealIngredientsSheet
          ingredientRows={editor.ingredientRows}
          isRecipeLoading={editor.isRecipeLoading}
          onAddIngredientRow={editor.addIngredientRow}
          onRemoveIngredientRow={editor.removeIngredientRow}
          onChangeIngredientName={editor.setIngredientName}
          onChangeIngredientQuantity={editor.setIngredientQuantity}
          onToggleIngredientUnit={editor.setIngredientUnit}
          onClose={editor.closeIngredientEditor}
          strings={{
            title: strings.ingredientsSheetTitle,
            ingredientNamePlaceholder: strings.ingredientNamePlaceholder,
            quantityPlaceholder: strings.quantityPlaceholder,
            addIngredient: strings.addIngredient,
            removeIngredient: strings.removeIngredient,
            loadingIngredients: strings.loadingIngredients,
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
    gap: theme.spacing.sm,
  },
  controlsCard: {
    gap: theme.spacing.xs,
    paddingTop: theme.spacing.xs,
    paddingBottom: theme.spacing.xs,
  },
  contentOffset: {
    flex: 1,
    paddingTop: theme.layout.topBarOffset + theme.spacing.md,
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
  controlsHeaderRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: theme.spacing.sm,
  },
  controlsHeaderCopy: {
    flex: 1,
    minWidth: 0,
  },
  controlsEyebrow: {
    ...textStyles.subtle,
    textTransform: 'uppercase',
    letterSpacing: 0.8,
    color: theme.colors.feature.meals,
    fontWeight: '700',
  },
  periodControlsRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: theme.spacing.xs,
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
    paddingVertical: theme.spacing.sm,
  },
  weekPlannerRow: {
    paddingVertical: theme.spacing.md,
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
