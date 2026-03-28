import { Ionicons } from '@expo/vector-icons';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { OverlaySheet } from '../../../shared/ui/OverlaySheet';
import { AppButton, Subtle } from '../../../shared/ui/components';
import { iconBackground, textStyles, theme } from '../../../shared/ui/theme';

export type MealRecipeCaptureSourceOption = {
  icon: keyof typeof Ionicons.glyphMap;
  title: string;
  hint: string;
  onPress: () => void;
};

type Strings = {
  title: string;
  subtitle: string;
  close: string;
};

type Props = {
  options: MealRecipeCaptureSourceOption[];
  onClose: () => void;
  strings: Strings;
};

export function MealRecipeCaptureSourceSheet({
  options,
  onClose,
  strings,
}: Props) {
  return (
    <OverlaySheet onClose={onClose} sheetStyle={styles.sheet}>
      <View style={styles.layout}>
        <View style={styles.header}>
          <Text style={styles.title}>{strings.title}</Text>
          <Subtle>{strings.subtitle}</Subtle>
        </View>

        <View style={styles.options}>
          {options.map((option, index) => (
            <Pressable
              key={option.title}
              onPress={option.onPress}
              accessibilityRole="button"
              style={({ pressed }) => [
                styles.optionRow,
                index > 0 ? styles.optionRowBorder : null,
                pressed ? styles.optionRowPressed : null,
              ]}
            >
              <View style={styles.optionIcon}>
                <Ionicons name={option.icon} size={18} color={theme.colors.feature.meals} />
              </View>
              <View style={styles.optionCopy}>
                <Text style={styles.optionTitle}>{option.title}</Text>
                <Text style={styles.optionHint}>{option.hint}</Text>
              </View>
              <Ionicons
                name="chevron-forward"
                size={18}
                color={theme.colors.textSecondary}
              />
            </Pressable>
          ))}
        </View>

        <AppButton
          title={strings.close}
          onPress={onClose}
          variant="ghost"
          fullWidth
        />
      </View>
    </OverlaySheet>
  );
}

const styles = StyleSheet.create({
  sheet: {
    backgroundColor: theme.colors.surface,
    borderTopLeftRadius: theme.radius.xl,
    borderTopRightRadius: theme.radius.xl,
    maxWidth: theme.layout.sheetMaxWidth,
    alignSelf: 'center',
    width: '100%',
    paddingTop: theme.spacing.lg,
    paddingHorizontal: theme.spacing.lg,
    paddingBottom: theme.layout.sheetPadding,
    borderWidth: 1,
    borderColor: theme.colors.border,
    overflow: 'hidden',
  },
  layout: {
    gap: theme.spacing.md,
  },
  header: {
    gap: 6,
  },
  title: {
    ...textStyles.h2,
    color: theme.colors.textPrimary,
  },
  options: {
    borderRadius: theme.radius.cardRadius,
    borderWidth: 1,
    borderColor: theme.colors.border,
    backgroundColor: theme.colors.card,
    overflow: 'hidden',
  },
  optionRow: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    gap: theme.spacing.sm,
    paddingHorizontal: theme.spacing.sm,
    paddingVertical: theme.spacing.md,
  },
  optionRowBorder: {
    borderTopWidth: 1,
    borderTopColor: theme.colors.border,
  },
  optionRowPressed: {
    opacity: 0.8,
  },
  optionIcon: {
    width: 34,
    height: 34,
    borderRadius: theme.radius.circle,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: iconBackground(theme.colors.feature.meals, 0.1),
    marginTop: 2,
  },
  optionCopy: {
    flex: 1,
    minWidth: 0,
    gap: 4,
  },
  optionTitle: {
    ...textStyles.body,
    color: theme.colors.textPrimary,
    fontWeight: '700',
    lineHeight: 20,
  },
  optionHint: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
    lineHeight: 18,
  },
});
