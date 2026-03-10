import { StyleSheet, Text, View } from 'react-native';
import { AppCard, Subtle } from '../../../shared/ui/components';
import { textStyles } from '../../../shared/ui/theme';

type Props = {
  title: string;
  subtitle: string;
  itemCountLabel: string;
  preview: string[];
  emptyTitle: string;
  emptyBody: string;
};

export function TodaySummaryCard({
  title,
  subtitle,
  itemCountLabel,
  preview,
  emptyTitle,
  emptyBody,
}: Props) {
  const summaryText = preview.length > 0
    ? itemCountLabel
    : `${emptyTitle}. ${emptyBody}`;

  return (
    <AppCard style={styles.card}>
      <View style={styles.content}>
        <Text style={textStyles.h2}>{title}</Text>
        <Subtle>{subtitle}</Subtle>
      </View>
      <Text style={styles.summary}>{summaryText}</Text>
    </AppCard>
  );
}

const styles = StyleSheet.create({
  card: {
    padding: 24,
    gap: 16,
  },
  content: {
    gap: 8,
  },
  summary: {
    ...textStyles.body,
  },
});
