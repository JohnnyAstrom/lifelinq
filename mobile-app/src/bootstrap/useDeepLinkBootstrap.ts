import { Linking } from 'react-native';
import { useCallback, useEffect, useRef } from 'react';
import {
  parseAuthCompleteUrl,
  parseInviteUrl,
  parseSharedRecipeUrl,
} from './deepLinkIntents';

type Params = {
  onLoginFromDeepLink: (token: string, refreshToken?: string | null) => Promise<void>;
  onAuthError: (message: string | null) => void;
  onInviteToken: (token: string) => void;
  onClearInviteError: () => void;
  onSharedRecipeUrl: (url: string) => void;
  onRecipeCaptureFailure: (message: string) => void;
};

export function useDeepLinkBootstrap({
  onLoginFromDeepLink,
  onAuthError,
  onInviteToken,
  onClearInviteError,
  onSharedRecipeUrl,
  onRecipeCaptureFailure,
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
        return;
      }

      const sharedRecipe = parseSharedRecipeUrl(url);
      if (sharedRecipe) {
        lastHandledUrlRef.current = url;
        if (sharedRecipe.url) {
          onSharedRecipeUrl(sharedRecipe.url);
          return;
        }
        if (sharedRecipe.invalid) {
          onRecipeCaptureFailure('We could not use that shared link. Try sharing a full recipe page link.');
          return;
        }
      }

    },
    [
      onAuthError,
      onClearInviteError,
      onInviteToken,
      onLoginFromDeepLink,
      onRecipeCaptureFailure,
      onSharedRecipeUrl,
    ]
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
