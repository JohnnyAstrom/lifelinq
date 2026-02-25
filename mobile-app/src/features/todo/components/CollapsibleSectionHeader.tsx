import { Pressable, Text } from 'react-native';

type Props = {
  title: string;
  expanded: boolean;
  onPress: () => void;
  styles: Record<string, any>;
};

export function CollapsibleSectionHeader({ title, expanded, onPress, styles }: Props) {
  return (
    <Pressable style={styles.collapsibleHeader} onPress={onPress}>
      <Text style={styles.todoSectionTitle}>{title}</Text>
      <Text style={styles.itemHintChevron}>{expanded ? '⌄' : '›'}</Text>
    </Pressable>
  );
}

