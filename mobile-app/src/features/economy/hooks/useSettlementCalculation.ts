import { useEffect, useState } from 'react';
import { formatApiError } from '../../../shared/api/client';
import { useAuth } from '../../../shared/auth/AuthContext';
import {
  calculateSettlement,
  type CalculateSettlementResponse,
} from '../api/economyApi';

export function useSettlementCalculation(
  token: string | null,
  periodId: string | null
) {
  const { handleApiError } = useAuth();
  const [settlement, setSettlement] = useState<CalculateSettlementResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function load(nextPeriodId?: string | null): Promise<void> {
    const effectivePeriodId = nextPeriodId ?? periodId;
    if (!token || !effectivePeriodId) {
      setSettlement(null);
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const response = await calculateSettlement(token, effectivePeriodId);
      setSettlement(response);
    } catch (err) {
      await handleApiError(err);
      setError(formatApiError(err));
      setSettlement(null);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void load(periodId);
  }, [token, periodId]);

  return {
    settlement,
    loading,
    error,
    load,
    reload: () => load(periodId),
  };
}
