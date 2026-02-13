import Constants from 'expo-constants';
import { Platform } from 'react-native';
import { getToken } from './tokenStore';

export type ApiClientOptions = {
  token?: string | null;
};

const DEFAULT_BASE_URL = 'http://localhost:8080';

function trimTrailingSlash(value: string): string {
  return value.replace(/\/+$/, '');
}

function getExpoDevHost(): string | null {
  const hostUriFromExpoConfig = (Constants.expoConfig as { hostUri?: string } | null)?.hostUri;
  if (hostUriFromExpoConfig) {
    return hostUriFromExpoConfig.split(':')[0] ?? null;
  }

  const hostUriFromManifest = (
    Constants.manifest2 as { extra?: { expoClient?: { hostUri?: string } } } | null
  )?.extra?.expoClient?.hostUri;
  if (hostUriFromManifest) {
    return hostUriFromManifest.split(':')[0] ?? null;
  }

  return null;
}

function resolveBaseUrl(): string {
  const configured = process.env.EXPO_PUBLIC_API_BASE_URL?.trim();
  if (configured) {
    return trimTrailingSlash(configured);
  }

  if (Platform.OS === 'android') {
    const devHost = getExpoDevHost();
    if (devHost) {
      return `http://${devHost}:8080`;
    }
    return 'http://10.0.2.2:8080';
  }

  return DEFAULT_BASE_URL;
}

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

export function formatApiError(err: unknown): string {
  if (err instanceof UnauthorizedError) {
    return 'You must create a household first or log in again.';
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
  const baseUrl = resolveBaseUrl();
  const headers: Record<string, string> = {
    Accept: 'application/json',
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
    const text = await response.text();
    throw new UnauthorizedError(text || 'Unauthorized');
  }

  if (!response.ok) {
    const text = await response.text();
    const error = new ApiError(response.status, text);
    // Dev-friendly logging to surface API failures during debugging.
    console.error('API error', {
      url: `${baseUrl}${path}`,
      status: response.status,
      body: text,
    });
    throw error;
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}
