import { type ReactNode } from 'react';
import { useDeepLinkBootstrap } from './useDeepLinkBootstrap';

type Props = {
  children: ReactNode;
  status: 'hydrating' | 'unauthenticated' | 'authenticated';
  onLoginFromDeepLink: (token: string, refreshToken?: string | null) => Promise<void>;
  onAuthError: (message: string | null) => void;
  onInviteToken: (token: string) => void;
  onClearInviteError: () => void;
  onSharedRecipeUrl?: (url: string) => void;
  onSharedRecipeAsset?: (asset: {
    assetKind: 'DOCUMENT' | 'IMAGE';
    referenceId: string;
    sourceLabel?: string | null;
    originalFilename?: string | null;
    mimeType?: string | null;
  }) => void;
  onRecipeCaptureFailure?: (message: string) => void;
};

export function DeepLinkRouter({
  children,
  onLoginFromDeepLink,
  onAuthError,
  onInviteToken,
  onClearInviteError,
  onSharedRecipeUrl,
  onSharedRecipeAsset,
  onRecipeCaptureFailure,
}: Props) {
  useDeepLinkBootstrap({
    onLoginFromDeepLink,
    onAuthError,
    onInviteToken,
    onClearInviteError,
    onSharedRecipeUrl: onSharedRecipeUrl ?? (() => {}),
    onSharedRecipeAsset: onSharedRecipeAsset ?? (() => {}),
    onRecipeCaptureFailure: onRecipeCaptureFailure ?? (() => {}),
  });

  return <>{children}</>;
}
