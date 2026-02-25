import { View } from 'react-native';
import {
  AppButton,
  AppCard,
  SectionTitle,
  Subtle,
} from '../../../shared/ui/components';
import { MealRow } from './MealRow';

type MealType = 'BREAKFAST' | 'LUNCH' | 'DINNER';

type MealEntry = {
  dayOfWeek: number;
  mealType: MealType;
  recipeId: string;
  recipeTitle: string;
};

type Props = {
  dailyDayNumber: number;
  dailyMeals: MealEntry[];
  onOpenEditor: (day: number, mealType: MealType) => void;
  MEAL_TYPE_LABELS: Record<MealType, string>;
  styles: Record<string, any>;
  addMealLabel: string;
  emptyText: string;
  title: string;
};

export function MealsDailyView({
  dailyDayNumber,
  dailyMeals,
  onOpenEditor,
  MEAL_TYPE_LABELS,
  styles,
  addMealLabel,
  emptyText,
  title,
}: Props) {
  return (
    <AppCard>
      <View style={styles.dayHeader}>
        <SectionTitle>{title}</SectionTitle>
        <AppButton
          title={addMealLabel}
          onPress={() => onOpenEditor(dailyDayNumber, 'DINNER')}
          variant="secondary"
        />
      </View>
      {dailyMeals.length === 0 ? (
        <Subtle>{emptyText}</Subtle>
      ) : (
        <View style={[styles.mealList, styles.dailyMealList]}>
          {dailyMeals.map((meal) => (
            <MealRow
              key={`${meal.dayOfWeek}-${meal.mealType}`}
              mealType={meal.mealType}
              recipeTitle={meal.recipeTitle}
              onPress={() => onOpenEditor(dailyDayNumber, meal.mealType)}
              mealTypeLabels={MEAL_TYPE_LABELS}
              styles={styles}
            />
          ))}
        </View>
      )}
    </AppCard>
  );
}
