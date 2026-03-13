import { useMemo, useState } from 'react';
import { Ionicons } from '@expo/vector-icons';
import {
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { MealsDailyView } from '../components/MealsDailyView';
import { MealEditorSheet } from '../components/MealEditorSheet';
import { MealIngredientsSheet } from '../components/MealIngredientsSheet';
import { MealsMonthlyView } from '../components/MealsMonthlyView';
import { MealsWeeklyView } from '../components/MealsWeeklyView';
import {
  addDays,
  buildMonthGridCells,
  getIsoWeekParts,
  getWeekStartDate,
  isTodayDate,
  toDateKey,
} from '../utils/mealDates';
import { useMealsWorkflow } from '../hooks/useMealsWorkflow';
import { useAppBackHandler } from '../../../shared/hooks/useAppBackHandler';
import {
  AppButton,
  BackIconButton,
  AppCard,
  AppChip,
  AppScreen,
  Subtle,
  TopBar,
} from '../../../shared/ui/components';
import { iconBackground, textStyles, theme } from '../../../shared/ui/theme';

type Props = {
  token: string;
  onDone: () => void;
};

type ViewMode = 'daily' | 'weekly' | 'monthly';

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

export function MealsWeekScreen({ token, onDone }: Props) {
  const insets = useSafeAreaInsets();
  const strings = {
    title: 'Meals',
    subtitle: 'Plan your week and keep shopping in sync.',
    daily: 'Daily',
    weekly: 'Weekly',
    monthly: 'Monthly',
    prev: 'Prev',
    next: 'Next',
    weekLabel: 'Week',
    monthlyOverview: 'Monthly overview. Only current week is loaded in V0.',
    loadingPlan: 'Loading week plan...',
    noMeals: 'No meals planned yet.',
    planMealTitle: 'Plan a meal',
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
    addIngredientsToShopping: 'Add ingredients to shopping list',
    noShoppingLists: 'No shopping lists yet.',
    shoppingSyncFailed: 'Meal saved, but shopping sync failed:',
    saveMeal: 'Save meal',
    removeMeal: 'Remove meal',
    close: 'Close',
  };
  const [viewMode, setViewMode] = useState<ViewMode>('daily');
  const [anchorDate, setAnchorDate] = useState(() => new Date());
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

  const weekEnd = useMemo(() => {
    const end = new Date(weekStart.getTime());
    end.setUTCDate(weekStart.getUTCDate() + 6);
    return end;
  }, [weekStart]);

  const dailyDayIndex = useMemo(() => {
    const day = anchorDate.getDay() || 7;
    return day - 1;
  }, [anchorDate]);

  const dailyDayNumber = dailyDayIndex + 1;
  const dailyMeals = mealsByDay.get(dailyDayNumber) ?? [];
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

  const monthMealCountByDateKey = useMemo(() => {
    const counts: Record<string, number> = {};
    for (let index = 0; index < 7; index += 1) {
      const date = new Date(weekStart.getTime());
      date.setUTCDate(weekStart.getUTCDate() + index);
      const localDate = new Date(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate());
      if (
        localDate.getFullYear() !== anchorDate.getFullYear()
        || localDate.getMonth() !== anchorDate.getMonth()
      ) {
        continue;
      }
      const meals = mealsByDay.get(index + 1) ?? [];
      counts[toDateKey(localDate)] = meals.length;
    }
    return counts;
  }, [weekStart, mealsByDay, anchorDate]);

  function closeEditor() {
    actions.closeEditor();
  }

  useAppBackHandler({
    canGoBack: true,
    onGoBack: onDone,
    isOverlayOpen: editor.isOpen || editor.isIngredientEditorOpen,
    onCloseOverlay: editor.isIngredientEditorOpen ? editor.closeIngredientEditor : closeEditor,
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
      <ScrollView contentContainerStyle={styles.contentOffset}>
        <View style={styles.contentInner}>
          <AppCard style={styles.headerCard}>
            <View style={styles.viewSwitchRow}>
              <AppChip
                label={strings.daily}
                active={viewMode === 'daily'}
                onPress={() => setViewMode('daily')}
                accentKey="meals"
              />
              <AppChip
                label={strings.weekly}
                active={viewMode === 'weekly'}
                onPress={() => setViewMode('weekly')}
                accentKey="meals"
              />
              <AppChip
                label={strings.monthly}
                active={viewMode === 'monthly'}
                onPress={() => setViewMode('monthly')}
                accentKey="meals"
              />
            </View>
          </AppCard>

          {viewMode === 'weekly' ? (
            <AppCard style={styles.compactNavCard}>
              <View style={[styles.headerRow, styles.compactHeaderRow]}>
                <AppButton
                  title={strings.prev}
                  onPress={() => setAnchorDate(addDays(anchorDate, -7))}
                  variant="ghost"
                />
                <Text style={styles.dailyHeaderText}>
                  {strings.weekLabel} {isoWeek} · {year}
                </Text>
                <AppButton
                  title={strings.next}
                  onPress={() => setAnchorDate(addDays(anchorDate, 7))}
                  variant="ghost"
                />
              </View>
              <Subtle>
                {formatDayLabel(weekStart, 0)} — {formatDayLabel(weekEnd, 6)}
              </Subtle>
            </AppCard>
          ) : null}

          {viewMode === 'daily' ? (
            <AppCard style={styles.compactNavCard}>
              <View style={[styles.headerRow, styles.compactHeaderRow]}>
                <AppButton
                  title={strings.prev}
                  onPress={() => setAnchorDate(addDays(anchorDate, -1))}
                  variant="ghost"
                />
                <Text style={styles.dailyHeaderText}>
                  {formatRelativeDailyNavLabel(anchorDate)}
                </Text>
                <AppButton
                  title={strings.next}
                  onPress={() => setAnchorDate(addDays(anchorDate, 1))}
                  variant="ghost"
                />
              </View>
              <Subtle style={styles.navSubLabel}>
                {strings.weekLabel} {isoWeek} · {year}
              </Subtle>
            </AppCard>
          ) : null}

          {viewMode === 'monthly' ? (
            <AppCard style={styles.compactNavCard}>
              <View style={[styles.headerRow, styles.compactHeaderRow]}>
                <AppButton
                  title={strings.prev}
                  onPress={() => setAnchorDate(addDays(anchorDate, -30))}
                  variant="ghost"
                />
                <Text style={styles.dailyHeaderText}>{formatMonthLabel(anchorDate)}</Text>
                <AppButton
                  title={strings.next}
                  onPress={() => setAnchorDate(addDays(anchorDate, 30))}
                  variant="ghost"
                />
              </View>
            </AppCard>
          ) : null}

          {plan.loading ? <Subtle>{strings.loadingPlan}</Subtle> : null}
          {plan.error ? <Text style={styles.error}>{plan.error}</Text> : null}

          {viewMode === 'weekly' ? (
            <MealsWeeklyView
              weekStart={weekStart}
              mealsByDay={mealsByDay}
              onOpenEditor={actions.openEditor}
              formatDayLabel={formatDayLabel}
              DAY_LABELS={DAY_LABELS}
              MEAL_TYPE_LABELS={MEAL_TYPE_LABELS}
              styles={styles}
              emptyText={strings.noMeals}
            />
          ) : null}

          {viewMode === 'daily' ? (
            <MealsDailyView
              dailyDayNumber={dailyDayNumber}
              dailyMeals={dailyMeals}
              onOpenEditor={actions.openEditor}
              MEAL_TYPE_LABELS={MEAL_TYPE_LABELS}
              styles={styles}
              emptyText={strings.noMeals}
              title={strings.title}
            />
          ) : null}

          {viewMode === 'monthly' ? (
            <AppCard>
              <MealsMonthlyView
                monthGridCells={monthGridCells}
                monthMealCountByDateKey={monthMealCountByDateKey}
                toDateKey={toDateKey}
                isTodayDate={isTodayDate}
                styles={styles}
                onPressDay={(date) => {
                  setAnchorDate(new Date(date.getTime()));
                  setViewMode('weekly');
                }}
                weekdayLabels={DAY_LABELS}
              />
            </AppCard>
          ) : null}
        </View>
      </ScrollView>

      {viewMode === 'daily' ? (
        <Pressable
          style={[styles.fab, { bottom: theme.spacing.lg + insets.bottom }]}
          onPress={() => actions.openEditor(dailyDayNumber, 'DINNER')}
        >
          <Text style={styles.fabLabel}>+</Text>
        </Pressable>
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
          pushToShopping={editor.pushToShopping}
          onChangePushToShopping={editor.setPushToShopping}
          lists={lists}
          effectiveListId={effectiveListId}
          onSelectListId={editor.setSelectedListId}
          shoppingSyncError={editor.shoppingSyncError}
          hasExistingMeal={!!editor.selectedMeal?.recipeTitle}
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
            addIngredientsToShopping: strings.addIngredientsToShopping,
            noShoppingLists: strings.noShoppingLists,
            shoppingSyncFailed: strings.shoppingSyncFailed,
            saveMeal: strings.saveMeal,
            removeMeal: strings.removeMeal,
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
    gap: theme.spacing.md,
  },
  headerCard: {
    gap: theme.spacing.xs,
  },
  navCard: {
    gap: theme.spacing.sm,
  },
  compactNavCard: {
    gap: theme.spacing.xs,
    paddingTop: theme.spacing.md,
    paddingBottom: theme.spacing.md,
  },
  contentOffset: {
    paddingTop: theme.layout.topBarOffset + theme.spacing.md,
    paddingBottom: theme.spacing.md,
    gap: theme.spacing.md,
  },
  viewSwitchRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: theme.spacing.sm,
  },
  headerRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  compactHeaderRow: {
    gap: theme.spacing.xs,
  },
  headerText: {
    ...textStyles.h2,
  },
  dailyHeaderText: {
    ...textStyles.h2,
    textTransform: 'capitalize',
  },
  navSubLabel: {
    ...textStyles.subtle,
  },
  dayList: {
    gap: theme.spacing.md,
  },
  dayCard: {
    gap: theme.spacing.xs,
  },
  dayHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  dayLabel: {
    ...textStyles.body,
  },
  mealList: {
    gap: theme.spacing.xs,
    marginTop: theme.spacing.xs,
  },
  dailyMealList: {
    marginTop: theme.spacing.sm,
  },
  mealRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: theme.spacing.sm,
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    paddingVertical: theme.spacing.xs,
    paddingHorizontal: theme.spacing.sm,
    backgroundColor: theme.colors.surfaceAlt,
  },
  mealTypeBadge: {
    paddingHorizontal: theme.spacing.xs,
    paddingVertical: theme.spacing.xs,
    borderRadius: theme.radius.pill,
  },
  mealTypeText: {
    fontSize: 12,
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
  },
  weekRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: theme.spacing.sm,
    borderBottomWidth: 1,
    borderBottomColor: theme.colors.border,
  },
  weekTitle: {
    ...textStyles.body,
  },
  weekCount: {
    ...textStyles.subtle,
  },
  weeklyEmptyText: {
    ...textStyles.subtle,
    opacity: 0.8,
    fontSize: 13,
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
  fab: {
    position: 'absolute',
    bottom: theme.spacing.lg,
    right: theme.spacing.lg,
    width: 56,
    height: 56,
    borderRadius: theme.radius.circle,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: theme.colors.feature.meals,
    ...theme.elevation.floating,
  },
  fabLabel: {
    fontSize: 28,
    color: theme.colors.surface,
    lineHeight: 32,
  },
});

