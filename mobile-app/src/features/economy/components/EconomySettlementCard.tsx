import { StyleSheet, View } from 'react-native';
import { type CalculateSettlementResponse } from '../api/economyApi';
import { RecommendedPaymentRow } from './RecommendedPaymentRow';
import { SettlementBalanceRow } from './SettlementBalanceRow';
import { AppCard, SectionTitle, Subtle } from '../../../shared/ui/components';
import { theme } from '../../../shared/ui/theme';

type ParticipantBalance = CalculateSettlementResponse['balances'][number];
type RecommendedPayment = CalculateSettlementResponse['recommendedPayments'][number];

type Props = {
  balances: ParticipantBalance[];
  recommendedPayments: RecommendedPayment[];
  resolveUserName: (userId: string) => string;
};

export function EconomySettlementCard({
  balances,
  recommendedPayments,
  resolveUserName,
}: Props) {
  return (
    <AppCard>
      <SectionTitle>Who owes who</SectionTitle>
      <View style={styles.list}>
        {recommendedPayments.length === 0 ? (
          <Subtle>No payments needed.</Subtle>
        ) : (
          recommendedPayments.map((item, index) => (
            <RecommendedPaymentRow
              key={`${item.fromUserId}-${item.toUserId}-${index}`}
              fromUserId={item.fromUserId}
              toUserId={item.toUserId}
              amount={item.amount}
              resolveUserName={resolveUserName}
            />
          ))
        )}
      </View>

      <Subtle style={styles.subsection}>Balances</Subtle>
      <View style={styles.list}>
        {balances.length === 0 ? (
          <Subtle>No balances yet.</Subtle>
        ) : (
          balances.map((item) => (
            <SettlementBalanceRow
              key={item.userId}
              userId={item.userId}
              amount={item.amount}
              resolveUserName={resolveUserName}
            />
          ))
        )}
      </View>
    </AppCard>
  );
}

const styles = StyleSheet.create({
  list: {
    marginTop: theme.spacing.xs,
    gap: theme.spacing.sm,
  },
  subsection: {
    marginTop: theme.spacing.sm,
  },
});
