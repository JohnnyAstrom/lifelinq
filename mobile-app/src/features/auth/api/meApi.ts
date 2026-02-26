import { fetchJson } from '../../../shared/api/client';

export type MeMembership = {
  groupId: string;
  role: 'ADMIN' | 'MEMBER';
};

export type MeResponse = {
  userId: string;
  activeGroupId: string | null;
  memberships: MeMembership[];
};

export async function fetchMe(token: string): Promise<MeResponse> {
  return fetchJson<MeResponse>('/me', {}, { token });
}

export async function setActiveGroup(token: string, groupId: string): Promise<void> {
  await fetchJson<void>(
    '/me/active-group',
    {
      method: 'PUT',
      body: JSON.stringify({ activeGroupId: groupId }),
    },
    { token }
  );
}
