import { StyleSheet, Text, View } from 'react-native';
import { textStyles, theme } from '../../../shared/ui/theme';

type Props = {
  fromUserId: string;
  toUserId: string;
  amount: number;
};

function formatAmount(amount: number): string {
  const rounded = Number.isFinite(amount) ? amount : 0;
  return rounded.toFixed(2);
}

export function RecommendedPaymentRow({ fromUserId, toUserId, amount }: Props) {
  return (
    <View style={styles.row}>
      <Text style={styles.users}>
        {fromUserId.slice(0, 8)} {'->'} {toUserId.slice(0, 8)}
      </Text>
      <Text style={styles.amount}>{formatAmount(amount)}</Text>
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
  users: {
    ...textStyles.body,
    flex: 1,
  },
  amount: {
    ...textStyles.body,
    fontWeight: '700',
  },
});
