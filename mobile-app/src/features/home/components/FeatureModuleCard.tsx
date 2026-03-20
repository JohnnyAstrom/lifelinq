import { Pressable, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { AppCard } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';
import type { HomeFeatureModule } from '../utils/homeOverview';

type Props = HomeFeatureModule;

export function FeatureModuleCard({
  title,
  statusText,
  icon,
  accentColor,
  accentSoft,
  onPress,
}: Props) {
  return (
    <Pressable
      onPress={onPress}
      accessibilityRole="button"
      style={({ pressed }) => [styles.pressable, pressed ? styles.pressed : null]}
    >
      <AppCard style={styles.card}>
        <View style={styles.topRow}>
          <View style={[styles.iconWrap, { backgroundColor: accentSoft }]}> 
            <Ionicons name={icon} size={20} color={accentColor} />
          </View>
          <Ionicons name="chevron-forward" size={16} color={theme.colors.borderStrong} />
        </View>
        <View style={styles.body}>
          <Text style={textStyles.h3}>{title}</Text>
          <Text style={styles.statusText} numberOfLines={1}>
            {statusText}
          </Text>
        </View>
      </AppCard>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  pressable: {
    flex: 1,
  },
  card: {
    flex: 1,
    minHeight: 128,
    paddingHorizontal: 16,
    paddingVertical: 16,
    gap: 16,
  },
  topRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  iconWrap: {
    width: 40,
    height: 40,
    borderRadius: 20,
    alignItems: 'center',
    justifyContent: 'center',
  },
  body: {
    gap: 8,
  },
  statusText: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
  },
  pressed: {
    opacity: 0.9,
  },
});
