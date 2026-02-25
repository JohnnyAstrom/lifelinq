import { fetchJson } from '../../../shared/api/client';

export type MemberItemResponse = {
  userId: string;
  role: 'ADMIN' | 'MEMBER';
};

type ListMembersResponse = {
  members: MemberItemResponse[];
};

export async function fetchMembers(token: string): Promise<MemberItemResponse[]> {
  const response = await fetchJson<ListMembersResponse>('/groups/members', {}, { token });
  return response.members;
}

export async function addMember(token: string, userId: string): Promise<MemberItemResponse> {
  return fetchJson<MemberItemResponse>(
    '/groups/members',
    {
      method: 'POST',
      body: JSON.stringify({ userId }),
    },
    { token }
  );
}

export async function removeMember(token: string, userId: string): Promise<boolean> {
  const response = await fetchJson<{ removed: boolean }>(
    '/groups/members/remove',
    {
      method: 'POST',
      body: JSON.stringify({ userId }),
    },
    { token }
  );
  return response.removed;
}
