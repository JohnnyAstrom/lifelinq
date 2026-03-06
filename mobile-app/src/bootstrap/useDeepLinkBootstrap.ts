import { Linking } from 'react-native';
import { useCallback, useEffect, useRef } from 'react';

type Params = {
  onLoginFromDeepLink: (token: string, refreshToken?: string | null) => Promise<void>;
  onAuthError: (message: string | null) => void;
  onInviteToken: (token: string) => void;
  onClearInviteError: () => void;
};

function parseAuthCompleteUrl(url: string): { token?: string; refresh?: string; error?: string } | null {
  const [base, fragment = ''] = url.split('#', 2);
  const normalizedBase = base.endsWith('/') ? base.slice(0, -1) : base;
  if (normalizedBase !== 'mobileapp://auth/complete') {
    return null;
  }
  const params = new URLSearchParams(fragment);
  const token = params.get('token')?.trim();
  const refresh = params.get('refresh')?.trim();
  const error = params.get('error')?.trim();
  if (token) {
    return { token, refresh };
  }
  if (error) {
    return { error };
  }
  return null;
}

function parseInviteUrl(url: string): { token: string } | null {
  const [baseWithPath, queryAndMaybeFragment = ''] = url.split('?', 2);
  const normalizedBase = baseWithPath.endsWith('/') ? baseWithPath.slice(0, -1) : baseWithPath;
  if (normalizedBase !== 'mobileapp://invite') {
    return null;
  }
  const query = queryAndMaybeFragment.split('#', 1)[0];
  const params = new URLSearchParams(query);
  const token = params.get('token')?.trim();
  if (!token) {
    return null;
  }
  return { token };
}

export function useDeepLinkBootstrap({
  onLoginFromDeepLink,
  onAuthError,
  onInviteToken,
  onClearInviteError,
}: Params) {
  const lastHandledUrlRef = useRef<string | null>(null);
  const tokenInFlightRef = useRef<string | null>(null);

  const handleIncomingUrl = useCallback(
    async (url: string | null | undefined) => {
      if (!url || lastHandledUrlRef.current === url) {
        return;
      }

      const parsed = parseAuthCompleteUrl(url);
      if (parsed) {
        lastHandledUrlRef.current = url;
        if (parsed.token) {
          if (tokenInFlightRef.current === parsed.token) {
            return;
          }
          tokenInFlightRef.current = parsed.token;
          onAuthError(null);
          try {
            await onLoginFromDeepLink(parsed.token, parsed.refresh ?? null);
          } finally {
            tokenInFlightRef.current = null;
          }
          return;
        }
        if (parsed.error) {
          onAuthError('Magic link is invalid or expired.');
        }
        return;
      }

      const invite = parseInviteUrl(url);
      if (invite) {
        lastHandledUrlRef.current = url;
        onClearInviteError();
        onInviteToken(invite.token);
      }
    },
    [onAuthError, onClearInviteError, onInviteToken, onLoginFromDeepLink]
  );

  useEffect(() => {
    let active = true;

    (async () => {
      const initialUrl = await Linking.getInitialURL();
      if (!active) {
        return;
      }
      void handleIncomingUrl(initialUrl);
    })();

    const subscription = Linking.addEventListener('url', (event) => {
      void handleIncomingUrl(event.url);
    });

    return () => {
      active = false;
      subscription.remove();
    };
  }, [handleIncomingUrl]);
}

