import { fetchJson } from '../../../shared/api/client';

export type MeResponse = {
  userId: string;
  householdId: string | null;
};

export async function fetchMe(token: string): Promise<MeResponse> {
  return fetchJson<MeResponse>('/me', {}, { token });
}
