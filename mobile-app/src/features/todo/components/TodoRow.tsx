import { Pressable, Text, View } from 'react-native';

type TodoRowItem = {
  id: string;
  text: string;
  status: 'OPEN' | 'COMPLETED';
  dueDate?: string | null;
  dueTime?: string | null;
};

type Props = {
  item: TodoRowItem;
  variant?: 'default' | 'daily';
  onToggleComplete: (id: string) => void;
  onEdit: (id: string) => void;
  formatDueLabel: (dueDate?: string | null, dueTime?: string | null) => string | null;
  editLabel: string;
  styles: Record<string, any>;
};

export function TodoRow({
  item,
  variant = 'default',
  onToggleComplete,
  onEdit,
  formatDueLabel,
  editLabel,
  styles,
}: Props) {
  const dueLabel = formatDueLabel(item.dueDate, item.dueTime);

  return (
    <View style={[styles.itemRow, variant === 'daily' ? styles.itemRowDaily : null]}>
      <Pressable style={styles.checkboxPressable} onPress={() => onToggleComplete(item.id)}>
        <View style={[styles.checkbox, item.status === 'COMPLETED' ? styles.checkboxChecked : null]}>
          {item.status === 'COMPLETED' ? <Text style={styles.checkboxMark}>✓</Text> : null}
        </View>
      </Pressable>
      <View style={styles.itemInfo}>
        <Text style={[styles.itemText, item.status === 'COMPLETED' ? styles.itemTextDone : null]}>
          {item.text}
        </Text>
        {dueLabel ? <Text style={styles.itemMeta}>{dueLabel}</Text> : null}
      </View>
      <Pressable style={styles.detailZone} onPress={() => onEdit(item.id)}>
        <Text style={styles.itemHintText}>{editLabel}</Text>
        <Text style={styles.itemHintChevron}>›</Text>
      </Pressable>
    </View>
  );
}

