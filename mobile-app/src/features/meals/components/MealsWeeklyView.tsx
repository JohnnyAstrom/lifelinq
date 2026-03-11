import { Pressable, Text, View } from 'react-native';
import { AppCard } from '../../../shared/ui/components';
import { MealRow } from './MealRow';

type MealType = 'BREAKFAST' | 'LUNCH' | 'DINNER';

type MealEntry = {
  dayOfWeek: number;
  mealType: MealType;
  recipeId: string;
  recipeTitle: string;
};

type Props = {
  weekStart: Date;
  mealsByDay: Map<number, MealEntry[]>;
  onOpenEditor: (day: number, mealType: MealType) => void;
  formatDayLabel: (date: Date, dayIndex: number) => string;
  DAY_LABELS: string[];
  MEAL_TYPE_LABELS: Record<MealType, string>;
  styles: Record<string, any>;
  emptyText: string;
};

export function MealsWeeklyView({
  weekStart,
  mealsByDay,
  onOpenEditor,
  formatDayLabel,
  DAY_LABELS,
  MEAL_TYPE_LABELS,
  styles,
  emptyText,
}: Props) {
  return (
    <View style={styles.dayList}>
      {DAY_LABELS.map((_, index) => {
        const day = index + 1;
        const date = new Date(weekStart.getTime());
        date.setUTCDate(weekStart.getUTCDate() + index);
        const label = formatDayLabel(date, index);
        const meals = mealsByDay.get(day) ?? [];
        return (
          <Pressable key={label} onPress={() => onOpenEditor(day, 'DINNER')}>
            <AppCard style={styles.dayCard}>
              <View style={styles.dayHeader}>
                <Text style={styles.dayLabel}>{label}</Text>
              </View>
              {meals.length === 0 ? (
                <Text style={styles.weeklyEmptyText}>{emptyText}</Text>
              ) : (
                <View style={styles.mealList}>
                  {meals.map((meal) => (
                    <MealRow
                      key={`${meal.dayOfWeek}-${meal.mealType}`}
                      mealType={meal.mealType}
                      recipeTitle={meal.recipeTitle}
                      onPress={() => onOpenEditor(day, meal.mealType)}
                      mealTypeLabels={MEAL_TYPE_LABELS}
                      styles={styles}
                    />
                  ))}
                </View>
              )}
            </AppCard>
          </Pressable>
        );
      })}
    </View>
  );
}
