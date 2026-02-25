import { fetchJson } from '../../../shared/api/client';

export type CreateGroupResponse = {
  groupId: string;
};

export async function createGroup(
  token: string,
  name: string
): Promise<CreateGroupResponse> {
  return fetchJson<CreateGroupResponse>(
    '/groups',
    {
      method: 'POST',
      body: JSON.stringify({ name }),
    },
    { token }
  );
}
