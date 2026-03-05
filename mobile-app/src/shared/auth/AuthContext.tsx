import React, { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { invalidateAuthSession, revokeRefreshSession, UnauthorizedError } from '../api/client';
import { clearAuthTokens, getRefreshToken, getToken, setAuthTokens, setToken } from '../api/tokenStore';
import { fetchMe, type MeResponse } from '../../features/auth/api/meApi';

type AuthState = {
  status: 'unauthenticated' | 'hydrating' | 'authenticated';
  isAuthenticated: boolean;
  isInitializing: boolean;
  token: string | null;
  me: MeResponse | null;
  meLoading: boolean;
  meError: string | null;
  reloadMe: () => Promise<MeResponse | null>;
  login: (accessToken: string, refreshToken?: string | null) => Promise<void>;
  logout: () => Promise<void>;
  handleApiError: (err: unknown) => Promise<void>;
};

const AuthContext = createContext<AuthState | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [token, setTokenState] = useState<string | null>(null);
  const [status, setStatus] = useState<'unauthenticated' | 'hydrating' | 'authenticated'>('hydrating');
  const [me, setMe] = useState<MeResponse | null>(null);
  const [meLoading, setMeLoading] = useState(false);
  const [meError, setMeError] = useState<string | null>(null);

  async function hydrateWithToken(candidateToken: string): Promise<MeResponse | null> {
    setStatus('hydrating');
    setMeLoading(true);
    setMeError(null);
    try {
      const response = await fetchMe(candidateToken);
      setTokenState(candidateToken);
      setMe(response);
      setStatus('authenticated');
      return response;
    } catch {
      await clearAuthTokens();
      setTokenState(null);
      setMe(null);
      setStatus('unauthenticated');
      return null;
    } finally {
      setMeLoading(false);
    }
  }

  useEffect(() => {
    (async () => {
      const stored = await getToken();
      if (!stored) {
        setTokenState(null);
        setMe(null);
        setStatus('unauthenticated');
        return;
      }
      await hydrateWithToken(stored);
    })();
  }, []);

  async function loginWithRefresh(newAccessToken: string, newRefreshToken?: string | null) {
    const normalizedAccessToken = newAccessToken.trim();
    const existingAccessToken = await getToken();
    if (existingAccessToken === normalizedAccessToken) {
      return;
    }

    if (newRefreshToken && newRefreshToken.trim()) {
      await setAuthTokens(normalizedAccessToken, newRefreshToken.trim());
    } else {
      await setToken(normalizedAccessToken);
    }
    await hydrateWithToken(normalizedAccessToken);
  }

  async function logout() {
    invalidateAuthSession();
    try {
      const currentRefreshToken = await getRefreshToken();
      if (token && currentRefreshToken) {
        await revokeRefreshSession(token, currentRefreshToken);
      }
    } catch {
      // Logout must still succeed locally even if backend revoke fails.
    }
    await clearAuthTokens();
    setTokenState(null);
    setMe(null);
    setMeError(null);
    setStatus('unauthenticated');
  }

  async function reloadMe(): Promise<MeResponse | null> {
    if (!token) {
      setMe(null);
      setMeError(null);
      return null;
    }
    setMeLoading(true);
    setMeError(null);
    try {
      const response = await fetchMe(token);
      setMe(response);
      setStatus('authenticated');
      return response;
    } catch (err) {
      if (err instanceof UnauthorizedError) {
        await logout();
        return null;
      }
      if (!me) {
        await logout();
        return null;
      }
      const fallbackMessage = err instanceof Error && err.message ? err.message : 'Failed to load profile';
      setMeError(fallbackMessage);
      return null;
    } finally {
      setMeLoading(false);
    }
  }

  async function handleApiError(err: unknown) {
    if (err instanceof UnauthorizedError) {
      await logout();
    }
  }

  const value = useMemo<AuthState>(
    () => ({
      status,
      isAuthenticated: status === 'authenticated',
      isInitializing: status === 'hydrating',
      token,
      me,
      meLoading,
      meError,
      reloadMe,
      login: loginWithRefresh,
      logout,
      handleApiError,
    }),
    [status, token, me, meLoading, meError]
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
