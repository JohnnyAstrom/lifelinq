import { Pressable, Text } from 'react-native';

type Props = {
  label: string;
  isToday: boolean;
  openCount: number;
  doneCount: number;
  onPress: () => void;
  styles: Record<string, any>;
};

export function WeekDayOverviewRow({
  label,
  isToday,
  openCount,
  doneCount,
  onPress,
  styles,
}: Props) {
  const total = openCount + doneCount;
  return (
    <Pressable
      style={[styles.weekDayOverviewRow, total === 0 ? styles.weekDayOverviewRowEmpty : null]}
      onPress={onPress}
    >
      <Text style={[styles.weekDayOverviewText, isToday ? styles.weekDayOverviewTextToday : null]}>
        {isToday ? 'Today' : label}
      </Text>
      <Text style={styles.weekDayOverviewMeta ?? styles.itemMeta}>
        {total === 0 ? '0' : `${total} task${total === 1 ? '' : 's'} · ${doneCount} done`}
      </Text>
      <Text style={styles.itemHintChevron}>›</Text>
    </Pressable>
  );
}
