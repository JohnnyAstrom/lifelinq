import { fetchJson } from '../../../shared/api/client';

export type CreateHouseholdResponse = {
  householdId: string;
};

export async function createHousehold(
  token: string,
  name: string
): Promise<CreateHouseholdResponse> {
  return fetchJson<CreateHouseholdResponse>(
    '/households',
    {
      method: 'POST',
      body: JSON.stringify({ name }),
    },
    { token }
  );
}
