import { useMemo, useState } from 'react';
import { formatApiError } from '../../../shared/api/client';
import { useAuth } from '../../../shared/auth/AuthContext';
import {
  closeSettlementPeriod,
  createSettlementTransaction,
  deleteSettlementTransaction,
  updateSettlementStrategy,
  type CreateSettlementTransactionRequest,
  type SettlementStrategyType,
  type UpdateSettlementStrategyRequest,
} from '../api/economyApi';
import { useActiveSettlementPeriod } from './useActiveSettlementPeriod';
import { useSettlementCalculation } from './useSettlementCalculation';
import { useSettlementTransactions } from './useSettlementTransactions';

export function useEconomyWorkflow(token: string | null) {
  const { handleApiError } = useAuth();
  const periodState = useActiveSettlementPeriod(token);
  const periodId = periodState.period?.periodId ?? null;
  const transactionsState = useSettlementTransactions(token, periodId);
  const settlementState = useSettlementCalculation(token, periodId);

  const [mutating, setMutating] = useState(false);
  const [mutationError, setMutationError] = useState<string | null>(null);

  const loading =
    periodState.loading ||
    transactionsState.loading ||
    settlementState.loading ||
    mutating;
  const error =
    mutationError ??
    periodState.error ??
    transactionsState.error ??
    settlementState.error;

  const balances = settlementState.settlement?.balances ?? [];
  const recommendedPayments = settlementState.settlement?.recommendedPayments ?? [];

  async function addTransaction(request: CreateSettlementTransactionRequest): Promise<boolean> {
    if (!token) {
      throw new Error('Missing token');
    }
    if (!periodId) {
      setMutationError('No active settlement period.');
      return false;
    }
    setMutating(true);
    setMutationError(null);
    try {
      await createSettlementTransaction(token, periodId, request);
      await transactionsState.reload();
      await settlementState.reload();
      return true;
    } catch (err) {
      await handleApiError(err);
      setMutationError(formatApiError(err));
      return false;
    } finally {
      setMutating(false);
    }
  }

  async function deleteTransaction(transactionId: string): Promise<boolean> {
    if (!token) {
      throw new Error('Missing token');
    }
    if (!periodId) {
      setMutationError('No active settlement period.');
      return false;
    }
    setMutating(true);
    setMutationError(null);
    try {
      await deleteSettlementTransaction(token, transactionId);
      await transactionsState.reload();
      await settlementState.reload();
      return true;
    } catch (err) {
      await handleApiError(err);
      setMutationError(formatApiError(err));
      return false;
    } finally {
      setMutating(false);
    }
  }

  async function closePeriod(): Promise<boolean> {
    if (!token) {
      throw new Error('Missing token');
    }
    setMutating(true);
    setMutationError(null);
    try {
      const result = await closeSettlementPeriod(token);
      await periodState.reload();
      await transactionsState.load(result.newOpenPeriodId);
      await settlementState.load(result.newOpenPeriodId);
      return true;
    } catch (err) {
      await handleApiError(err);
      setMutationError(formatApiError(err));
      return false;
    } finally {
      setMutating(false);
    }
  }

  async function updateStrategy(
    strategyType: SettlementStrategyType,
    percentageShares?: Record<string, number> | null
  ): Promise<boolean> {
    if (!token) {
      throw new Error('Missing token');
    }
    if (!periodId) {
      setMutationError('No active settlement period.');
      return false;
    }
    setMutating(true);
    setMutationError(null);
    try {
      const request: UpdateSettlementStrategyRequest = {
        strategyType,
        percentageShares: percentageShares ?? null,
      };
      await updateSettlementStrategy(token, periodId, request);
      await settlementState.reload();
      return true;
    } catch (err) {
      await handleApiError(err);
      setMutationError(formatApiError(err));
      return false;
    } finally {
      setMutating(false);
    }
  }

  async function reloadAll(): Promise<void> {
    await periodState.reload();
    const refreshedPeriodId = periodState.period?.periodId ?? periodId;
    if (refreshedPeriodId) {
      await Promise.all([
        transactionsState.load(refreshedPeriodId),
        settlementState.load(refreshedPeriodId),
      ]);
      return;
    }
    await Promise.all([transactionsState.reload(), settlementState.reload()]);
  }

  const state = useMemo(
    () => ({
      period: periodState.period,
      transactions: transactionsState.items,
      settlement: settlementState.settlement,
      balances,
      recommendedPayments,
      loading,
      mutating,
      error,
      periodError: periodState.error,
      transactionsError: transactionsState.error,
      settlementError: settlementState.error,
      mutationError,
    }),
    [
      periodState.period,
      transactionsState.items,
      settlementState.settlement,
      balances,
      recommendedPayments,
      loading,
      mutating,
      error,
      periodState.error,
      transactionsState.error,
      settlementState.error,
      mutationError,
    ]
  );

  return {
    state,
    actions: {
      addTransaction,
      deleteTransaction,
      closePeriod,
      updateStrategy,
      reloadAll,
      reloadPeriod: periodState.reload,
      reloadTransactions: transactionsState.reload,
      reloadSettlement: settlementState.reload,
    },
  };
}
