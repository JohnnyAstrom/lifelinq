import { fetchJson } from '../../../shared/api/client';

export type DevLoginResponse = {
  token: string;
};

export async function devLogin(email: string): Promise<DevLoginResponse> {
  return fetchJson<DevLoginResponse>(
    '/auth/dev-login',
    {
      method: 'POST',
      body: JSON.stringify({ email }),
    },
    {}
  );
}
