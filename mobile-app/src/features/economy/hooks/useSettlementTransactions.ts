import { useEffect, useState } from 'react';
import { formatApiError } from '../../../shared/api/client';
import { useAuth } from '../../../shared/auth/AuthContext';
import {
  listSettlementTransactions,
  type SettlementTransactionResponse,
} from '../api/economyApi';

export function useSettlementTransactions(
  token: string | null,
  periodId: string | null
) {
  const { handleApiError } = useAuth();
  const [items, setItems] = useState<SettlementTransactionResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function load(nextPeriodId?: string | null): Promise<void> {
    const effectivePeriodId = nextPeriodId ?? periodId;
    if (!token || !effectivePeriodId) {
      setItems([]);
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const response = await listSettlementTransactions(token, effectivePeriodId);
      setItems(response);
    } catch (err) {
      await handleApiError(err);
      setError(formatApiError(err));
      setItems([]);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void load(periodId);
  }, [token, periodId]);

  return {
    items,
    loading,
    error,
    load,
    reload: () => load(periodId),
  };
}
