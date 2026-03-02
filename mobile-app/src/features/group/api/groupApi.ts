import { fetchJson } from '../../../shared/api/client';

export type CreateGroupResponse = {
  groupId: string;
};

export type CreateInvitationResponse = {
  invitationId: string;
  token: string;
  expiresAt: string;
};

export type AcceptInvitationResponse = {
  groupId: string;
  userId: string;
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

export async function renameCurrentPlace(token: string, name: string): Promise<void> {
  await fetchJson<void>(
    '/me/place',
    {
      method: 'PATCH',
      body: JSON.stringify({ name }),
    },
    { token }
  );
}

export async function leaveCurrentPlace(token: string): Promise<void> {
  await fetchJson<void>(
    '/me/place/leave',
    {
      method: 'POST',
    },
    { token }
  );
}

export async function deleteCurrentPlace(token: string): Promise<void> {
  await fetchJson<void>(
    '/me/place',
    {
      method: 'DELETE',
    },
    { token }
  );
}

export async function createInvitationLink(token: string): Promise<CreateInvitationResponse> {
  return fetchJson<CreateInvitationResponse>(
    '/groups/invitations/link',
    {
      method: 'POST',
      body: JSON.stringify({}),
    },
    { token }
  );
}

export async function acceptInvitation(
  token: string,
  invitationToken: string
): Promise<AcceptInvitationResponse> {
  return fetchJson<AcceptInvitationResponse>(
    '/groups/invitations/accept',
    {
      method: 'POST',
      body: JSON.stringify({ token: invitationToken }),
    },
    { token }
  );
}
