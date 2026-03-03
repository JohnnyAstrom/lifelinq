import React, { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { UnauthorizedError } from '../api/client';
import { clearAuthTokens, getToken, setAuthTokens, setToken } from '../api/tokenStore';
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
    if (newRefreshToken && newRefreshToken.trim()) {
      await setAuthTokens(newAccessToken, newRefreshToken.trim());
    } else {
      await setToken(newAccessToken);
    }
    await hydrateWithToken(newAccessToken);
  }

  async function logout() {
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
