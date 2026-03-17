import { StyleSheet, Text, View } from 'react-native';
import { OverlaySheet } from '../../../shared/ui/OverlaySheet';
import { AppButton, AppInput, Subtle } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';

type Strings = {
  title: string;
  subtitle: string;
  urlLabel: string;
  urlPlaceholder: string;
  importAction: string;
  importingAction: string;
  close: string;
};

type Props = {
  importUrl: string;
  onChangeImportUrl: (value: string) => void;
  onImport: () => void;
  onClose: () => void;
  isImporting: boolean;
  error: string | null;
  strings: Strings;
};

export function MealRecipeImportSheet({
  importUrl,
  onChangeImportUrl,
  onImport,
  onClose,
  isImporting,
  error,
  strings,
}: Props) {
  return (
    <OverlaySheet onClose={onClose} sheetStyle={styles.sheet}>
      <View style={styles.layout}>
        <View style={styles.header}>
          <Text style={textStyles.h2}>{strings.title}</Text>
          <Subtle>{strings.subtitle}</Subtle>
        </View>

        <View style={styles.body}>
          <Text style={styles.fieldLabel}>{strings.urlLabel}</Text>
          <AppInput
            value={importUrl}
            onChangeText={onChangeImportUrl}
            placeholder={strings.urlPlaceholder}
            keyboardType="url"
          />

          {error ? <Text style={styles.error}>{error}</Text> : null}

          <View style={styles.actions}>
            <AppButton
              title={isImporting ? strings.importingAction : strings.importAction}
              onPress={onImport}
              fullWidth
              accentKey="meals"
              disabled={isImporting || importUrl.trim().length === 0}
            />
            <AppButton
              title={strings.close}
              onPress={onClose}
              variant="ghost"
              fullWidth
              disabled={isImporting}
            />
          </View>
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
    paddingBottom: theme.spacing.sm,
    borderBottomWidth: 1,
    borderBottomColor: theme.colors.border,
  },
  body: {
    gap: theme.spacing.sm,
  },
  fieldLabel: {
    ...textStyles.subtle,
    color: theme.colors.textSecondary,
    fontWeight: '600',
  },
  actions: {
    gap: theme.spacing.sm,
    paddingTop: theme.spacing.xs,
  },
  error: {
    ...textStyles.body,
    color: theme.colors.danger,
  },
});
