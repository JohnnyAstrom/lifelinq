import { fetchJson } from '../../../shared/api/client';

export async function startMagicLink(email: string): Promise<void> {
  await fetchJson<void>(
    '/auth/magic/start',
    {
      method: 'POST',
      body: JSON.stringify({ email }),
    },
    {}
  );
}

