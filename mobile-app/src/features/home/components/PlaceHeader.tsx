import { Pressable, StyleSheet, Text, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { Subtle } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';

type Props = {
  title: string;
  subtitle?: string;
  canSwitch: boolean;
  onPressPlace: () => void;
  onPressSettings: () => void;
};

export function PlaceHeader({ title, subtitle, canSwitch, onPressPlace, onPressSettings }: Props) {
  const titleBlock = (
    <>
      <View style={styles.titleRow}>
        <Text style={styles.title}>{title}</Text>
        {canSwitch ? (
          <Ionicons name="chevron-down" size={18} color={theme.colors.subtle} />
        ) : null}
      </View>
      {subtitle ? <Subtle>{subtitle}</Subtle> : null}
    </>
  );

  return (
    <View style={styles.container}>
      {canSwitch ? (
        <Pressable
          onPress={onPressPlace}
          accessibilityRole="button"
          style={({ pressed }) => [styles.titlePressable, pressed ? styles.pressed : null]}
        >
          {titleBlock}
        </Pressable>
      ) : (
        <View style={styles.titlePressable}>{titleBlock}</View>
      )}
      <Pressable
        onPress={onPressSettings}
        accessibilityRole="button"
        accessibilityLabel="Settings"
        style={({ pressed }) => [styles.settingsButton, pressed ? styles.pressed : null]}
      >
        <Ionicons name="settings-outline" size={24} color={theme.colors.text} />
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 16,
  },
  titlePressable: {
    flex: 1,
    gap: 8,
  },
  titleRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  title: {
    ...textStyles.h1,
    flexShrink: 1,
  },
  settingsButton: {
    width: 40,
    height: 40,
    borderRadius: 20,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: theme.colors.surface,
    borderWidth: 1,
    borderColor: theme.colors.border,
  },
  pressed: {
    opacity: 0.8,
  },
});
