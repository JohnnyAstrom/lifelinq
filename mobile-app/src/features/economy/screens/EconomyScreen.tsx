import { useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { CreateTransactionSheetContent } from '../components/CreateTransactionSheetContent';
import { RecommendedPaymentRow } from '../components/RecommendedPaymentRow';
import { SettlementBalanceRow } from '../components/SettlementBalanceRow';
import { SettlementPeriodHeader } from '../components/SettlementPeriodHeader';
import { SettlementTransactionRow } from '../components/SettlementTransactionRow';
import { useEconomyWorkflow } from '../hooks/useEconomyWorkflow';
import { useAppBackHandler } from '../../../shared/hooks/useAppBackHandler';
import { OverlaySheet } from '../../../shared/ui/OverlaySheet';
import { AppButton, AppCard, AppScreen, BackIconButton, SectionTitle, Subtle, TopBar } from '../../../shared/ui/components';
import { theme } from '../../../shared/ui/theme';

type Props = {
  token: string;
  onDone: () => void;
};

export function EconomyScreen({ token, onDone }: Props) {
  const workflow = useEconomyWorkflow(token);
  const { state, actions } = workflow;

  const [showCreateSheet, setShowCreateSheet] = useState(false);
  const [amountInput, setAmountInput] = useState('');
  const [descriptionInput, setDescriptionInput] = useState('');
  const [categoryInput, setCategoryInput] = useState('');
  const [paidByUserId, setPaidByUserId] = useState<string | null>(null);
  const [deletingTransactionId, setDeletingTransactionId] = useState<string | null>(null);

  const participants = state.period?.participantUserIds ?? [];

  useAppBackHandler({
    canGoBack: true,
    onGoBack: onDone,
    isOverlayOpen: showCreateSheet,
    onCloseOverlay: () => setShowCreateSheet(false),
  });

  function openCreateSheet() {
    const defaultPayer = participants[0] ?? null;
    setAmountInput('');
    setDescriptionInput('');
    setCategoryInput('');
    setPaidByUserId(defaultPayer);
    setShowCreateSheet(true);
  }

  async function handleCreateTransaction() {
    if (!paidByUserId) {
      return;
    }
    const normalizedAmount = Number(amountInput.replace(',', '.'));
    if (!Number.isFinite(normalizedAmount) || normalizedAmount <= 0) {
      return;
    }
    const created = await actions.addTransaction({
      amount: normalizedAmount,
      description: descriptionInput.trim() || null,
      category: categoryInput.trim() || null,
      paidByUserId,
    });
    if (created) {
      setShowCreateSheet(false);
    }
  }

  async function handleDeleteTransaction(transactionId: string) {
    if (deletingTransactionId) {
      return;
    }
    setDeletingTransactionId(transactionId);
    try {
      await actions.deleteTransaction(transactionId);
    } finally {
      setDeletingTransactionId(null);
    }
  }

  return (
    <AppScreen>
      <TopBar
        title="Economy"
        subtitle="Settlement and transactions"
        right={<BackIconButton onPress={onDone} />}
      />

      <View style={styles.contentOffset}>
        {state.loading ? <Subtle>Loading economy data...</Subtle> : null}
        {state.error ? <Text style={styles.error}>{state.error}</Text> : null}

        <AppCard>
          <SectionTitle>Period</SectionTitle>
          {state.period ? (
            <>
              <SettlementPeriodHeader period={state.period} />
              <AppButton title="Close current period" onPress={() => { void actions.closePeriod(); }} variant="ghost" fullWidth />
            </>
          ) : (
            <Subtle>No active period.</Subtle>
          )}
        </AppCard>

        <AppCard>
          <SectionTitle>Settlement</SectionTitle>
          <Subtle>Balances</Subtle>
          <View style={styles.list}>
            {state.balances.length === 0 ? (
              <Subtle>No balances yet.</Subtle>
            ) : (
              state.balances.map((item) => (
                <SettlementBalanceRow key={item.userId} userId={item.userId} amount={item.amount} />
              ))
            )}
          </View>

          <Subtle style={styles.subsection}>Recommended payments</Subtle>
          <View style={styles.list}>
            {state.recommendedPayments.length === 0 ? (
              <Subtle>No payments needed.</Subtle>
            ) : (
              state.recommendedPayments.map((item, index) => (
                <RecommendedPaymentRow
                  key={`${item.fromUserId}-${item.toUserId}-${index}`}
                  fromUserId={item.fromUserId}
                  toUserId={item.toUserId}
                  amount={item.amount}
                />
              ))
            )}
          </View>
        </AppCard>

        <AppCard>
          <SectionTitle>Transactions</SectionTitle>
          <View style={styles.list}>
            {state.transactions.length === 0 ? (
              <Subtle>No transactions yet.</Subtle>
            ) : (
              state.transactions.map((item) => (
                <SettlementTransactionRow
                  key={item.transactionId}
                  item={item}
                  deleting={deletingTransactionId === item.transactionId}
                  onDelete={() => {
                    void handleDeleteTransaction(item.transactionId);
                  }}
                />
              ))
            )}
          </View>
          <AppButton title="Add transaction" onPress={openCreateSheet} fullWidth />
        </AppCard>
      </View>

      {showCreateSheet ? (
        <OverlaySheet onClose={() => setShowCreateSheet(false)}>
          <CreateTransactionSheetContent
            amount={amountInput}
            description={descriptionInput}
            category={categoryInput}
            paidByUserId={paidByUserId}
            participantUserIds={participants}
            loading={state.mutating}
            error={state.mutationError}
            onChangeAmount={setAmountInput}
            onChangeDescription={setDescriptionInput}
            onChangeCategory={setCategoryInput}
            onChangePaidByUserId={setPaidByUserId}
            onSubmit={() => {
              void handleCreateTransaction();
            }}
            onClose={() => setShowCreateSheet(false)}
          />
        </OverlaySheet>
      ) : null}
    </AppScreen>
  );
}

const styles = StyleSheet.create({
  contentOffset: {
    paddingTop: 90,
    gap: theme.spacing.md,
  },
  list: {
    marginTop: theme.spacing.xs,
    gap: theme.spacing.sm,
  },
  subsection: {
    marginTop: theme.spacing.sm,
  },
  error: {
    color: theme.colors.danger,
    fontFamily: theme.typography.body,
  },
});
