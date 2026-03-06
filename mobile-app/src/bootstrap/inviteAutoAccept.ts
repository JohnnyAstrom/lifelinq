export type AuthStatus = 'hydrating' | 'unauthenticated' | 'authenticated';

export type AutoAcceptDecisionInput = {
  status: AuthStatus;
  accessToken: string | null;
  pendingInviteToken: string | null;
  inFlightAttemptKey: string | null;
  lastAttemptedKey: string | null;
};

export function buildInviteAttemptKey(
  _accessToken: string | null,
  pendingInviteToken: string | null
): string | null {
  if (!pendingInviteToken) {
    return null;
  }
  return pendingInviteToken;
}

export function shouldStartInviteAutoAccept(input: AutoAcceptDecisionInput): boolean {
  const attemptKey = buildInviteAttemptKey(input.accessToken, input.pendingInviteToken);
  if (input.status !== 'authenticated' || !attemptKey) {
    return false;
  }
  if (input.inFlightAttemptKey === attemptKey) {
    return false;
  }
  if (input.lastAttemptedKey === attemptKey) {
    return false;
  }
  return true;
}

export function isTerminalInviteAcceptError(error: unknown): boolean {
  if (!isHttpStatusError(error)) {
    return false;
  }
  return error.status === 400 || error.status === 404 || error.status === 409;
}

export function shouldClearPendingInviteAfterAccept(
  succeeded: boolean,
  error: unknown
): boolean {
  if (succeeded) {
    return true;
  }
  return isTerminalInviteAcceptError(error);
}

function isHttpStatusError(error: unknown): error is { status: number } {
  if (!error || typeof error !== 'object') {
    return false;
  }
  const candidate = error as { status?: unknown };
  return typeof candidate.status === 'number';
}
