import { StyleSheet, View } from 'react-native';
import { type SettlementTransactionResponse } from '../api/economyApi';
import { SettlementTransactionRow } from './SettlementTransactionRow';
import { AppCard, SectionTitle, Subtle } from '../../../shared/ui/components';
import { theme } from '../../../shared/ui/theme';

type Props = {
  transactions: SettlementTransactionResponse[];
  deletingTransactionId: string | null;
  resolveUserName: (userId: string) => string;
  onDelete: (transactionId: string) => void;
};

export function EconomyTransactionsCard({
  transactions,
  deletingTransactionId,
  resolveUserName,
  onDelete,
}: Props) {
  return (
    <AppCard>
      <SectionTitle>Expenses</SectionTitle>
      <View style={styles.list}>
        {transactions.length === 0 ? (
          <Subtle>No transactions yet.</Subtle>
        ) : (
          transactions.map((item) => (
            <SettlementTransactionRow
              key={item.transactionId}
              item={item}
              deleting={deletingTransactionId === item.transactionId}
              resolveUserName={resolveUserName}
              onDelete={() => onDelete(item.transactionId)}
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
    minHeight: 220,
  },
});

