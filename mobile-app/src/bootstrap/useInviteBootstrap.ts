import { useCallback, useState } from 'react';
import type { MeResponse } from '../features/auth/api/meApi';
import { useInviteAutoAccept } from '../flows/useInviteAutoAccept';
import type { AuthStatus } from '../flows/inviteAutoAccept';

type Params = {
  status: AuthStatus;
  token: string | null;
  me: MeResponse | null;
  pendingInviteToken: string | null;
  clearPendingInviteToken: () => void;
  reloadMe: () => Promise<MeResponse | null>;
  handleApiError: (error: unknown) => Promise<void>;
};

export function useInviteBootstrap({
  status,
  token,
  me,
  pendingInviteToken,
  clearPendingInviteToken,
  reloadMe,
  handleApiError,
}: Params) {
  const [inviteError, setInviteError] = useState<string | null>(null);
  const [autoAccepting, setAutoAccepting] = useState(false);
  const [manualInviteAcceptInFlight, setManualInviteAcceptInFlight] = useState(false);

  const clearInviteError = useCallback(() => {
    setInviteError(null);
  }, []);

  useInviteAutoAccept({
    status,
    token,
    currentMe: me,
    manualInviteAcceptInFlight,
    pendingInviteToken,
    clearPendingInviteToken,
    reloadMe,
    handleApiError,
    setInviteError,
    setAutoAccepting,
  });

  return {
    inviteError,
    clearInviteError,
    autoAccepting,
    manualInviteAcceptInFlight,
    setManualInviteAcceptInFlight,
  };
}
