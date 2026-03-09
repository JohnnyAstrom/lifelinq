import { StyleSheet, Text } from 'react-native';
import { type CalculateSettlementResponse } from '../api/economyApi';
import { AppCard, SectionTitle, Subtle } from '../../../shared/ui/components';
import { textStyles } from '../../../shared/ui/theme';

type ParticipantBalance = CalculateSettlementResponse['balances'][number];
type RecommendedPayment = CalculateSettlementResponse['recommendedPayments'][number];

type Props = {
  balances: ParticipantBalance[];
  recommendedPayments: RecommendedPayment[];
  resolveUserName: (userId: string) => string;
};

function formatAmount(amount: number): string {
  const rounded = Number.isFinite(amount) ? amount : 0;
  return rounded.toFixed(2);
}

function isSettled(balances: ParticipantBalance[]): boolean {
  return balances.every((item) => Math.abs(item.amount) < 0.01);
}

export function EconomySummaryCard({ balances, recommendedPayments, resolveUserName }: Props) {
  let summary: string;
  if (recommendedPayments.length > 0) {
    const firstPayment = recommendedPayments[0];
    summary = `${resolveUserName(firstPayment.fromUserId)} owes ${resolveUserName(firstPayment.toUserId)} ${formatAmount(firstPayment.amount)} kr`;
  } else if (balances.length === 0 || isSettled(balances)) {
    summary = 'Everyone is settled';
  } else {
    summary = 'Settlement updated';
  }

  return (
    <AppCard>
      <SectionTitle>Summary</SectionTitle>
      <Text style={styles.summary}>{summary}</Text>
      <Subtle>
        {recommendedPayments.length > 0
          ? `${recommendedPayments.length} recommended payment${recommendedPayments.length === 1 ? '' : 's'}`
          : 'No payments needed right now'}
      </Subtle>
    </AppCard>
  );
}

const styles = StyleSheet.create({
  summary: {
    ...textStyles.h3,
  },
});
