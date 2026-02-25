import { Pressable, Text, View } from 'react-native';
import type { MonthGridCell } from '../utils/mealDates';

type Props = {
  cells: MonthGridCell[];
  mealCountByDateKey: Record<string, number>;
  toDateKey: (date: Date) => string;
  isTodayDate: (date: Date) => boolean;
  onPressDay: (date: Date) => void;
  styles: Record<string, any>;
  weekdayLabels?: string[];
};

export function MealsMonthGrid({
  cells,
  mealCountByDateKey,
  toDateKey,
  isTodayDate,
  onPressDay,
  styles,
  weekdayLabels = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
}: Props) {
  return (
    <>
      <View style={styles.monthGridHeaderRow}>
        {weekdayLabels.map((label) => (
          <Text key={label} style={styles.monthGridWeekday}>{label}</Text>
        ))}
      </View>
      <View style={styles.monthGrid}>
        {Array.from({ length: 6 }).map((_, rowIndex) => (
          <View key={`meal-month-row-${rowIndex}`} style={styles.monthGridRow}>
            {cells.slice(rowIndex * 7, rowIndex * 7 + 7).map((cell) => {
              const key = toDateKey(cell.date);
              const count = mealCountByDateKey[key] ?? 0;
              if (!cell.isCurrentMonth) {
                return (
                  <View key={key} style={[styles.monthGridCellBase, styles.monthCell, styles.monthCellPlaceholder]}>
                    <Text style={styles.monthCellGhostText}>{cell.date.getDate()}</Text>
                    <View style={styles.monthCellGhostBadge} />
                  </View>
                );
              }
              return (
                <Pressable
                  key={key}
                  style={[styles.monthGridCellBase, styles.monthCell, isTodayDate(cell.date) ? styles.monthCellToday : null]}
                  onPress={() => onPressDay(cell.date)}
                >
                  <Text style={[styles.monthCellText, isTodayDate(cell.date) ? styles.monthCellTextToday : null]}>
                    {cell.date.getDate()}
                  </Text>
                  {count > 0 ? (
                    <View style={styles.monthCountBadge}>
                      <Text style={styles.monthCountBadgeText}>{count}</Text>
                    </View>
                  ) : null}
                </Pressable>
              );
            })}
          </View>
        ))}
      </View>
    </>
  );
}
