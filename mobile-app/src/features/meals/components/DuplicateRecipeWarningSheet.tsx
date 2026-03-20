import { StyleSheet, Text, View } from 'react-native';
import { OverlaySheet } from '../../../shared/ui/OverlaySheet';
import { AppButton, Subtle } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';

type Strings = {
  title: string;
  archivedHint?: string;
  primaryAction: string;
  secondaryAction: string;
  cancelAction: string;
};

type Props = {
  visible: boolean;
  body: string;
  recipeName: string;
  archivedHint?: string;
  onPrimaryAction: () => void;
  onSecondaryAction: () => void;
  onClose: () => void;
  strings: Strings;
};

export function DuplicateRecipeWarningSheet({
  visible,
  body,
  recipeName,
  archivedHint,
  onPrimaryAction,
  onSecondaryAction,
  onClose,
  strings,
}: Props) {
  if (!visible) {
    return null;
  }

  return (
    <OverlaySheet onClose={onClose} sheetStyle={styles.sheet}>
      <View style={styles.layout}>
        <View style={styles.header}>
          <Text style={textStyles.h2}>{strings.title}</Text>
          <Subtle>{body}</Subtle>
        </View>

        <View style={styles.recipeBlock}>
          <Text style={styles.recipeName}>{recipeName}</Text>
          {archivedHint ? <Text style={styles.archivedHint}>{archivedHint}</Text> : null}
        </View>

        <View style={styles.actions}>
          <AppButton
            title={strings.primaryAction}
            onPress={onPrimaryAction}
            fullWidth
            accentKey="meals"
          />
          <AppButton
            title={strings.secondaryAction}
            onPress={onSecondaryAction}
            variant="secondary"
            fullWidth
          />
          <AppButton
            title={strings.cancelAction}
            onPress={onClose}
            variant="ghost"
            fullWidth
          />
        </View>
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
    gap: theme.spacing.xs,
  },
  recipeBlock: {
    gap: 4,
    paddingVertical: theme.spacing.sm,
    paddingHorizontal: theme.spacing.sm,
    borderRadius: theme.radius.lg,
    backgroundColor: theme.colors.surfaceSubtle,
    borderWidth: 1,
    borderColor: theme.colors.border,
  },
  recipeName: {
    ...textStyles.body,
    fontWeight: '600',
    color: theme.colors.text,
  },
  archivedHint: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
  },
  actions: {
    gap: theme.spacing.sm,
  },
});
