import { Text, View } from 'react-native';
import { AppButton, AppCard } from '../../../shared/ui/components';
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
  addMealLabel: string;
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
  addMealLabel,
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
          <AppCard key={label} style={styles.dayCard}>
            <View style={styles.dayHeader}>
              <Text style={styles.dayLabel}>{label}</Text>
              <AppButton
                title={addMealLabel}
                onPress={() => onOpenEditor(day, 'DINNER')}
                variant="secondary"
              />
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
        );
      })}
    </View>
  );
}
