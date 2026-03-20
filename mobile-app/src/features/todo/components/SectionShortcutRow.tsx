import { Pressable, Text } from 'react-native';

type Props = {
  label: string;
  meta?: string | null;
  onPress: () => void;
  styles: Record<string, any>;
  style?: any;
};

export function SectionShortcutRow({ label, meta, onPress, styles, style }: Props) {
  return (
    <Pressable style={[styles.unplannedShortcutRow, style]} onPress={onPress}>
      <Text style={styles.unplannedShortcutText ?? styles.itemText}>{label}</Text>
      <Text style={styles.unplannedShortcutMeta ?? styles.itemMeta}>
        {meta ? `${meta} ›` : '›'}
      </Text>
    </Pressable>
  );
}
