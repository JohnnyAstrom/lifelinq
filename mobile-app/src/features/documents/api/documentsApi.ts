import { fetchJson } from '../../../shared/api/client';

export type DocumentItemResponse = {
  id: string;
  groupId: string;
  createdByUserId: string;
  title: string | null;
  notes: string | null;
  date: string | null;
  category: string | null;
  tags: string[];
  externalLink: string | null;
  createdAt: string;
};

export type CreateDocumentRequest = {
  title?: string | null;
  notes?: string | null;
  date?: string | null;
  category?: string | null;
  tags?: string[] | null;
  externalLink?: string | null;
};

type CreateDocumentResponse = {
  documentId: string;
};

type ListDocumentsResponse = {
  items: DocumentItemResponse[];
};

export async function createDocument(
  token: string,
  payload: CreateDocumentRequest
): Promise<string> {
  const response = await fetchJson<CreateDocumentResponse>(
    '/documents',
    {
      method: 'POST',
      body: JSON.stringify(payload),
    },
    { token }
  );
  return response.documentId;
}

export async function listDocuments(
  token: string,
  q?: string | null
): Promise<DocumentItemResponse[]> {
  const trimmed = q?.trim() ?? '';
  const query = trimmed ? `?q=${encodeURIComponent(trimmed)}` : '';
  const response = await fetchJson<ListDocumentsResponse>(`/documents${query}`, {}, { token });
  return response.items;
}

export async function deleteDocument(
  token: string,
  documentId: string
): Promise<void> {
  await fetchJson<void>(
    `/documents/${encodeURIComponent(documentId)}`,
    { method: 'DELETE' },
    { token }
  );
}
