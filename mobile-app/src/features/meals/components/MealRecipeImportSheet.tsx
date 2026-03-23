import { useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { OverlaySheet } from '../../../shared/ui/OverlaySheet';
import { AppButton, AppInput, Subtle } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';

type Strings = {
  title: string;
  subtitle: string;
  urlPlaceholder: string;
  helpText: string;
  clipboardHint?: string;
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
  clipboardImportUrl?: string | null;
  strings: Strings;
};

export function MealRecipeImportSheet({
  importUrl,
  onChangeImportUrl,
  onImport,
  onClose,
  isImporting,
  error,
  clipboardImportUrl,
  strings,
}: Props) {
  const [isUrlFocused, setIsUrlFocused] = useState(false);
  const [inputHeight, setInputHeight] = useState(56);

  return (
    <OverlaySheet onClose={onClose} sheetStyle={styles.sheet}>
      <View style={styles.layout}>
        <View style={styles.header}>
          <Text style={textStyles.h2}>{strings.title}</Text>
          <Subtle>{strings.subtitle}</Subtle>
        </View>

        <View style={styles.body}>
          {clipboardImportUrl && strings.clipboardHint ? (
            <View style={styles.clipboardCallout}>
              <Text style={styles.clipboardCalloutText}>{strings.clipboardHint}</Text>
            </View>
          ) : null}
          <AppInput
            value={importUrl}
            onChangeText={onChangeImportUrl}
            placeholder={strings.urlPlaceholder}
            keyboardType="url"
            multiline
            blurOnSubmit={false}
            onFocus={() => setIsUrlFocused(true)}
            onBlur={() => setIsUrlFocused(false)}
            onContentSizeChange={(event) => {
              const nextHeight = Math.max(48, Math.ceil(event.nativeEvent.contentSize.height) + 12);
              setInputHeight((currentHeight) =>
                currentHeight === nextHeight ? currentHeight : nextHeight,
              );
            }}
            selection={isUrlFocused ? undefined : { start: 0, end: 0 }}
            style={[styles.captureInput, { height: inputHeight }]}
          />
          <Subtle>{strings.helpText}</Subtle>

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
  },
  body: {
    gap: theme.spacing.sm,
  },
  clipboardCallout: {
    alignSelf: 'flex-start',
    borderRadius: theme.radius.pill,
    paddingHorizontal: theme.spacing.sm,
    paddingVertical: 6,
    backgroundColor: theme.colors.surfaceSubtle,
    borderWidth: 1,
    borderColor: theme.colors.border,
  },
  clipboardCalloutText: {
    ...textStyles.subtle,
    color: theme.colors.textPrimary,
    fontWeight: '600',
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
