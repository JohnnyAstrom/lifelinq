export type ApiClientOptions = {
  token?: string | null;
};

const DEFAULT_BASE_URL = 'http://localhost:8080';

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

  if (clientOptions.token) {
    headers.Authorization = `Bearer ${clientOptions.token}`;
  }

  const response = await fetch(`${baseUrl}${path}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    const text = await response.text();
    throw new Error(`HTTP ${response.status}: ${text}`);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}
