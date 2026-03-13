import { Ionicons } from '@expo/vector-icons';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { AppRow } from '../../../shared/ui/components';
import { type ShoppingListResponse } from '../api/shoppingApi';
import { getShoppingListTypeDefinition } from '../utils/shoppingListTypes';
import { textStyles, theme } from '../../../shared/ui/theme';

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
  const listType = getShoppingListTypeDefinition(list.type);
  const subtitle = `${listType.label} · ${openCount} ${strings.open} · ${totalCount} ${strings.total}`;

  return (
    <View style={[styles.listCard, isDragging ? styles.listCardDragging : null]}>
      <AppRow
        style={styles.listMainPressable}
        onPress={onPress}
        onLongPress={onLongPress}
        title={(
          <View style={localStyles.titleRow}>
            <View style={localStyles.typeIconWrap}>
              <Ionicons name={listType.icon as any} size={14} color={theme.colors.feature.shopping} />
            </View>
            <Text style={styles.listTitle} numberOfLines={1} ellipsizeMode="tail">
              {list.name}
            </Text>
          </View>
        )}
        subtitle={subtitle}
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

const localStyles = StyleSheet.create({
  titleRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: theme.spacing.xs,
    minWidth: 0,
  },
  typeIconWrap: {
    width: 24,
    height: 24,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'rgba(80, 126, 178, 0.10)',
    flexShrink: 0,
  },
  typeIcon: {
    ...textStyles.subtle,
  },
});
