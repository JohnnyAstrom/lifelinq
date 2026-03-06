import { useEffect, useRef, type ReactNode } from 'react';
import { acceptInvitation } from '../features/group/api/groupApi';
import { ApiError, UnauthorizedError } from '../shared/api/client';
import type { MeResponse } from '../features/auth/api/meApi';
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
  currentMe: MeResponse | null;
  manualInviteAcceptInFlight: boolean;
  pendingInviteToken: string | null;
  clearPendingInviteToken: () => void;
  reloadMe: () => Promise<MeResponse | null>;
  handleApiError: (error: unknown) => Promise<void>;
  setInviteError: (message: string | null) => void;
  setAutoAccepting: (value: boolean) => void;
};

export function InviteFlowCoordinator({
  children,
  status,
  token,
  currentMe,
  manualInviteAcceptInFlight,
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
    if (manualInviteAcceptInFlight) {
      return;
    }

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
    const beforeMembershipGroupIds = new Set((currentMe?.memberships ?? []).map((membership) => membership.groupId));
    const beforeActiveGroupId = currentMe?.activeGroupId ?? null;
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
          if (isRevokedInvitationConflict(err)) {
            const meAfterConflict = await reloadMe();
            if (didAlreadyJoinInvitedGroup(beforeMembershipGroupIds, beforeActiveGroupId, meAfterConflict)) {
              clearPendingInviteToken();
              setInviteError(null);
              return;
            }
            setInviteError('This invitation was already used. Ask for a new invite if you still need access.');
            return;
          }
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
    currentMe,
    handleApiError,
    manualInviteAcceptInFlight,
    pendingInviteToken,
    reloadMe,
    setAutoAccepting,
    setInviteError,
    status,
    token,
  ]);

  return <>{children}</>;
}

function isRevokedInvitationConflict(error: unknown): boolean {
  if (!(error instanceof ApiError) || error.status !== 409) {
    return false;
  }
  return error.body.toLowerCase().includes('invitation is revoked');
}

function didAlreadyJoinInvitedGroup(
  beforeGroupIds: Set<string>,
  beforeActiveGroupId: string | null,
  meAfterConflict: MeResponse | null
): boolean {
  if (!meAfterConflict) {
    return false;
  }

  for (const membership of meAfterConflict.memberships) {
    if (!beforeGroupIds.has(membership.groupId)) {
      return true;
    }
  }

  const afterActiveGroupId = meAfterConflict.activeGroupId ?? null;
  if (!afterActiveGroupId || afterActiveGroupId === beforeActiveGroupId) {
    return false;
  }
  return !beforeGroupIds.has(afterActiveGroupId);
}
