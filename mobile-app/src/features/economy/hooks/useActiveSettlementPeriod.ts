import { useEffect, useState } from 'react';
import { formatApiError } from '../../../shared/api/client';
import { useAuth } from '../../../shared/auth/AuthContext';
import {
  getActiveSettlementPeriod,
  type ActiveSettlementPeriodResponse,
} from '../api/economyApi';

export function useActiveSettlementPeriod(token: string | null) {
  const { handleApiError } = useAuth();
  const [period, setPeriod] = useState<ActiveSettlementPeriodResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function load(): Promise<void> {
    if (!token) {
      setPeriod(null);
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const response = await getActiveSettlementPeriod(token);
      setPeriod(response);
    } catch (err) {
      await handleApiError(err);
      setError(formatApiError(err));
      setPeriod(null);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void load();
  }, [token]);

  return {
    period,
    loading,
    error,
    load,
    reload: load,
  };
}
