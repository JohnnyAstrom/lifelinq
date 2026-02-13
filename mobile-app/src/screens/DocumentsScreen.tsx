import { StyleSheet, Text } from 'react-native';
import { AppButton, AppCard, AppScreen, Subtle, TopBar } from '../shared/ui/components';
import { textStyles, theme } from '../shared/ui/theme';

type Props = {
  onDone: () => void;
};

export function DocumentsScreen({ onDone }: Props) {
  const strings = {
    title: 'Documents',
    subtitle: 'Receipts, warranties, and important records.',
    placeholderTitle: 'Coming soon',
    placeholderBody: 'Documents will live here in a future update.',
    back: 'Back',
  };

  return (
    <AppScreen>
      <TopBar
        title={strings.title}
        subtitle={strings.subtitle}
        left={<AppButton title={strings.back} onPress={onDone} variant="ghost" />}
      />

      <AppCard style={styles.contentOffset}>
        <Text style={textStyles.h3}>{strings.placeholderTitle}</Text>
        <Subtle>{strings.placeholderBody}</Subtle>
      </AppCard>
    </AppScreen>
  );
}

const styles = StyleSheet.create({
  contentOffset: {
    marginTop: 90,
    gap: theme.spacing.sm,
  },
});
