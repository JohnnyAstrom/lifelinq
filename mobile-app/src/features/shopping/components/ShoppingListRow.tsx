import { Ionicons } from '@expo/vector-icons';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { type ShoppingListResponse } from '../api/shoppingApi';
import { getShoppingListTypeDefinition } from '../utils/shoppingListTypes';
import { textStyles, theme } from '../../../shared/ui/theme';

type Props = {
  list: ShoppingListResponse;
  openCount: number;
  totalCount: number;
  strings: {
    toBuy: string;
    allBought: string;
    empty: string;
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
  const listType = getShoppingListTypeDefinition(list.type);
  const statusLabel = openCount > 0
    ? `${openCount} ${strings.toBuy}`
    : totalCount > 0
      ? strings.allBought
      : strings.empty;

  return (
    <View style={[styles.listCard, isDragging ? styles.listCardDragging : null]}>
      <Pressable
        style={({ pressed }) => [
          styles.listMainPressable,
          pressed ? styles.listCardPressed : null,
        ]}
        onPress={onPress}
        onLongPress={onLongPress}
        accessibilityRole="button"
      >
        <View style={localStyles.mainRow}>
          <View style={localStyles.typeIconWrap}>
            <Ionicons name={listType.icon as any} size={20} color={theme.colors.feature.shopping} />
          </View>
          <View style={styles.listMain}>
            <View style={localStyles.titleBlock}>
              <Text style={localStyles.typeLabel}>
                {listType.label}
              </Text>
              <Text style={styles.listTitle} numberOfLines={1} ellipsizeMode="tail">
                {list.name}
              </Text>
            </View>
            <Text style={localStyles.statusLabel} numberOfLines={1}>
              {statusLabel}
            </Text>
          </View>
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

const localStyles = StyleSheet.create({
  mainRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: theme.spacing.sm,
    minWidth: 0,
  },
  titleBlock: {
    gap: 2,
    minWidth: 0,
  },
  typeIconWrap: {
    width: 40,
    height: 40,
    borderRadius: 20,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'rgba(80, 126, 178, 0.14)',
    borderWidth: 1,
    borderColor: 'rgba(80, 126, 178, 0.18)',
    flexShrink: 0,
  },
  typeLabel: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
    fontWeight: '600',
  },
  statusLabel: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
  },
});
