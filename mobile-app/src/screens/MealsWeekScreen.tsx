import { useMemo, useState } from 'react';
import {
  Pressable,
  StyleSheet,
  Switch,
  Text,
  View,
} from 'react-native';
import { useWeekPlan } from '../features/meals/hooks/useWeekPlan';
import { useShoppingLists } from '../features/shopping/hooks/useShoppingLists';
import {
  AppButton,
  AppCard,
  AppChip,
  AppInput,
  AppScreen,
  SectionTitle,
  Subtle,
  TopBar,
} from '../shared/ui/components';
import { textStyles, theme } from '../shared/ui/theme';
import { addShoppingItem } from '../shared/api/shopping';
import { formatApiError } from '../shared/api/client';
import { useAuth } from '../shared/auth/AuthContext';

type Props = {
  token: string;
  onDone: () => void;
};

type ViewMode = 'daily' | 'weekly' | 'monthly';

type MealEntry = {
  dayOfWeek: number;
  mealType: (typeof MEAL_TYPES)[number];
  recipeTitle: string;
};

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

const MEAL_ORDER = new Map(MEAL_TYPES.map((type, index) => [type, index]));

function uuidv4() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    const v = c === 'x' ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

function addDays(date: Date, days: number) {
  const next = new Date(date.getTime());
  next.setDate(next.getDate() + days);
  return next;
}

function getIsoWeekParts(date: Date): { year: number; isoWeek: number } {
  const target = new Date(date.getTime());
  target.setHours(0, 0, 0, 0);
  const day = target.getDay() || 7;
  target.setDate(target.getDate() + 4 - day);
  const yearStart = new Date(target.getFullYear(), 0, 1);
  const diff = target.getTime() - yearStart.getTime();
  const dayMs = 24 * 60 * 60 * 1000;
  const week = Math.ceil((diff / dayMs + 1) / 7);
  return { year: target.getFullYear(), isoWeek: week };
}

function getWeekStartDate(year: number, isoWeek: number): Date {
  const date = new Date(Date.UTC(year, 0, 4));
  const day = date.getUTCDay() || 7;
  date.setUTCDate(date.getUTCDate() + 1 - day);
  date.setUTCDate(date.getUTCDate() + (isoWeek - 1) * 7);
  return date;
}

function formatDayLabel(date: Date, dayIndex: number) {
  const dayLabel = DAY_LABELS[dayIndex];
  const day = String(date.getUTCDate()).padStart(2, '0');
  const month = MONTH_LABELS[date.getUTCMonth()];
  return `${dayLabel} ${day} ${month}`;
}

function formatMonthLabel(date: Date) {
  return `${MONTH_LABELS[date.getMonth()]} ${date.getFullYear()}`;
}

function getWeeksInMonth(anchorDate: Date) {
  const startOfMonth = new Date(Date.UTC(anchorDate.getFullYear(), anchorDate.getMonth(), 1));
  const endOfMonth = new Date(Date.UTC(anchorDate.getFullYear(), anchorDate.getMonth() + 1, 0));
  const weeks: { year: number; isoWeek: number; start: Date; end: Date }[] = [];
  let cursor = new Date(startOfMonth.getTime());
  while (cursor <= endOfMonth) {
    const { year, isoWeek } = getIsoWeekParts(cursor);
    const start = getWeekStartDate(year, isoWeek);
    const end = addDays(start, 6);
    if (!weeks.find((week) => week.year === year && week.isoWeek === isoWeek)) {
      weeks.push({ year, isoWeek, start, end });
    }
    cursor = addDays(cursor, 7);
  }
  return weeks;
}

function toIngredientsList(value: string) {
  return value
    .split('\n')
    .flatMap((line) => line.split(','))
    .map((item) => item.trim())
    .filter((item) => item.length > 0);
}

export function MealsWeekScreen({ token, onDone }: Props) {
  const { handleApiError } = useAuth();
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
  const plan = useWeekPlan(token, year, isoWeek);
  const shopping = useShoppingLists(token);

  const [selectedDay, setSelectedDay] = useState<number | null>(null);
  const [selectedMealType, setSelectedMealType] = useState<(typeof MEAL_TYPES)[number] | null>('DINNER');
  const [recipeTitle, setRecipeTitle] = useState('');
  const [ingredientsText, setIngredientsText] = useState('');
  const [pushToShopping, setPushToShopping] = useState(true);
  const [selectedListId, setSelectedListId] = useState<string | null>(null);
  const [shoppingSyncError, setShoppingSyncError] = useState<string | null>(null);

  const mealsByDay = useMemo(() => {
    const map = new Map<number, MealEntry[]>();
    if (plan.data) {
      for (const meal of plan.data.meals) {
        const entry: MealEntry = {
          dayOfWeek: meal.dayOfWeek,
          mealType: meal.mealType as (typeof MEAL_TYPES)[number],
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

  const lists = shopping.lists;
  const effectiveListId =
    selectedListId ?? (lists.length > 0 ? lists[0].id : null);

  const selectedMealTitle = useMemo(() => {
    if (!selectedDay || !selectedMealType) {
      return '';
    }
    const list = mealsByDay.get(selectedDay) ?? [];
    return list.find((meal) => meal.mealType === selectedMealType)?.recipeTitle ?? '';
  }, [selectedDay, selectedMealType, mealsByDay]);

  function openEditor(day: number, mealType: (typeof MEAL_TYPES)[number]) {
    setSelectedDay(day);
    setSelectedMealType(mealType);
    const list = mealsByDay.get(day) ?? [];
    const existing = list.find((meal) => meal.mealType === mealType)?.recipeTitle ?? '';
    setRecipeTitle(existing);
    setIngredientsText('');
    setShoppingSyncError(null);
  }

  async function handleSave() {
    if (!selectedDay || !selectedMealType) {
      return;
    }
    if (!recipeTitle.trim()) {
      return;
    }

    await plan.addMeal(selectedDay, selectedMealType, {
      recipeId: uuidv4(),
      recipeTitle: recipeTitle.trim(),
      mealType: selectedMealType,
      targetShoppingListId: null,
    });

    const ingredients = toIngredientsList(ingredientsText);
    if (pushToShopping && ingredients.length > 0 && effectiveListId) {
      let syncFailed = false;
      for (const ingredient of ingredients) {
        try {
          await addShoppingItem(effectiveListId, { name: ingredient }, { token });
        } catch (err) {
          syncFailed = true;
          await handleApiError(err);
          setShoppingSyncError(formatApiError(err));
        }
      }
      if (!syncFailed) {
        await shopping.reload();
      } else {
        return;
      }
    }

    setRecipeTitle('');
    setIngredientsText('');
    setSelectedDay(null);
    setSelectedMealType('DINNER');
    setShoppingSyncError(null);
  }

  async function handleRemove() {
    if (!selectedDay || !selectedMealType) {
      return;
    }
    await plan.removeMeal(selectedDay, selectedMealType);
    setSelectedDay(null);
    setSelectedMealType('DINNER');
    setRecipeTitle('');
    setIngredientsText('');
    setShoppingSyncError(null);
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
          <AppCard>
            <View style={styles.headerRow}>
              <AppButton
                title={strings.prev}
                onPress={() => setAnchorDate(addDays(anchorDate, -7))}
                variant="ghost"
              />
              <Text style={styles.headerText}>
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
          <AppCard>
            <View style={styles.headerRow}>
              <AppButton
                title={strings.prev}
                onPress={() => setAnchorDate(addDays(anchorDate, -1))}
                variant="ghost"
              />
              <Text style={styles.headerText}>
                {formatDayLabel(anchorDate, dailyDayIndex)}
              </Text>
              <AppButton
                title={strings.next}
                onPress={() => setAnchorDate(addDays(anchorDate, 1))}
                variant="ghost"
              />
            </View>
            <Subtle>
              {strings.weekLabel} {isoWeek} · {year}
            </Subtle>
          </AppCard>
        ) : null}

        {viewMode === 'monthly' ? (
          <AppCard>
            <View style={styles.headerRow}>
              <AppButton
                title={strings.prev}
                onPress={() => setAnchorDate(addDays(anchorDate, -30))}
                variant="ghost"
              />
              <Text style={styles.headerText}>{formatMonthLabel(anchorDate)}</Text>
              <AppButton
                title={strings.next}
                onPress={() => setAnchorDate(addDays(anchorDate, 30))}
                variant="ghost"
              />
            </View>
            <Subtle>{strings.monthlyOverview}</Subtle>
          </AppCard>
        ) : null}

        {plan.loading ? <Subtle>{strings.loadingPlan}</Subtle> : null}
        {plan.error ? <Text style={styles.error}>{plan.error}</Text> : null}

        {viewMode === 'weekly' ? (
          <View style={styles.dayList}>
            {DAY_LABELS.map((_, index) => {
              const day = index + 1;
              const date = new Date(weekStart.getTime());
              date.setUTCDate(weekStart.getUTCDate() + index);
              const label = formatDayLabel(date, index);
              const meals = mealsByDay.get(day) ?? [];
              return (
                <AppCard key={label} style={styles.dayCard}>
                  <View style={styles.dayHeader}>
                    <Text style={styles.dayLabel}>{label}</Text>
                    <AppButton
                      title={strings.addMeal}
                      onPress={() => openEditor(day, 'DINNER')}
                      variant="secondary"
                    />
                  </View>
                  {meals.length === 0 ? (
                    <Subtle>{strings.noMeals}</Subtle>
                  ) : (
                    <View style={styles.mealList}>
                      {meals.map((meal) => (
                        <Pressable
                          key={`${meal.dayOfWeek}-${meal.mealType}`}
                          style={styles.mealRow}
                          onPress={() => openEditor(day, meal.mealType)}
                        >
                          <View style={[styles.mealTypeBadge, styles[`mealType_${meal.mealType}`]]}>
                            <Text style={styles.mealTypeText}>{MEAL_TYPE_LABELS[meal.mealType]}</Text>
                          </View>
                          <Text style={styles.mealTitle}>{meal.recipeTitle}</Text>
                        </Pressable>
                      ))}
                    </View>
                  )}
                </AppCard>
              );
            })}
          </View>
        ) : null}

        {viewMode === 'daily' ? (
          <AppCard>
            <View style={styles.dayHeader}>
              <SectionTitle>{strings.title}</SectionTitle>
              <AppButton
                title={strings.addMeal}
                onPress={() => openEditor(dailyDayNumber, 'DINNER')}
                variant="secondary"
              />
            </View>
            {dailyMeals.length === 0 ? (
              <Subtle>{strings.noMeals}</Subtle>
            ) : (
              <View style={styles.mealList}>
                {dailyMeals.map((meal) => (
                  <Pressable
                    key={`${meal.dayOfWeek}-${meal.mealType}`}
                    style={styles.mealRow}
                    onPress={() => openEditor(dailyDayNumber, meal.mealType)}
                  >
                    <View style={[styles.mealTypeBadge, styles[`mealType_${meal.mealType}`]]}>
                      <Text style={styles.mealTypeText}>{MEAL_TYPE_LABELS[meal.mealType]}</Text>
                    </View>
                    <Text style={styles.mealTitle}>{meal.recipeTitle}</Text>
                  </Pressable>
                ))}
              </View>
            )}
          </AppCard>
        ) : null}

        {viewMode === 'monthly' ? (
          <AppCard>
            <SectionTitle>{strings.weeksTitle}</SectionTitle>
            <View style={styles.mealList}>
              {getWeeksInMonth(anchorDate).map((week) => {
                const isCurrent = week.year === year && week.isoWeek === isoWeek;
                return (
                  <Pressable
                    key={`${week.year}-${week.isoWeek}`}
                    style={styles.weekRow}
                    onPress={() => {
                      setAnchorDate(week.start);
                      setViewMode('weekly');
                    }}
                  >
                    <View>
                      <Text style={styles.weekTitle}>Week {week.isoWeek}</Text>
                      <Subtle>
                        {formatDayLabel(week.start, 0)} — {formatDayLabel(week.end, 6)}
                      </Subtle>
                    </View>
                    <Text style={styles.weekCount}>
                      {isCurrent ? currentWeekMealCount : 0} {strings.mealsCount}
                    </Text>
                  </Pressable>
                );
              })}
            </View>
          </AppCard>
        ) : null}

        </View>
      </AppScreen>

      {selectedDay && selectedMealType ? (
        <Pressable style={styles.backdrop} onPress={() => setSelectedDay(null)}>
          <Pressable style={styles.sheet} onPress={() => null}>
            <View style={styles.sheetHandle} />
            <Text style={textStyles.h3}>{strings.planMealTitle}</Text>
            <Subtle>
              {`${strings.selectedDayPrefix} ${formatDayLabel(
                new Date(weekStart.getTime() + (selectedDay - 1) * 86400000),
                selectedDay - 1
              )}`}
            </Subtle>
            <View style={styles.mealTypeRow}>
              {MEAL_TYPES.map((mealType) => {
                const active = mealType === selectedMealType;
                return (
                  <AppChip
                    key={mealType}
                    label={MEAL_TYPE_LABELS[mealType]}
                    active={active}
                    onPress={() => {
                      setSelectedMealType(mealType);
                      const list = mealsByDay.get(selectedDay) ?? [];
                      const existing = list.find((meal) => meal.mealType === mealType)?.recipeTitle ?? '';
                      setRecipeTitle(existing);
                      setIngredientsText('');
                    }}
                  />
                );
              })}
            </View>
            <AppInput
              placeholder={strings.mealTitlePlaceholder}
              value={recipeTitle}
              onChangeText={setRecipeTitle}
              autoFocus
            />
            <AppInput
              placeholder={strings.ingredientsPlaceholder}
              value={ingredientsText}
              onChangeText={setIngredientsText}
              multiline
              style={styles.ingredientsInput}
            />
            <View style={styles.toggleRow}>
              <Text style={styles.toggleLabel}>{strings.addIngredientsToShopping}</Text>
              <Switch value={pushToShopping} onValueChange={setPushToShopping} />
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
                        onPress={() => setSelectedListId(list.id)}
                      />
                    );
                  })}
                </View>
              )}
            </View>
            {shoppingSyncError ? (
              <Text style={styles.error}>{strings.shoppingSyncFailed} {shoppingSyncError}</Text>
            ) : null}
            <View style={styles.editorActions}>
              <AppButton title={strings.saveMeal} onPress={handleSave} fullWidth />
              {selectedMealTitle ? (
                <AppButton
                  title={strings.removeMeal}
                  onPress={handleRemove}
                  variant="ghost"
                  fullWidth
                />
              ) : null}
              <AppButton title={strings.close} onPress={() => setSelectedDay(null)} variant="secondary" fullWidth />
            </View>
          </Pressable>
        </Pressable>
      ) : null}
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
  contentOffset: {
    paddingTop: 90,
    gap: theme.spacing.md,
  },
  viewSwitchRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: theme.spacing.sm,
    marginTop: theme.spacing.sm,
  },
  headerRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  headerText: {
    ...textStyles.h2,
  },
  dayList: {
    gap: theme.spacing.md,
  },
  dayCard: {
    gap: theme.spacing.sm,
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
    gap: theme.spacing.sm,
    marginTop: theme.spacing.sm,
  },
  mealRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: theme.spacing.sm,
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    padding: theme.spacing.sm,
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
  ingredientsInput: {
    minHeight: 90,
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
  sheet: {
    backgroundColor: theme.colors.surface,
    borderTopLeftRadius: theme.radius.xl,
    borderTopRightRadius: theme.radius.xl,
    padding: theme.spacing.lg,
    gap: theme.spacing.sm,
    borderWidth: 1,
    borderColor: theme.colors.border,
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
