export type ParsedAuthCompleteUrl = {
  token?: string;
  refresh?: string;
  error?: string;
};

export type ParsedInviteUrl = {
  token: string;
};

export type ParsedSharedRecipeUrl = {
  url: string | null;
  invalid: boolean;
};

export type ParsedSharedRecipeAsset = {
  assetKind: 'DOCUMENT' | 'IMAGE';
  referenceId: string | null;
  sourceLabel: string | null;
  originalFilename: string | null;
  mimeType: string | null;
  invalid: boolean;
  unsupported: boolean;
};

export function parseAuthCompleteUrl(url: string): ParsedAuthCompleteUrl | null {
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

export function parseInviteUrl(url: string): ParsedInviteUrl | null {
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

export function parseSharedRecipeUrl(url: string): ParsedSharedRecipeUrl | null {
  const [baseWithPath, queryAndMaybeFragment = ''] = url.split('?', 2);
  const normalizedBase = baseWithPath.endsWith('/') ? baseWithPath.slice(0, -1) : baseWithPath;
  if (normalizedBase !== 'mobileapp://recipes/import-url') {
    return null;
  }

  const query = queryAndMaybeFragment.split('#', 1)[0];
  const params = new URLSearchParams(query);
  const sharedUrl = params.get('url')?.trim() ?? null;
  const error = params.get('error')?.trim()?.toLowerCase() ?? null;

  if (sharedUrl) {
    return {
      url: sharedUrl,
      invalid: false,
    };
  }

  return {
    url: null,
    invalid: error === 'invalid',
  };
}

export function parseSharedRecipeAsset(url: string): ParsedSharedRecipeAsset | null {
  const [baseWithPath, queryAndMaybeFragment = ''] = url.split('?', 2);
  const normalizedBase = baseWithPath.endsWith('/') ? baseWithPath.slice(0, -1) : baseWithPath;
  if (normalizedBase !== 'mobileapp://recipes/import-asset') {
    return null;
  }

  const query = queryAndMaybeFragment.split('#', 1)[0];
  const params = new URLSearchParams(query);
  const assetKind = params.get('assetKind')?.trim()?.toUpperCase() ?? null;
  const referenceId = params.get('referenceId')?.trim() ?? null;
  const sourceLabel = params.get('sourceLabel')?.trim() ?? null;
  const originalFilename = params.get('originalFilename')?.trim() ?? null;
  const mimeType = params.get('mimeType')?.trim() ?? null;
  const error = params.get('error')?.trim()?.toLowerCase() ?? null;

  if (assetKind === 'DOCUMENT' || assetKind === 'IMAGE') {
    return {
      assetKind,
      referenceId,
      sourceLabel,
      originalFilename,
      mimeType,
      invalid: !referenceId,
      unsupported: false,
    };
  }

  return {
    assetKind: 'DOCUMENT',
    referenceId: null,
    sourceLabel,
    originalFilename,
    mimeType,
    invalid: error === 'invalid',
    unsupported: error === 'unsupported',
  };
}
