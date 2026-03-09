import { StyleSheet, Text, View } from 'react-native';
import { type ActiveSettlementPeriodResponse } from '../api/economyApi';
import { Subtle } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';

type Props = {
  period: ActiveSettlementPeriodResponse;
  showPreviousPeriodClosed?: boolean;
  resolveUserName?: (userId: string) => string;
};

function formatDate(value: string | null): string {
  if (!value) {
    return 'Now';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return date.toLocaleDateString();
}

export function SettlementPeriodHeader({
  period,
  showPreviousPeriodClosed = false,
  resolveUserName,
}: Props) {
  function displayUser(userId: string): string {
    const resolved = resolveUserName ? resolveUserName(userId) : userId;
    return resolved === userId ? userId.slice(0, 8) : resolved;
  }

  return (
    <View style={styles.root}>
      <Subtle>
        {formatDate(period.startDate)} - {period.endDate ? formatDate(period.endDate) : 'Open'}
      </Subtle>
      {showPreviousPeriodClosed ? <Subtle>Previous period closed</Subtle> : null}
      <Subtle>{`Strategy: ${period.strategyType}`}</Subtle>
      <Subtle>{`${period.participantUserIds.length} participants`}</Subtle>
      <View style={styles.participants}>
        {period.participantUserIds.map((userId) => (
          <View key={userId} style={styles.participantChip}>
            <Text style={styles.participantText}>{displayUser(userId)}</Text>
          </View>
        ))}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  root: {
    gap: theme.spacing.xs,
  },
  participants: {
    marginTop: theme.spacing.xs,
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: theme.spacing.xs,
  },
  participantChip: {
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: 999,
    paddingVertical: 4,
    paddingHorizontal: 8,
    backgroundColor: theme.colors.surfaceAlt,
  },
  participantText: {
    ...textStyles.subtle,
    color: theme.colors.text,
  },
});
