import { useEffect, useRef, type ReactNode } from 'react';
import { useAuth } from '../shared/auth/AuthContext';
import { LoginScreen } from '../features/auth/screens/LoginScreen';
import { HydratingSplashScreen } from './HydratingSplashScreen';

type Props = {
  children: ReactNode;
  authError: string | null;
  onClearAuthError: () => void;
  onClearPendingInviteToken: () => void;
  onClearInviteError: () => void;
};

export function AuthGate({
  children,
  authError,
  onClearAuthError,
  onClearPendingInviteToken,
  onClearInviteError,
}: Props) {
  const { status, token, me, meLoading, meError, login, logout } = useAuth();
  const recoveringRef = useRef(false);

  useEffect(() => {
    if (status !== 'unauthenticated') {
      return;
    }
    onClearPendingInviteToken();
    onClearInviteError();
    onClearAuthError();
  }, [onClearAuthError, onClearInviteError, onClearPendingInviteToken, status]);

  useEffect(() => {
    if (status !== 'authenticated') {
      recoveringRef.current = false;
      return;
    }
    const missingAuthState = !token;
    const failedProfileHydration = !meLoading && !me && !!meError;
    if (!missingAuthState && !failedProfileHydration) {
      return;
    }
    if (recoveringRef.current) {
      return;
    }
    recoveringRef.current = true;
    void logout();
  }, [logout, me, meError, meLoading, status, token]);

  if (status === 'hydrating') {
    return <HydratingSplashScreen />;
  }

  if (status === 'unauthenticated') {
    return <LoginScreen onLoggedIn={login} authError={authError} onClearAuthError={onClearAuthError} />;
  }

  return <>{children}</>;
}
