import { Pressable, Text, View } from 'react-native';
import { Subtle } from '../../../shared/ui/components';
import { type ShoppingListResponse } from '../api/shoppingApi';

type Props = {
  list: ShoppingListResponse;
  openCount: number;
  totalCount: number;
  strings: {
    open: string;
    total: string;
  };
  styles: any;
  isDragging: boolean;
  onPress: () => void;
  onLongPress: (event: any) => void;
  onOpenActions: () => void;
};

export function ShoppingListRow({
  list,
  openCount,
  totalCount,
  strings,
  styles,
  isDragging,
  onPress,
  onLongPress,
  onOpenActions,
}: Props) {
  return (
    <View style={[styles.listCard, isDragging ? styles.listCardDragging : null]}>
      <Pressable
        style={({ pressed }) => [styles.listMainPressable, pressed ? styles.listCardPressed : null]}
        onPress={onPress}
        onLongPress={onLongPress}
        delayLongPress={180}
      >
        <View style={styles.listMain}>
          <Text style={styles.listTitle} numberOfLines={1} ellipsizeMode="tail">
            {list.name}
          </Text>
          <Subtle>
            {openCount} {strings.open} · {totalCount} {strings.total}
          </Subtle>
        </View>
      </Pressable>
      <Pressable
        style={({ pressed }) => [styles.listMenuButton, pressed ? styles.listMenuButtonPressed : null]}
        onPress={onOpenActions}
      >
        <Text style={styles.listMenuText}>⋮</Text>
      </Pressable>
    </View>
  );
}
