import { useEffect, useRef, type ReactNode } from 'react';
import { acceptInvitation } from '../features/group/api/groupApi';
import { ApiError, UnauthorizedError } from '../shared/api/client';
import {
  buildInviteAttemptKey,
  isTerminalInviteAcceptError,
  shouldClearPendingInviteAfterAccept,
  shouldStartInviteAutoAccept,
} from './inviteAutoAccept';

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
  const autoAcceptInFlightRef = useRef<string | null>(null);
  const lastAutoAcceptAttemptRef = useRef<string | null>(null);

  useEffect(() => {
    if (!shouldStartInviteAutoAccept({
      status,
      accessToken: token,
      pendingInviteToken,
      inFlightAttemptKey: autoAcceptInFlightRef.current,
      lastAttemptedKey: lastAutoAcceptAttemptRef.current,
    })) {
      return;
    }

    const attemptKey = buildInviteAttemptKey(token, pendingInviteToken);
    if (!attemptKey || !pendingInviteToken || !token) {
      return;
    }
    lastAutoAcceptAttemptRef.current = attemptKey;
    let cancelled = false;
    autoAcceptInFlightRef.current = attemptKey;
    setAutoAccepting(true);

    (async () => {
      try {
        await acceptInvitation(token, pendingInviteToken);
        if (cancelled) {
          return;
        }
        if (shouldClearPendingInviteAfterAccept(true, null)) {
          clearPendingInviteToken();
        }
        await reloadMe();
        setInviteError(null);
      } catch (err) {
        if (cancelled) {
          return;
        }
        if (err instanceof UnauthorizedError || (err instanceof ApiError && err.status === 401)) {
          await handleApiError(err);
          setInviteError(null);
          return;
        }
        if (isTerminalInviteAcceptError(err)) {
          if (shouldClearPendingInviteAfterAccept(false, err)) {
            clearPendingInviteToken();
          }
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
