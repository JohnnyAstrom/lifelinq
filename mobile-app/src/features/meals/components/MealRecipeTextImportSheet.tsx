import { useState } from 'react';
import { ScrollView, StyleSheet, Text, View } from 'react-native';
import { OverlaySheet } from '../../../shared/ui/OverlaySheet';
import { AppButton, AppInput, Subtle } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';

type Strings = {
  title: string;
  subtitle: string;
  textPlaceholder: string;
  helpText: string;
  importAction: string;
  importingAction: string;
  close: string;
};

type Props = {
  importText: string;
  onChangeImportText: (value: string) => void;
  onImport: () => void;
  onClose: () => void;
  isImporting: boolean;
  error: string | null;
  strings: Strings;
};

export function MealRecipeTextImportSheet({
  importText,
  onChangeImportText,
  onImport,
  onClose,
  isImporting,
  error,
  strings,
}: Props) {
  const [inputHeight, setInputHeight] = useState(180);
  const resolvedInputHeight = Math.min(320, Math.max(180, inputHeight));

  return (
    <OverlaySheet onClose={onClose} sheetStyle={styles.sheet}>
      <View style={styles.layout}>
        <View style={styles.header}>
          <Text style={styles.title}>{strings.title}</Text>
          <Subtle>{strings.subtitle}</Subtle>
        </View>

        <ScrollView
          style={styles.bodyScroll}
          contentContainerStyle={styles.body}
          keyboardShouldPersistTaps="handled"
          keyboardDismissMode="interactive"
        >
          <AppInput
            value={importText}
            onChangeText={onChangeImportText}
            placeholder={strings.textPlaceholder}
            multiline
            blurOnSubmit={false}
            scrollEnabled
            onContentSizeChange={(event) => {
              const nextHeight = Math.ceil(event.nativeEvent.contentSize.height) + 12;
              setInputHeight((currentHeight) =>
                currentHeight === nextHeight ? currentHeight : nextHeight,
              );
            }}
            style={[styles.captureInput, { height: resolvedInputHeight }]}
          />
          <Subtle>{strings.helpText}</Subtle>

          {error ? <Text style={styles.error}>{error}</Text> : null}

          <View style={styles.actions}>
            <AppButton
              title={isImporting ? strings.importingAction : strings.importAction}
              onPress={onImport}
              fullWidth
              accentKey="meals"
              disabled={isImporting || importText.trim().length === 0}
            />
            <AppButton
              title={strings.close}
              onPress={onClose}
              variant="ghost"
              fullWidth
              disabled={isImporting}
            />
          </View>
        </ScrollView>
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
    maxHeight: '100%',
    minHeight: 0,
  },
  header: {
    gap: 6,
  },
  title: {
    ...textStyles.h2,
    color: theme.colors.textPrimary,
  },
  bodyScroll: {
    minHeight: 0,
    maxHeight: '100%',
  },
  body: {
    gap: theme.spacing.sm,
    paddingBottom: theme.spacing.xs,
  },
  captureInput: {
    fontSize: 15,
    lineHeight: 20,
    fontWeight: '500',
    paddingVertical: theme.spacing.sm,
    backgroundColor: theme.colors.surface,
    color: theme.colors.textPrimary,
    textAlignVertical: 'top',
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
