import { getRefreshToken, getToken, setAuthTokens } from './tokenStore';
import { getApiBaseUrl } from './apiConfig';

export type ApiClientOptions = {
  token?: string | null;
  suppressErrorLoggingStatuses?: number[];
};

let refreshInFlight: Promise<string | null> | null = null;
let authSessionVersion = 0;

export class ApiError extends Error {
  status: number;
  body: string;

  constructor(status: number, body: string) {
    super(`HTTP ${status}: ${body}`);
    this.status = status;
    this.body = body;
  }
}

export class UnauthorizedError extends ApiError {
  constructor(body = 'Unauthorized') {
    super(401, body);
  }
}

export function invalidateAuthSession(): void {
  authSessionVersion += 1;
}

export function formatApiError(err: unknown): string {
  if (err instanceof UnauthorizedError) {
    return 'You must create a group first or log in again.';
  }
  if (err instanceof ApiError) {
    if (err.status === 403) {
      return 'Access denied.';
    }
    if (err.status === 409) {
      return 'Conflict: action not allowed.';
    }
    return err.message;
  }
  if (err instanceof Error) {
    return err.message;
  }
  return 'Unknown error';
}

export async function fetchJson<T>(
  path: string,
  options: RequestInit = {},
  clientOptions: ApiClientOptions = {}
): Promise<T> {
  const baseUrl = getApiBaseUrl();
  const headers: Record<string, string> = {
    Accept: 'application/json',
    ...(options.headers as Record<string, string> | undefined),
  };

  if (options.body && !headers['Content-Type']) {
    headers['Content-Type'] = 'application/json';
  }

  const token = clientOptions.token ?? (await getToken());
  const suppressErrorLoggingStatuses = clientOptions.suppressErrorLoggingStatuses ?? [];
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${baseUrl}${path}`, {
    ...options,
    headers,
  });

  if (response.status === 401) {
    const refreshedAccessToken = await tryRefreshAccessToken(baseUrl, path);
    if (!refreshedAccessToken) {
      const text = await response.text();
      throw new UnauthorizedError(text || 'Unauthorized');
    }
    headers.Authorization = `Bearer ${refreshedAccessToken}`;
    const retriedResponse = await fetch(`${baseUrl}${path}`, {
      ...options,
      headers,
    });
    if (retriedResponse.status === 401) {
      const text = await retriedResponse.text();
      throw new UnauthorizedError(text || 'Unauthorized');
    }
    if (!retriedResponse.ok) {
      const text = await retriedResponse.text();
      const error = new ApiError(retriedResponse.status, text);
      if (!suppressErrorLoggingStatuses.includes(retriedResponse.status)) {
        console.error('API error', {
          url: `${baseUrl}${path}`,
          status: retriedResponse.status,
          body: text,
        });
      }
      throw error;
    }
    if (retriedResponse.status === 204) {
      return undefined as T;
    }
    return (await retriedResponse.json()) as T;
  }

  if (!response.ok) {
    const text = await response.text();
    const error = new ApiError(response.status, text);
    if (!suppressErrorLoggingStatuses.includes(response.status)) {
      // Dev-friendly logging to surface API failures during debugging.
      console.error('API error', {
        url: `${baseUrl}${path}`,
        status: response.status,
        body: text,
      });
    }
    throw error;
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}

export async function fetchText(
  path: string,
  options: RequestInit = {},
  clientOptions: ApiClientOptions = {}
): Promise<string> {
  const baseUrl = getApiBaseUrl();
  const headers: Record<string, string> = {
    Accept: 'text/plain, text/html, */*',
    ...(options.headers as Record<string, string> | undefined),
  };

  if (options.body && !headers['Content-Type']) {
    headers['Content-Type'] = 'application/json';
  }

  const token = clientOptions.token ?? (await getToken());
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${baseUrl}${path}`, {
    ...options,
    headers,
  });

  if (response.status === 401) {
    const refreshedAccessToken = await tryRefreshAccessToken(baseUrl, path);
    if (!refreshedAccessToken) {
      const text = await response.text();
      throw new UnauthorizedError(text || 'Unauthorized');
    }
    headers.Authorization = `Bearer ${refreshedAccessToken}`;
    const retriedResponse = await fetch(`${baseUrl}${path}`, {
      ...options,
      headers,
    });
    if (retriedResponse.status === 401) {
      const text = await retriedResponse.text();
      throw new UnauthorizedError(text || 'Unauthorized');
    }
    if (!retriedResponse.ok) {
      const text = await retriedResponse.text();
      const error = new ApiError(retriedResponse.status, text);
      console.error('API error', {
        url: `${baseUrl}${path}`,
        status: retriedResponse.status,
        body: text,
      });
      throw error;
    }
    return await retriedResponse.text();
  }

  if (!response.ok) {
    const text = await response.text();
    const error = new ApiError(response.status, text);
    console.error('API error', {
      url: `${baseUrl}${path}`,
      status: response.status,
      body: text,
    });
    throw error;
  }

  return await response.text();
}

async function tryRefreshAccessToken(baseUrl: string, path: string): Promise<string | null> {
  if (path === '/auth/refresh') {
    return null;
  }
  if (refreshInFlight) {
    return refreshInFlight;
  }
  const refreshVersion = authSessionVersion;
  refreshInFlight = runRefreshAccessToken(baseUrl, refreshVersion);
  try {
    return await refreshInFlight;
  } finally {
    refreshInFlight = null;
  }
}

async function runRefreshAccessToken(baseUrl: string, refreshVersion: number): Promise<string | null> {
  try {
    const refreshToken = await getRefreshToken();
    if (!refreshToken) {
      return null;
    }
    const refreshResponse = await fetch(`${baseUrl}/auth/refresh`, {
      method: 'POST',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ refreshToken }),
    });
    if (!refreshResponse.ok) {
      return null;
    }
    const payload = (await refreshResponse.json()) as {
      accessToken?: string;
      refreshToken?: string;
    };
    const accessToken = payload.accessToken?.trim();
    const rotatedRefreshToken = payload.refreshToken?.trim();
    if (!accessToken || !rotatedRefreshToken) {
      return null;
    }
    if (refreshVersion !== authSessionVersion) {
      return null;
    }
    await setAuthTokens(accessToken, rotatedRefreshToken);
    return accessToken;
  } catch {
    return null;
  }
}

export async function revokeRefreshSession(accessToken: string, refreshToken: string): Promise<void> {
  const baseUrl = getApiBaseUrl();
  await fetch(`${baseUrl}/auth/logout`, {
    method: 'POST',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
      Authorization: `Bearer ${accessToken}`,
    },
    body: JSON.stringify({ refreshToken }),
  });
}
