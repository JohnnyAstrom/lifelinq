import { useEffect, type ReactNode } from 'react';
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
  const { status, login } = useAuth();

  useEffect(() => {
    if (status !== 'unauthenticated') {
      return;
    }
    onClearPendingInviteToken();
    onClearInviteError();
    onClearAuthError();
  }, [onClearAuthError, onClearInviteError, onClearPendingInviteToken, status]);

  if (status === 'hydrating') {
    return <HydratingSplashScreen />;
  }

  if (status === 'unauthenticated') {
    return <LoginScreen onLoggedIn={login} authError={authError} onClearAuthError={onClearAuthError} />;
  }

  return <>{children}</>;
}

