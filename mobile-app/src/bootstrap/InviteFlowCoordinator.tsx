import { useEffect, useRef, type ReactNode } from 'react';
import { acceptInvitation } from '../features/group/api/groupApi';
import { ApiError, UnauthorizedError } from '../shared/api/client';

type Props = {
  children: ReactNode;
  status: 'hydrating' | 'unauthenticated' | 'authenticated';
  token: string | null;
  pendingInviteToken: string | null;
  clearPendingInviteToken: () => void;
  reloadMe: () => Promise<unknown>;
  handleApiError: (error: unknown) => Promise<void>;
  setInviteError: (message: string | null) => void;
  setAutoAccepting: (value: boolean) => void;
};

export function InviteFlowCoordinator({
  children,
  status,
  token,
  pendingInviteToken,
  clearPendingInviteToken,
  reloadMe,
  handleApiError,
  setInviteError,
  setAutoAccepting,
}: Props) {
  const previousStatusRef = useRef(status);
  const autoAcceptInFlightRef = useRef<string | null>(null);
  const lastAutoAcceptedTokenRef = useRef<string | null>(null);

  useEffect(() => {
    const previousStatus = previousStatusRef.current;
    previousStatusRef.current = status;

    const transitionedToAuthenticated = previousStatus === 'unauthenticated' && status === 'authenticated';
    if (!transitionedToAuthenticated || !pendingInviteToken || !token) {
      return;
    }
    if (autoAcceptInFlightRef.current === pendingInviteToken) {
      return;
    }
    if (lastAutoAcceptedTokenRef.current === pendingInviteToken) {
      return;
    }

    let cancelled = false;
    autoAcceptInFlightRef.current = pendingInviteToken;
    setAutoAccepting(true);

    (async () => {
      try {
        await acceptInvitation(token, pendingInviteToken);
        if (cancelled) {
          return;
        }
        lastAutoAcceptedTokenRef.current = pendingInviteToken;
        clearPendingInviteToken();
        await reloadMe();
        setInviteError(null);
      } catch (err) {
        if (cancelled) {
          return;
        }
        clearPendingInviteToken();
        if (err instanceof UnauthorizedError || (err instanceof ApiError && err.status === 401)) {
          await handleApiError(err);
          setInviteError(null);
          return;
        }
        if (err instanceof ApiError && err.status === 409) {
          setInviteError('This invitation is invalid or expired.');
          return;
        }
        setInviteError('Invitation could not be accepted.');
      } finally {
        if (!cancelled) {
          autoAcceptInFlightRef.current = null;
          setAutoAccepting(false);
        }
      }
    })();

    return () => {
      cancelled = true;
      autoAcceptInFlightRef.current = null;
      setAutoAccepting(false);
    };
  }, [
    clearPendingInviteToken,
    handleApiError,
    pendingInviteToken,
    reloadMe,
    setAutoAccepting,
    setInviteError,
    status,
    token,
  ]);

  return <>{children}</>;
}

