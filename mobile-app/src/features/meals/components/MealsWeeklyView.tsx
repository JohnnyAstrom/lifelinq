import { Pressable, Text, View } from 'react-native';
import { AppCard } from '../../../shared/ui/components';
import { MealRow } from './MealRow';

type MealType = 'BREAKFAST' | 'LUNCH' | 'DINNER';

type MealEntry = {
  dayOfWeek: number;
  mealType: MealType;
  mealTitle: string;
  recipeId: string | null;
  recipeTitle: string | null;
};

type Props = {
  weekStart: Date;
  mealsByDay: Map<number, MealEntry[]>;
  onOpenDay: (date: Date) => void;
  onOpenEditor: (day: number, mealType: MealType) => void;
  formatDayLabel: (date: Date, dayIndex: number) => string;
  DAY_LABELS: string[];
  MEAL_TYPE_LABELS: Record<MealType, string>;
  styles: Record<string, any>;
  emptyText?: string;
  todayLabel: string;
};

function isSameCalendarDay(left: Date, right: Date) {
  return left.getFullYear() === right.getFullYear()
    && left.getMonth() === right.getMonth()
    && left.getDate() === right.getDate();
}

export function MealsWeeklyView({
  weekStart,
  mealsByDay,
  onOpenDay,
  onOpenEditor,
  formatDayLabel,
  DAY_LABELS,
  MEAL_TYPE_LABELS,
  styles,
  emptyText,
  todayLabel,
}: Props) {
  const today = new Date();
  return (
    <AppCard style={styles.weekPlannerCard}>
      {DAY_LABELS.map((_, index) => {
        const day = index + 1;
        const date = new Date(weekStart.getTime());
        date.setUTCDate(weekStart.getUTCDate() + index);
        const localDate = new Date(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate());
        const label = formatDayLabel(date, index);
        const meals = mealsByDay.get(day) ?? [];
        const isToday = isSameCalendarDay(localDate, today);
        const hasMeals = meals.length > 0;
        return (
          <Pressable
            key={label}
            onPress={() => onOpenDay(localDate)}
            style={({ pressed }) => [
              styles.weekPlannerRow,
              index > 0 ? styles.weekPlannerRowBorder : null,
              pressed ? styles.weekPlannerRowPressed : null,
            ]}
          >
            <View style={styles.dayRowInner}>
              <View style={styles.dayHeader}>
                <Text style={[styles.dayLabel, isToday ? styles.dayLabelToday : null]}>
                  {isToday ? `${todayLabel} · ${label}` : label}
                </Text>
                {hasMeals ? (
                  <Text style={styles.daySummaryText}>
                    {meals.length} {meals.length === 1 ? 'meal' : 'meals'}
                  </Text>
                ) : null}
              </View>
              {!hasMeals ? (
                emptyText ? (
                <Text style={styles.weeklyEmptyText}>{emptyText}</Text>
                ) : null
              ) : (
                <View style={styles.mealList}>
                  {meals.map((meal) => (
                    <MealRow
                      key={`${meal.dayOfWeek}-${meal.mealType}`}
                      mealType={meal.mealType}
                      mealTitle={meal.mealTitle}
                      onPress={() => onOpenEditor(day, meal.mealType)}
                      mealTypeLabels={MEAL_TYPE_LABELS}
                      styles={styles}
                    />
                  ))}
                </View>
              )}
            </View>
          </Pressable>
        );
      })}
    </AppCard>
  );
}
