import { Pressable, Text, View } from 'react-native';

type MonthGridCell = {
  date: Date;
  isCurrentMonth: boolean;
};

type Props = {
  cells: MonthGridCell[];
  dayTodoCountByDateKey: Record<string, number>;
  toDateKey: (date: Date) => string;
  isToday: (date: Date) => boolean;
  onPressDay: (date: Date) => void;
  styles: Record<string, any>;
  weekdayLabels?: string[];
};

type MonthDayCellProps = {
  cell: MonthGridCell;
  count: number;
  isCurrentDay: boolean;
  onPressDay: (date: Date) => void;
  toDateKey: (date: Date) => string;
  styles: Record<string, any>;
};

function MonthDayCell({ cell, count, isCurrentDay, onPressDay, styles, toDateKey }: MonthDayCellProps) {
  const { date, isCurrentMonth } = cell;
  const key = toDateKey(date);

  if (!isCurrentMonth) {
    return (
      <View key={key} style={[styles.monthGridCellBase, styles.monthCell, styles.monthCellPlaceholder]}>
        <Text style={styles.monthCellGhostText}>{date.getDate()}</Text>
        <View style={styles.monthCellGhostBadge} />
      </View>
    );
  }

  return (
    <Pressable
      key={key}
      style={[styles.monthGridCellBase, styles.monthCell, isCurrentDay ? styles.monthCellToday : null]}
      onPress={() => onPressDay(date)}
    >
      <Text style={[styles.monthCellText, isCurrentDay ? styles.monthCellTextToday : null]}>{date.getDate()}</Text>
      {count > 0 ? (
        <View style={styles.monthCountBadge}>
          <Text style={styles.monthCountBadgeText}>{count}</Text>
        </View>
      ) : null}
    </Pressable>
  );
}

export function MonthGrid({
  cells,
  dayTodoCountByDateKey,
  toDateKey,
  isToday,
  onPressDay,
  styles,
  weekdayLabels = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
}: Props) {
  return (
    <View style={styles.todoSection}>
      <View style={styles.monthGridHeaderRow}>
        {weekdayLabels.map((label) => (
          <Text key={label} style={styles.monthGridWeekday}>{label}</Text>
        ))}
      </View>
      <View style={styles.monthGrid}>
        {Array.from({ length: 6 }).map((_, rowIndex) => (
          <View key={`month-row-${rowIndex}`} style={styles.monthGridRow}>
            {cells.slice(rowIndex * 7, rowIndex * 7 + 7).map((cell) => {
              const count = dayTodoCountByDateKey[toDateKey(cell.date)] ?? 0;
              return (
                <MonthDayCell
                  key={toDateKey(cell.date)}
                  cell={cell}
                  count={count}
                  isCurrentDay={cell.isCurrentMonth && isToday(cell.date)}
                  onPressDay={onPressDay}
                  toDateKey={toDateKey}
                  styles={styles}
                />
              );
            })}
          </View>
        ))}
      </View>
    </View>
  );
}

