import { useMemo, useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { CreateTransactionSheetContent } from '../components/CreateTransactionSheetContent';
import { EconomyRoundCard } from '../components/EconomyRoundCard';
import { EconomySettlementCard } from '../components/EconomySettlementCard';
import { EconomySummaryCard } from '../components/EconomySummaryCard';
import { EconomyTransactionsCard } from '../components/EconomyTransactionsCard';
import { buildParticipantNameMap, resolveParticipantDisplayName } from '../utils/participantNameMap';
import { useGroupMembers } from '../../group/hooks/useGroupMembers';
import { useEconomyWorkflow } from '../hooks/useEconomyWorkflow';
import { useAppBackHandler } from '../../../shared/hooks/useAppBackHandler';
import { OverlaySheet } from '../../../shared/ui/OverlaySheet';
import { AppButton, AppScreen, BackIconButton, Subtle, TopBar } from '../../../shared/ui/components';
import { theme } from '../../../shared/ui/theme';

type Props = {
  token: string;
  onDone: () => void;
  onShowToast: (message: string) => void;
};

export function EconomyScreen({ token, onDone, onShowToast }: Props) {
  const workflow = useEconomyWorkflow(token);
  const { state, actions } = workflow;
  const members = useGroupMembers(token);

  const [showCreateSheet, setShowCreateSheet] = useState(false);
  const [amountInput, setAmountInput] = useState('');
  const [descriptionInput, setDescriptionInput] = useState('');
  const [categoryInput, setCategoryInput] = useState('');
  const [paidByUserId, setPaidByUserId] = useState<string | null>(null);
  const [deletingTransactionId, setDeletingTransactionId] = useState<string | null>(null);
  const [showPreviousPeriodClosed, setShowPreviousPeriodClosed] = useState(false);

  const participants = state.period?.participantUserIds ?? [];
  const participantNameMap = useMemo(() => buildParticipantNameMap(members.items), [members.items]);
  const resolveUserName = (userId: string) =>
    resolveParticipantDisplayName(participantNameMap, userId);

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

  async function handleClosePeriod() {
    const closed = await actions.closePeriod();
    if (!closed) {
      return;
    }
    setShowPreviousPeriodClosed(true);
    onShowToast('Settlement round closed. A new round has started.');
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

        <EconomySummaryCard
          balances={state.balances}
          recommendedPayments={state.recommendedPayments}
          resolveUserName={resolveUserName}
        />

        <EconomyTransactionsCard
          transactions={state.transactions}
          deletingTransactionId={deletingTransactionId}
          resolveUserName={resolveUserName}
          onDelete={(transactionId) => {
            void handleDeleteTransaction(transactionId);
          }}
        />

        <AppButton title="+ Add expense" onPress={openCreateSheet} fullWidth />

        <EconomySettlementCard
          balances={state.balances}
          recommendedPayments={state.recommendedPayments}
          resolveUserName={resolveUserName}
        />

        <EconomyRoundCard
          period={state.period}
          showPreviousPeriodClosed={showPreviousPeriodClosed}
          resolveUserName={resolveUserName}
          onCloseRound={() => {
            void handleClosePeriod();
          }}
        />
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
    paddingTop: theme.layout.topBarOffset + theme.spacing.md,
    gap: theme.spacing.md,
  },
  error: {
    color: theme.colors.danger,
    fontFamily: theme.typography.body,
  },
});

