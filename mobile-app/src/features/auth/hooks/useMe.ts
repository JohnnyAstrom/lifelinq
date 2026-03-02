import { useEffect, useRef, useState } from 'react';
import { formatApiError } from '../../../shared/api/client';
import { useAuth } from '../../../shared/auth/AuthContext';
import { fetchMe, MeResponse } from '../api/meApi';

export function useMe(token: string | null) {
  const [data, setData] = useState<MeResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [reloadKey, setReloadKey] = useState(0);
  const pendingReloadsRef = useRef<Array<(value: MeResponse | null) => void>>([]);
  const { handleApiError } = useAuth();

  function resolvePendingReloads(value: MeResponse | null) {
    if (pendingReloadsRef.current.length === 0) {
      return;
    }
    const pending = pendingReloadsRef.current;
    pendingReloadsRef.current = [];
    pending.forEach((resolve) => resolve(value));
  }

  useEffect(() => {
    let cancelled = false;

    async function load() {
      if (!token) {
        setData(null);
        resolvePendingReloads(null);
        return;
      }

      setLoading(true);
      setError(null);

      try {
        const result = await fetchMe(token);
        if (!cancelled) {
          setData(result);
          resolvePendingReloads(result);
        }
      } catch (err) {
        await handleApiError(err);
        if (!cancelled) {
          setError(formatApiError(err));
          resolvePendingReloads(null);
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

  function reload(): Promise<MeResponse | null> {
    if (!token) {
      setData(null);
      return Promise.resolve(null);
    }
    return new Promise((resolve) => {
      pendingReloadsRef.current.push(resolve);
      setReloadKey((value) => value + 1);
    });
  }

  return { data, error, loading, reload };
}
