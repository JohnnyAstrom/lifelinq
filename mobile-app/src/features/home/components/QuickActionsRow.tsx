import { Pressable, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { textStyles, theme } from '../../../shared/ui/theme';
import type { HomeQuickAction } from '../utils/homeOverview';

type Props = {
  actions: HomeQuickAction[];
};

export function QuickActionsRow({ actions }: Props) {
  return (
    <View style={styles.container}>
      {actions.map((action) => (
        <Pressable
          key={action.id}
          onPress={action.onPress}
          accessibilityRole="button"
          style={({ pressed }) => [styles.action, pressed ? styles.pressed : null]}
        >
          <View style={styles.iconWrap}>
            <Ionicons name={action.icon} size={20} color={theme.colors.primary} />
          </View>
          <Text style={styles.label}>{action.label}</Text>
        </Pressable>
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    gap: 16,
  },
  action: {
    flex: 1,
    minHeight: 56,
    borderRadius: 16,
    paddingHorizontal: 16,
    paddingVertical: 16,
    backgroundColor: theme.colors.surface,
    borderWidth: 1,
    borderColor: theme.colors.border,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
  },
  iconWrap: {
    width: 32,
    height: 32,
    borderRadius: 16,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: theme.colors.primarySoft,
  },
  label: {
    ...textStyles.h3,
    flex: 1,
  },
  pressed: {
    opacity: 0.85,
  },
});
