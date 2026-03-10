import { Pressable, Text, View } from 'react-native';
import { AppRow } from '../../../shared/ui/components';
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
      <AppRow
        style={styles.listMainPressable}
        onPress={onPress}
        onLongPress={onLongPress}
        title={<Text style={styles.listTitle} numberOfLines={1} ellipsizeMode="tail">{list.name}</Text>}
        subtitle={`${openCount} ${strings.open} · ${totalCount} ${strings.total}`}
        contentStyle={styles.listMain}
      />
      <Pressable
        style={({ pressed }) => [styles.listMenuButton, pressed ? styles.listMenuButtonPressed : null]}
        onPress={onOpenActions}
      >
        <Text style={styles.listMenuText}>⋮</Text>
      </Pressable>
    </View>
  );
}
