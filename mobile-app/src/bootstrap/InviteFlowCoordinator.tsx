import { type ReactNode } from 'react';
import type { MeResponse } from '../features/auth/api/meApi';
import { useInviteAutoAccept } from '../flows/useInviteAutoAccept';

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
  useInviteAutoAccept({
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
  });

  return <>{children}</>;
}
