import { Keyboard, StyleSheet, Text, View } from 'react-native';
import { AppButton, AppChip, AppInput } from '../../../shared/ui/components';
import { textStyles, theme } from '../../../shared/ui/theme';

type Props = {
  amount: string;
  description: string;
  category: string;
  paidByUserId: string | null;
  participantUserIds: string[];
  loading?: boolean;
  error?: string | null;
  onChangeAmount: (value: string) => void;
  onChangeDescription: (value: string) => void;
  onChangeCategory: (value: string) => void;
  onChangePaidByUserId: (value: string) => void;
  onSubmit: () => void;
  onClose: () => void;
};

export function CreateTransactionSheetContent({
  amount,
  description,
  category,
  paidByUserId,
  participantUserIds,
  loading,
  error,
  onChangeAmount,
  onChangeDescription,
  onChangeCategory,
  onChangePaidByUserId,
  onSubmit,
  onClose,
}: Props) {
  const canSubmit = amount.trim().length > 0 && !!paidByUserId && !loading;

  return (
    <View style={styles.root}>
      <View style={styles.handle} />
      <Text style={textStyles.h3}>Add transaction</Text>
      <AppInput
        value={amount}
        onChangeText={onChangeAmount}
        placeholder="Amount"
        keyboardType="decimal-pad"
      />
      <AppInput
        value={description}
        onChangeText={onChangeDescription}
        placeholder="Description (optional)"
      />
      <AppInput
        value={category}
        onChangeText={onChangeCategory}
        placeholder="Category (optional)"
      />
      <Text style={styles.label}>Paid by</Text>
      <View style={styles.participants}>
        {participantUserIds.map((userId) => (
          <AppChip
            key={userId}
            label={userId.slice(0, 8)}
            active={paidByUserId === userId}
            onPress={() => {
              Keyboard.dismiss();
              onChangePaidByUserId(userId);
            }}
          />
        ))}
      </View>
      {error ? <Text style={styles.error}>{error}</Text> : null}
      <View style={styles.actions}>
        <AppButton
          title={loading ? 'Saving...' : 'Save transaction'}
          onPress={onSubmit}
          disabled={!canSubmit}
          fullWidth
        />
        <AppButton title="Cancel" onPress={onClose} variant="ghost" fullWidth />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  root: {
    padding: theme.spacing.lg,
    gap: theme.spacing.sm,
    backgroundColor: theme.colors.surface,
    borderWidth: 1,
    borderColor: theme.colors.border,
  },
  handle: {
    alignSelf: 'center',
    width: 48,
    height: 5,
    borderRadius: 999,
    backgroundColor: theme.colors.borderStrong,
    marginBottom: theme.spacing.sm,
  },
  label: {
    ...textStyles.subtle,
    textTransform: 'uppercase',
    letterSpacing: 0.8,
  },
  participants: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: theme.spacing.sm,
  },
  actions: {
    gap: theme.spacing.sm,
    marginTop: theme.spacing.xs,
  },
  error: {
    color: theme.colors.danger,
    fontFamily: theme.typography.body,
  },
});
