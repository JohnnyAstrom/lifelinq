import { useCallback } from 'react';
import { acceptInvitation, resolveInvitationCode } from '../features/group/api/groupApi';
import type { MeResponse } from '../features/auth/api/meApi';
import { ApiError, UnauthorizedError } from '../shared/api/client';

type UseJoinPlaceFlowParams = {
  token: string | null;
  reloadMe: () => Promise<MeResponse | null>;
  handleApiError: (err: unknown) => Promise<void>;
  setPendingInviteToken: (token: string) => void;
  clearPendingInviteToken: () => void;
  setManualInviteAcceptInFlight?: (value: boolean) => void;
};

export type JoinPlaceFlowResult =
  | { status: 'success'; placeName: string }
  | { status: 'unauthorized'; message: string }
  | { status: 'error'; message: string };

export function useJoinPlaceFlow({
  token,
  reloadMe,
  handleApiError,
  setPendingInviteToken,
  clearPendingInviteToken,
  setManualInviteAcceptInFlight = () => {},
}: UseJoinPlaceFlowParams) {
  const joinPlace = useCallback(
    async (input: string): Promise<JoinPlaceFlowResult> => {
      if (!token) {
        return { status: 'error', message: 'Could not join this place. Check the code and try again.' };
      }

      const normalizedInput = input.trim();
      let resolvedInvitationToken: string | null = null;
      setManualInviteAcceptInFlight(true);
      try {
        const isShortCodeInput = /^[A-Za-z0-9]{6}$/.test(normalizedInput);
        const invitationToken = isShortCodeInput
          ? (await resolveInvitationCode(token, normalizedInput)).token
          : normalizedInput;

        resolvedInvitationToken = invitationToken;
        setPendingInviteToken(invitationToken);
        await acceptInvitation(token, invitationToken);

        const updatedMe = await reloadMe();
        const activeMembership = updatedMe?.activeGroupId
          ? updatedMe.memberships.find((membership) => membership.groupId === updatedMe.activeGroupId) ?? null
          : null;
        const placeName = activeMembership?.groupName?.trim() || 'My space';
        clearPendingInviteToken();
        return { status: 'success', placeName };
      } catch (err) {
        await handleApiError(err);
        if (isUnauthorizedInviteAcceptError(err) && resolvedInvitationToken) {
          return { status: 'unauthorized', message: 'Session expired. Log in again to continue joining this place.' };
        }
        if (resolvedInvitationToken) {
          clearPendingInviteToken();
        }
        return { status: 'error', message: 'Could not join this place. Check the code and try again.' };
      } finally {
        setManualInviteAcceptInFlight(false);
      }
    },
    [
      clearPendingInviteToken,
      handleApiError,
      reloadMe,
      setManualInviteAcceptInFlight,
      setPendingInviteToken,
      token,
    ]
  );

  return { joinPlace };
}

function isUnauthorizedInviteAcceptError(err: unknown): boolean {
  return err instanceof UnauthorizedError || (err instanceof ApiError && err.status === 401);
}
