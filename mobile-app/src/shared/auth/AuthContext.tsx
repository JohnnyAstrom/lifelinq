import React, { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { UnauthorizedError } from '../api/client';
import { clearToken, getToken, setToken } from '../api/tokenStore';

type AuthState = {
  isAuthenticated: boolean;
  isInitializing: boolean;
  token: string | null;
  login: (token: string) => Promise<void>;
  logout: () => Promise<void>;
  handleApiError: (err: unknown) => Promise<void>;
};

const AuthContext = createContext<AuthState | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [token, setTokenState] = useState<string | null>(null);
  const [isInitializing, setIsInitializing] = useState(true);

  useEffect(() => {
    (async () => {
      const stored = await getToken();
      setTokenState(stored);
      setIsInitializing(false);
    })();
  }, []);

  async function login(newToken: string) {
    await setToken(newToken);
    setTokenState(newToken);
  }

  async function logout() {
    await clearToken();
    setTokenState(null);
  }

  async function handleApiError(err: unknown) {
    if (err instanceof UnauthorizedError) {
      await logout();
    }
  }

  const value = useMemo<AuthState>(
    () => ({
      isAuthenticated: !!token,
      isInitializing,
      token,
      login,
      logout,
      handleApiError,
    }),
    [token, isInitializing]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return ctx;
}
