import { Pressable, Text } from 'react-native';

type Props = {
  label: string;
  onPress: () => void;
  styles: Record<string, any>;
  style?: any;
};

export function SectionShortcutRow({ label, onPress, styles, style }: Props) {
  return (
    <Pressable style={[styles.unplannedShortcutRow, style]} onPress={onPress}>
      <Text style={styles.itemText}>{label}</Text>
      <Text style={styles.itemHintChevron}>›</Text>
    </Pressable>
  );
}
