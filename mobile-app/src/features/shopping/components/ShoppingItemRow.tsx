import { Pressable, Text, View } from 'react-native';
import { type ShoppingItemResponse } from '../api/shoppingApi';

type Props = {
  item: ShoppingItemResponse;
  title: string;
  checked: boolean;
  detailLabel: string;
  dragging?: boolean;
  styles: any;
  onToggle: () => void;
  onEdit: () => void;
  onToggleLongPress?: (event: any) => void;
  onToggleTouchMove?: (event: any) => void;
  onToggleTouchEnd?: () => void;
  onToggleTouchCancel?: () => void;
};

export function ShoppingItemRow({
  item,
  title,
  checked,
  detailLabel,
  dragging = false,
  styles,
  onToggle,
  onEdit,
  onToggleLongPress,
  onToggleTouchMove,
  onToggleTouchEnd,
  onToggleTouchCancel,
}: Props) {
  return (
    <View style={[styles.itemRow, dragging ? styles.itemRowDragging : null]}>
      <Pressable
        style={styles.toggleZone}
        onLongPress={onToggleLongPress}
        delayLongPress={180}
        onTouchMove={onToggleTouchMove}
        onTouchEnd={onToggleTouchEnd}
        onTouchCancel={onToggleTouchCancel}
        onPress={onToggle}
      >
        {checked ? (
          <View style={[styles.checkbox, styles.checkboxChecked]}>
            <Text style={[styles.checkboxMark, styles.checkboxMarkChecked]}>✓</Text>
          </View>
        ) : (
          <View style={styles.checkbox} />
        )}
        <View style={styles.itemContent}>
          <Text style={[styles.itemText, checked ? styles.itemTextDone : null]}>{title}</Text>
        </View>
      </Pressable>
      <Pressable style={styles.detailZone} onPress={onEdit}>
        <Text style={[styles.itemHintText, checked ? styles.itemTextDone : null]}>{detailLabel}</Text>
        <Text style={[styles.itemHintChevron, checked ? styles.itemTextDone : null]}>›</Text>
      </Pressable>
    </View>
  );
}
