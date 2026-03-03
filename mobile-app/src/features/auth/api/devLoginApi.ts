import { fetchJson } from '../../../shared/api/client';

export type DevLoginResponse = {
  accessToken: string;
  refreshToken: string;
};

export async function devLogin(
  email: string,
  initialPlaceName?: string | null
): Promise<DevLoginResponse> {
  return fetchJson<DevLoginResponse>(
    '/auth/dev-login',
    {
      method: 'POST',
      body: JSON.stringify({ email, initialPlaceName: initialPlaceName ?? null }),
    },
    {}
  );
}
