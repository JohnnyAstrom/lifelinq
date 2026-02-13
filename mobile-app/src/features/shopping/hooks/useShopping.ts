import { useState } from 'react';
import { formatApiError } from '../../../shared/api/client';
import { useAuth } from '../../../shared/auth/AuthContext';
import { createShoppingItem } from '../api/shoppingApi';

export function useShopping(token: string | null) {
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const { handleApiError } = useAuth();

  async function add(name: string) {
    if (!token) {
      throw new Error('Missing token');
    }
    setLoading(true);
    setError(null);
    try {
      await createShoppingItem(token, name);
    } catch (err) {
      await handleApiError(err);
      setError(formatApiError(err));
    } finally {
      setLoading(false);
    }
  }

  return { add, error, loading };
}
