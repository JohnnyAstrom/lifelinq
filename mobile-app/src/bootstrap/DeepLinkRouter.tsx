import { type ReactNode } from 'react';
import { useDeepLinkBootstrap } from './useDeepLinkBootstrap';

type Props = {
  children: ReactNode;
  status: 'hydrating' | 'unauthenticated' | 'authenticated';
  onLoginFromDeepLink: (token: string, refreshToken?: string | null) => Promise<void>;
  onAuthError: (message: string | null) => void;
  onInviteToken: (token: string) => void;
  onClearInviteError: () => void;
};

export function DeepLinkRouter({
  children,
  onLoginFromDeepLink,
  onAuthError,
  onInviteToken,
  onClearInviteError,
}: Props) {
  useDeepLinkBootstrap({
    onLoginFromDeepLink,
    onAuthError,
    onInviteToken,
    onClearInviteError,
  });

  return <>{children}</>;
}

