import { useEffect, useMemo, useRef, useState } from 'react';
import {
  Keyboard,
  Modal,
  Pressable,
  StyleSheet,
  Switch,
  Text,
  View,
} from 'react-native';
import { KeyboardAwareScrollView } from 'react-native-keyboard-controller';
import { MealsDailyView } from '../components/MealsDailyView';
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
  AppCard,
  AppChip,
  AppInput,
  AppScreen,
  Subtle,
  TopBar,
} from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';

type Props = {
  token: string;
  onDone: () => void;
};

type ViewMode = 'daily' | 'weekly' | 'monthly';

const DAY_LABELS = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
const MEAL_TYPES = ['BREAKFAST', 'LUNCH', 'DINNER'] as const;
const MEAL_TYPE_LABELS: Record<(typeof MEAL_TYPES)[number], string> = {
  BREAKFAST: 'Breakfast',
  LUNCH: 'Lunch',
  DINNER: 'Dinner',
};
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
    addMeal: 'Add meal',
    noMeals: 'No meals planned yet.',
    weeksTitle: 'Weeks',
    mealsCount: 'meals',
    back: 'Back',
    planMealTitle: 'Plan a meal',
    selectedDayPrefix: 'Selected day:',
    mealTitlePlaceholder: 'Meal title',
    ingredientsPlaceholder: 'Ingredients (one per line)',
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

  const [isKeyboardVisible, setIsKeyboardVisible] = useState(false);
  const mealsByDay = workflow.mealsByDay;
  const lists = shopping.lists;
  const effectiveListId = editor.effectiveListId;

  useEffect(() => {
    const showSub = Keyboard.addListener('keyboardDidShow', () => setIsKeyboardVisible(true));
    const hideSub = Keyboard.addListener('keyboardDidHide', () => setIsKeyboardVisible(false));
    return () => {
      showSub.remove();
      hideSub.remove();
    };
  }, []);
  const wasEditorOpenRef = useRef(false);
  useEffect(() => {
    if (wasEditorOpenRef.current && !editor.isOpen) {
      Keyboard.dismiss();
    }
    wasEditorOpenRef.current = editor.isOpen;
  }, [editor.isOpen]);

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

  const currentWeekMealCount = useMemo(() => {
    let count = 0;
    for (const list of mealsByDay.values()) {
      count += list.length;
    }
    return count;
  }, [mealsByDay]);

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
    isOverlayOpen: editor.isOpen,
    onCloseOverlay: closeEditor,
  });

  return (
    <View style={styles.root}>
      <AppScreen>
        <TopBar
          title={strings.title}
          subtitle={strings.subtitle}
          left={<AppButton title={strings.back} onPress={onDone} variant="ghost" />}
        />

        <View style={styles.contentOffset}>
          <AppCard style={styles.headerCard}>
            <View style={styles.viewSwitchRow}>
              <AppChip
                label={strings.daily}
                active={viewMode === 'daily'}
                onPress={() => setViewMode('daily')}
              />
              <AppChip
                label={strings.weekly}
                active={viewMode === 'weekly'}
                onPress={() => setViewMode('weekly')}
              />
              <AppChip
                label={strings.monthly}
                active={viewMode === 'monthly'}
                onPress={() => setViewMode('monthly')}
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
            addMealLabel={strings.addMeal}
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
            addMealLabel={strings.addMeal}
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
      </AppScreen>

      <Modal
        visible={editor.isOpen}
        transparent
        animationType="slide"
        onRequestClose={closeEditor}
      >
        {editor.selectedDay && editor.selectedMealType ? (
            <Pressable style={styles.backdrop} onPress={closeEditor}>
              <View style={styles.modalContent}>
                <Pressable style={styles.sheet} onPress={() => null}>
                  <View style={styles.sheetHandle} />
                  <View style={styles.sheetStickyHeader}>
                    <Text style={textStyles.h3}>{strings.planMealTitle}</Text>
                    <Subtle>
                      {`${strings.selectedDayPrefix} ${formatDayLabel(
                        new Date(weekStart.getTime() + (editor.selectedDay - 1) * 86400000),
                        editor.selectedDay - 1
                      )}`}
                    </Subtle>
                    <View style={styles.mealTypeRow}>
                      {MEAL_TYPES.map((mealType) => {
                        const active = mealType === editor.selectedMealType;
                        return (
                          <AppChip
                            key={mealType}
                            label={MEAL_TYPE_LABELS[mealType]}
                            active={active}
                            onPress={() => {
                              Keyboard.dismiss();
                              editor.setSelectedMealType(mealType);
                              const list = mealsByDay.get(editor.selectedDay!) ?? [];
                              const existing = list.find((meal) => meal.mealType === mealType);
                              editor.setRecipeTitle(existing?.recipeTitle ?? '');
                              editor.setSelectedMealRecipeId(existing?.recipeId ?? null);
                              editor.setIngredientsText('');
                            }}
                          />
                        );
                      })}
                    </View>
                  </View>
                  <KeyboardAwareScrollView
                    style={styles.sheetScroll}
                    contentContainerStyle={styles.sheetScrollContent}
                    keyboardShouldPersistTaps="handled"
                    extraKeyboardSpace={-24}
                    bottomOffset={0}
                    showsVerticalScrollIndicator={false}
                  >
                <AppInput
                  placeholder={strings.mealTitlePlaceholder}
                  value={editor.recipeTitle}
                  onChangeText={editor.setRecipeTitle}
                />
                <AppInput
                  placeholder={strings.ingredientsPlaceholder}
                  value={editor.ingredientsText}
                  onChangeText={editor.setIngredientsText}
                  multiline
                  style={styles.ingredientsInput}
                />
                <View style={styles.toggleRow}>
                  <Text style={styles.toggleLabel}>{strings.addIngredientsToShopping}</Text>
                  <Switch value={editor.pushToShopping} onValueChange={editor.setPushToShopping} />
                </View>
                <View style={styles.lists}>
                  {lists.length === 0 ? (
                    <Subtle>{strings.noShoppingLists}</Subtle>
                  ) : (
                    <View style={styles.chipRow}>
                      {lists.map((list) => {
                        const active = list.id === effectiveListId;
                        return (
                          <AppChip
                            key={list.id}
                            label={list.name}
                            active={active}
                            onPress={() => editor.setSelectedListId(list.id)}
                          />
                        );
                      })}
                    </View>
                  )}
                </View>
                {editor.shoppingSyncError ? (
                  <Text style={styles.error}>{strings.shoppingSyncFailed} {editor.shoppingSyncError}</Text>
                ) : null}
                <View style={styles.editorActions}>
                  <AppButton title={strings.saveMeal} onPress={handleSave} fullWidth />
                  {editor.selectedMeal?.recipeTitle && !isKeyboardVisible ? (
                    <AppButton
                      title={strings.removeMeal}
                      onPress={handleRemove}
                      variant="ghost"
                      fullWidth
                    />
                  ) : null}
                  {!isKeyboardVisible ? (
                    <AppButton
                      title={strings.close}
                      onPress={closeEditor}
                      variant="secondary"
                      fullWidth
                    />
                  ) : null}
                </View>
                  </KeyboardAwareScrollView>
                </Pressable>
              </View>
            </Pressable>
        ) : null}
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
    backgroundColor: theme.colors.bg,
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
    paddingTop: 90,
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
    ...textStyles.h3,
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
    ...textStyles.h3,
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
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 999,
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
    ...textStyles.h3,
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
    backgroundColor: theme.colors.primarySoft,
    borderColor: theme.colors.primary,
  },
  monthCellText: {
    ...textStyles.subtle,
    color: theme.colors.text,
  },
  monthCellTextToday: {
    color: theme.colors.primary,
    fontWeight: '700',
  },
  monthCountBadge: {
    alignSelf: 'flex-end',
    minWidth: 18,
    height: 18,
    borderRadius: 999,
    paddingHorizontal: 4,
    backgroundColor: theme.colors.primary,
    alignItems: 'center',
    justifyContent: 'center',
  },
  monthCountBadgeText: {
    color: '#fff',
    fontSize: 11,
    fontWeight: '700',
  },
  ingredientsInput: {
    minHeight: 64,
    textAlignVertical: 'top',
  },
  toggleRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  toggleLabel: {
    fontWeight: '600',
    color: theme.colors.text,
    fontFamily: theme.typography.body,
  },
  lists: {
    gap: theme.spacing.xs,
  },
  chipRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: theme.spacing.sm,
  },
  mealTypeRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: theme.spacing.sm,
  },
  editorActions: {
    gap: theme.spacing.sm,
  },
  error: {
    color: theme.colors.danger,
    fontFamily: theme.typography.body,
  },
  backdrop: {
    position: 'absolute',
    left: 0,
    right: 0,
    top: 0,
    bottom: 0,
    backgroundColor: 'rgba(0,0,0,0.4)',
    justifyContent: 'flex-end',
  },
  modalContent: {
    flex: 1,
    justifyContent: 'flex-end',
  },
  sheet: {
    backgroundColor: theme.colors.surface,
    borderTopLeftRadius: 0,
    borderTopRightRadius: 0,
    maxHeight: '95%',
    paddingTop: theme.spacing.lg,
    paddingHorizontal: theme.spacing.lg,
    borderWidth: 1,
    borderColor: theme.colors.border,
  },
  sheetScroll: {
    flexGrow: 0,
  },
  sheetScrollContent: {
    paddingTop: theme.spacing.sm,
    paddingBottom: theme.spacing.lg,
    gap: theme.spacing.sm,
  },
  sheetStickyHeader: {
    gap: theme.spacing.sm,
    paddingBottom: theme.spacing.sm,
    borderBottomWidth: 1,
    borderBottomColor: theme.colors.border,
  },
  sheetHandle: {
    alignSelf: 'center',
    width: 48,
    height: 5,
    borderRadius: 999,
    backgroundColor: theme.colors.borderStrong,
    marginBottom: theme.spacing.sm,
  },
});
