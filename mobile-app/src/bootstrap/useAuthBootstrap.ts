import { useCallback, useState } from 'react';
import { useAuth } from '../shared/auth/AuthContext';

export function useAuthBootstrap() {
  const auth = useAuth();
  const [authError, setAuthError] = useState<string | null>(null);

  const clearAuthError = useCallback(() => {
    setAuthError(null);
  }, []);

  return {
    ...auth,
    authError,
    setAuthError,
    clearAuthError,
  };
}

