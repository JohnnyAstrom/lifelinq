import { useEffect, useState } from 'react';
import { formatApiError } from '../../../shared/api/client';
import { addMember, fetchMembers, MemberItemResponse, removeMember } from '../api/householdMembersApi';

export function useHouseholdMembers(token: string | null) {
  const [items, setItems] = useState<MemberItemResponse[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function load() {
    if (!token) {
      setItems([]);
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const result = await fetchMembers(token);
      setItems(result);
    } catch (err) {
      setError(formatApiError(err));
    } finally {
      setLoading(false);
    }
  }

  async function add(userId: string) {
    if (!token) {
      throw new Error('Missing token');
    }
    setError(null);
    try {
      await addMember(token, userId);
      await load();
    } catch (err) {
      setError(formatApiError(err));
    }
  }

  async function remove(userId: string) {
    if (!token) {
      throw new Error('Missing token');
    }
    setError(null);
    try {
      await removeMember(token, userId);
      await load();
    } catch (err) {
      setError(formatApiError(err));
    }
  }

  useEffect(() => {
    load();
  }, [token]);

  return { items, error, loading, reload: load, add, remove };
}
