import { StyleSheet, Text, View } from 'react-native';
import { textStyles, theme } from '../../../shared/ui/theme';

type Props = {
  userId: string;
  amount: number;
};

function formatAmount(amount: number): string {
  const rounded = Number.isFinite(amount) ? amount : 0;
  return rounded.toFixed(2);
}

export function SettlementBalanceRow({ userId, amount }: Props) {
  const positive = amount > 0;
  const color = positive ? theme.colors.danger : theme.colors.success;

  return (
    <View style={styles.row}>
      <Text style={styles.user}>{userId.slice(0, 8)}</Text>
      <Text style={[styles.amount, { color }]}>
        {positive ? '+' : ''}{formatAmount(amount)}
      </Text>
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
    justifyContent: 'space-between',
    gap: theme.spacing.sm,
  },
  user: {
    ...textStyles.body,
    flex: 1,
  },
  amount: {
    ...textStyles.body,
    fontWeight: '700',
  },
});
