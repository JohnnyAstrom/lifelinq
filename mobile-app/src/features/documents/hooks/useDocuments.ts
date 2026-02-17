import { useEffect, useMemo, useState } from 'react';
import { ApiError, formatApiError } from '../../../shared/api/client';
import { useAuth } from '../../../shared/auth/AuthContext';
import { createDocument, deleteDocument, listDocuments, type CreateDocumentRequest, type DocumentItemResponse } from '../api/documentsApi';

export function useDocuments(token: string | null) {
  const [items, setItems] = useState<DocumentItemResponse[]>([]);
  const [query, setQuery] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const { handleApiError } = useAuth();

  const trimmedQuery = useMemo(() => query.trim(), [query]);

  async function load(nextQuery?: string) {
    if (!token) {
      setItems([]);
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const result = await listDocuments(token, nextQuery ?? trimmedQuery);
      setItems(result);
    } catch (err) {
      await handleApiError(err);
      setError(formatApiError(err));
    } finally {
      setLoading(false);
    }
  }

  async function create(payload: CreateDocumentRequest): Promise<boolean> {
    if (!token) {
      throw new Error('Missing token');
    }
    setError(null);
    try {
      await createDocument(token, payload);
      await load(trimmedQuery);
      return true;
    } catch (err) {
      await handleApiError(err);
      setError(formatApiError(err));
      return false;
    }
  }

  async function remove(id: string): Promise<void> {
    if (!token) {
      throw new Error('Missing token');
    }
    const previousItems = items;
    setError(null);
    setItems((current) => current.filter((item) => item.id !== id));
    try {
      await deleteDocument(token, id);
    } catch (err) {
      setItems(previousItems);
      await handleApiError(err);
      if (err instanceof ApiError && err.status === 404) {
        await load(trimmedQuery);
      }
      setError(formatApiError(err));
      throw err;
    }
  }

  useEffect(() => {
    load(trimmedQuery);
  }, [token, trimmedQuery]);

  return {
    items,
    query,
    setQuery,
    error,
    loading,
    reload: () => load(trimmedQuery),
    create,
    remove,
  };
}
