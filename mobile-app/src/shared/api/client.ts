import { getToken } from './tokenStore';

export type ApiClientOptions = {
  token?: string | null;
};

const DEFAULT_BASE_URL = 'http://localhost:8080';

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
  const baseUrl = process.env.EXPO_PUBLIC_API_BASE_URL || DEFAULT_BASE_URL;
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
