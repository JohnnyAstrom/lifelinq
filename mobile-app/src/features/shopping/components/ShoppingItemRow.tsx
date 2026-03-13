import { Ionicons } from '@expo/vector-icons';
import { type LayoutChangeEvent, Pressable, StyleSheet, Text, View } from 'react-native';
import { iconBackground, textStyles, theme } from '../../../shared/ui/theme';

type Props = {
  title: string;
  meta?: string | null;
  checked: boolean;
  shoppingFocused?: boolean;
  dragging?: boolean;
  secondaryActionLabel?: string;
  onToggle: () => void;
  onOpenDetails: () => void;
  onToggleLongPress?: (event: any) => void;
  onToggleTouchMove?: (event: any) => void;
  onToggleTouchEnd?: () => void;
  onToggleTouchCancel?: () => void;
  onMeasuredHeight?: (height: number) => void;
};

export function ShoppingItemRow({
  title,
  meta,
  checked,
  shoppingFocused = false,
  dragging = false,
  secondaryActionLabel = 'Item details',
  onToggle,
  onOpenDetails,
  onToggleLongPress,
  onToggleTouchMove,
  onToggleTouchEnd,
  onToggleTouchCancel,
  onMeasuredHeight,
}: Props) {
  function handleLayout(event: LayoutChangeEvent) {
    onMeasuredHeight?.(event.nativeEvent.layout.height);
  }

  return (
    <View
      style={[
        styles.row,
        checked ? styles.rowChecked : null,
        dragging ? styles.rowDragging : null,
      ]}
      onLayout={handleLayout}
    >
      <Pressable
        style={styles.primaryZone}
        onLongPress={onToggleLongPress}
        delayLongPress={180}
        onTouchMove={onToggleTouchMove}
        onTouchEnd={onToggleTouchEnd}
        onTouchCancel={onToggleTouchCancel}
        onPress={onToggle}
      >
        {checked ? (
          <View style={[styles.checkbox, styles.checkboxChecked]}>
            <Text style={styles.checkboxMarkChecked}>✓</Text>
          </View>
        ) : (
          <View style={[styles.checkbox, shoppingFocused ? styles.checkboxShoppingFocused : null]} />
        )}
        <View style={styles.content}>
          <Text
            style={[styles.title, checked ? styles.titleChecked : null]}
            numberOfLines={1}
          >
            {title}
          </Text>
          {meta ? (
            <Text
              style={[styles.meta, checked ? styles.metaChecked : null]}
              numberOfLines={1}
            >
              {meta}
            </Text>
          ) : null}
        </View>
      </Pressable>
      <Pressable
        style={[
          styles.secondaryAction,
          shoppingFocused && !checked ? styles.secondaryActionShoppingFocused : null,
          checked ? styles.secondaryActionChecked : null,
        ]}
        onPress={onOpenDetails}
        accessibilityRole="button"
        accessibilityLabel={secondaryActionLabel}
      >
        <Ionicons
          name="ellipsis-horizontal"
          size={18}
          color={checked ? theme.colors.subtle : theme.colors.textSecondary}
        />
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  row: {
    minHeight: 68,
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    paddingVertical: 12,
    paddingHorizontal: 12,
    backgroundColor: theme.colors.surface,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
  },
  rowChecked: {
    backgroundColor: theme.colors.surfaceSubtle,
    borderColor: theme.colors.border,
  },
  rowDragging: {
    borderColor: theme.colors.feature.shopping,
    opacity: 0.86,
  },
  primaryZone: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
  },
  checkbox: {
    width: 24,
    height: 24,
    borderRadius: 8,
    borderWidth: 1.5,
    borderColor: theme.colors.feature.shopping,
    backgroundColor: theme.colors.surface,
    alignItems: 'center',
    justifyContent: 'center',
    flexShrink: 0,
  },
  checkboxShoppingFocused: {
    width: 26,
    height: 26,
    borderWidth: 2,
  },
  checkboxChecked: {
    backgroundColor: theme.colors.success,
    borderColor: theme.colors.success,
  },
  checkboxMarkChecked: {
    fontSize: 14,
    fontWeight: '700',
    color: '#ffffff',
  },
  content: {
    flex: 1,
    minWidth: 0,
    gap: 2,
  },
  title: {
    ...textStyles.body,
    fontWeight: '600',
  },
  titleChecked: {
    color: theme.colors.subtle,
    textDecorationLine: 'line-through',
  },
  meta: {
    ...textStyles.subtle,
  },
  metaChecked: {
    color: theme.colors.subtle,
  },
  secondaryAction: {
    width: 36,
    height: 36,
    borderRadius: 18,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: iconBackground(theme.colors.feature.shopping, 0.12),
  },
  secondaryActionShoppingFocused: {
    backgroundColor: theme.colors.surface,
    borderWidth: 1,
    borderColor: theme.colors.border,
  },
  secondaryActionChecked: {
    backgroundColor: iconBackground(theme.colors.subtle, 0.12),
  },
});
