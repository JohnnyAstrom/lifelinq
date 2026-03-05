import { fetchJson } from '../../../shared/api/client';

export type CreateGroupResponse = {
  groupId: string;
};

export type CreateInvitationResponse = {
  invitationId: string;
  token: string;
  shortCode: string;
  expiresAt: string;
};

export type AcceptInvitationResponse = {
  groupId: string;
  userId: string;
};

export type ResolveInvitationCodeResponse = {
  invitationId: string;
  groupId: string;
  token: string;
  type: 'EMAIL' | 'LINK';
  status: 'ACTIVE' | 'REVOKED';
  expiresAt: string;
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

export async function createInvitationByEmail(
  token: string,
  email: string,
  ttlSeconds?: number
): Promise<CreateInvitationResponse> {
  return fetchJson<CreateInvitationResponse>(
    '/groups/invitations',
    {
      method: 'POST',
      body: JSON.stringify({
        email,
        ...(ttlSeconds == null ? {} : { ttlSeconds }),
      }),
    },
    { token }
  );
}

export async function getActiveInvitationLink(
  token: string,
  groupId: string
): Promise<CreateInvitationResponse | null> {
  const response = await fetchJson<{
    invitationId: string | null;
    token: string | null;
    shortCode: string | null;
    expiresAt: string | null;
  }>(
    `/groups/${encodeURIComponent(groupId)}/invitations/active`,
    {
      method: 'GET',
    },
    { token }
  );
  if (!response.invitationId || !response.token || !response.shortCode || !response.expiresAt) {
    return null;
  }
  return {
    invitationId: response.invitationId,
    token: response.token,
    shortCode: response.shortCode,
    expiresAt: response.expiresAt,
  };
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

export async function resolveInvitationCode(
  token: string,
  code: string
): Promise<ResolveInvitationCodeResponse> {
  return fetchJson<ResolveInvitationCodeResponse>(
    '/groups/invitations/resolve-code',
    {
      method: 'POST',
      body: JSON.stringify({ code }),
    },
    { token }
  );
}

export async function revokeInvitation(token: string, invitationId: string): Promise<void> {
  await fetchJson<void>(
    `/groups/invitations/${invitationId}`,
    {
      method: 'DELETE',
    },
    { token }
  );
}
