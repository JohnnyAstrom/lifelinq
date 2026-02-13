import { useEffect, useState } from 'react';
import { formatApiError } from '../../../shared/api/client';
import { useAuth } from '../../../shared/auth/AuthContext';
import { fetchMe, MeResponse } from '../api/meApi';

export function useMe(token: string | null) {
  const [data, setData] = useState<MeResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [reloadKey, setReloadKey] = useState(0);
  const { handleApiError } = useAuth();

  useEffect(() => {
    let cancelled = false;

    async function load() {
      if (!token) {
        setData(null);
        return;
      }

      setLoading(true);
      setError(null);

      try {
        const result = await fetchMe(token);
        if (!cancelled) {
          setData(result);
        }
      } catch (err) {
        await handleApiError(err);
        if (!cancelled) {
          setError(formatApiError(err));
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    load();
    return () => {
      cancelled = true;
    };
  }, [token, reloadKey]);

  function reload() {
    setReloadKey((value) => value + 1);
  }

  return { data, error, loading, reload };
}
