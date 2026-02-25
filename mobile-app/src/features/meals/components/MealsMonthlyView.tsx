import { View } from 'react-native';
import type { MonthGridCell } from '../utils/mealDates';
import { MealsMonthGrid } from './MealsMonthGrid';

type Props = {
  monthGridCells: MonthGridCell[];
  monthMealCountByDateKey: Record<string, number>;
  onPressDay: (date: Date) => void;
  toDateKey: (date: Date) => string;
  isTodayDate: (date: Date) => boolean;
  styles: Record<string, any>;
  weekdayLabels?: string[];
};

export function MealsMonthlyView({
  monthGridCells,
  monthMealCountByDateKey,
  onPressDay,
  toDateKey,
  isTodayDate,
  styles,
  weekdayLabels,
}: Props) {
  return (
    <View>
      <MealsMonthGrid
        cells={monthGridCells}
        mealCountByDateKey={monthMealCountByDateKey}
        toDateKey={toDateKey}
        isTodayDate={isTodayDate}
        styles={styles}
        onPressDay={onPressDay}
        weekdayLabels={weekdayLabels}
      />
    </View>
  );
}
