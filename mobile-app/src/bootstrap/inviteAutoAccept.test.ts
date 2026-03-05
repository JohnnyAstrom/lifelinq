// @ts-nocheck
import {
  buildInviteAttemptKey,
  isTerminalInviteAcceptError,
  shouldClearPendingInviteAfterAccept,
  shouldStartInviteAutoAccept,
} from './inviteAutoAccept';

function apiError(status: number): { status: number } {
  return { status };
}

describe('invite auto-accept guards', () => {
  test('token survives unauthenticated/hydrating until authenticated decision point', () => {
    const accessToken = 'access-1';
    const inviteToken = 'invite-1';

    expect(
      shouldStartInviteAutoAccept({
        status: 'unauthenticated',
        accessToken,
        pendingInviteToken: inviteToken,
        inFlightAttemptKey: null,
        lastAttemptedKey: null,
      })
    ).toBe(false);

    expect(
      shouldStartInviteAutoAccept({
        status: 'hydrating',
        accessToken,
        pendingInviteToken: inviteToken,
        inFlightAttemptKey: null,
        lastAttemptedKey: null,
      })
    ).toBe(false);

    expect(
      shouldStartInviteAutoAccept({
        status: 'authenticated',
        accessToken,
        pendingInviteToken: inviteToken,
        inFlightAttemptKey: null,
        lastAttemptedKey: null,
      })
    ).toBe(true);
  });

  test('same token+session attempt key is accepted only once', () => {
    const key = buildInviteAttemptKey('access-2', 'invite-2');
    expect(key).toBe('invite-2::access-2');

    expect(
      shouldStartInviteAutoAccept({
        status: 'authenticated',
        accessToken: 'access-2',
        pendingInviteToken: 'invite-2',
        inFlightAttemptKey: null,
        lastAttemptedKey: key,
      })
    ).toBe(false);
  });

  test('pending token is cleared on success and terminal failures', () => {
    expect(shouldClearPendingInviteAfterAccept(true, null)).toBe(true);
    expect(shouldClearPendingInviteAfterAccept(false, apiError(409))).toBe(true);
    expect(shouldClearPendingInviteAfterAccept(false, apiError(400))).toBe(true);
  });

  test('non-terminal failures do not clear pending invite token', () => {
    expect(isTerminalInviteAcceptError(apiError(500))).toBe(false);
    expect(shouldClearPendingInviteAfterAccept(false, apiError(500))).toBe(false);
    expect(shouldClearPendingInviteAfterAccept(false, new Error('network'))).toBe(false);
  });
});
