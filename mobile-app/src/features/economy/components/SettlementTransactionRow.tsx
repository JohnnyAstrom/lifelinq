import { StyleSheet, Text, View } from 'react-native';
import { AppButton, Subtle } from '../../../shared/ui/components';
import { type SettlementTransactionResponse } from '../api/economyApi';
import { textStyles, theme } from '../../../shared/ui/theme';

type Props = {
  item: SettlementTransactionResponse;
  deleting?: boolean;
  onDelete: () => void;
  resolveUserName?: (userId: string) => string;
};

function formatAmount(amount: number): string {
  const rounded = Number.isFinite(amount) ? amount : 0;
  return rounded.toFixed(2);
}

export function SettlementTransactionRow({
  item,
  deleting,
  onDelete,
  resolveUserName,
}: Props) {
  const resolved = resolveUserName ? resolveUserName(item.paidByUserId) : item.paidByUserId;
  const payerLabel = resolved === item.paidByUserId ? item.paidByUserId.slice(0, 8) : resolved;

  return (
    <View style={styles.row}>
      <View style={styles.content}>
        <Text style={styles.title}>{item.description?.trim() || 'Transaction'}</Text>
        <Subtle>
          Paid by {payerLabel}
          {item.category ? ` · ${item.category}` : ''}
        </Subtle>
        <Subtle>{new Date(item.createdAt).toLocaleDateString()}</Subtle>
      </View>
      <View style={styles.actions}>
        <Text style={styles.amount}>{formatAmount(item.amount)}</Text>
        <AppButton
          title={deleting ? 'Removing...' : 'Remove'}
          onPress={onDelete}
          variant="ghost"
          disabled={deleting}
        />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  row: {
    borderWidth: 1,
    borderColor: theme.colors.border,
    borderRadius: theme.radius.md,
    paddingVertical: theme.spacing.sm,
    paddingHorizontal: theme.spacing.md,
    backgroundColor: theme.colors.surfaceAlt,
    flexDirection: 'row',
    alignItems: 'center',
    gap: theme.spacing.sm,
  },
  content: {
    flex: 1,
    gap: 2,
  },
  title: {
    ...textStyles.body,
    fontWeight: '600',
  },
  actions: {
    minWidth: 110,
    alignItems: 'flex-end',
    gap: theme.spacing.xs,
  },
  amount: {
    ...textStyles.body,
    fontWeight: '700',
  },
});
