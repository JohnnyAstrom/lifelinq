import { fetchJson } from '../../../shared/api/client';

export type SettlementPeriodStatus = 'OPEN' | 'CLOSED';
export type SettlementStrategyType = 'EQUAL_COST' | 'PERCENTAGE_COST';

export type ActiveSettlementPeriodResponse = {
  periodId: string;
  groupId: string;
  startDate: string;
  endDate: string | null;
  status: SettlementPeriodStatus;
  strategyType: SettlementStrategyType;
  percentageShares: Record<string, number>;
  participantUserIds: string[];
};

export type SettlementTransactionResponse = {
  transactionId: string;
  periodId: string;
  amount: number;
  description: string | null;
  createdByUserId: string;
  paidByUserId: string;
  createdAt: string;
  deletedAt: string | null;
  category: string | null;
};

export type CalculateSettlementResponse = {
  balances: Array<{
    userId: string;
    amount: number;
  }>;
  recommendedPayments: Array<{
    fromUserId: string;
    toUserId: string;
    amount: number;
  }>;
};

export type CloseSettlementPeriodResponse = {
  closedPeriodId: string;
  newOpenPeriodId: string;
};

export type CreateSettlementTransactionRequest = {
  amount: number;
  description?: string | null;
  paidByUserId: string;
  category?: string | null;
};

export type UpdateSettlementStrategyRequest = {
  strategyType: SettlementStrategyType;
  percentageShares?: Record<string, number> | null;
};

function requireToken(token: string): string {
  const value = token?.trim();
  if (!value) {
    throw new Error('Missing token');
  }
  return value;
}

export async function getActiveSettlementPeriod(
  token: string
): Promise<ActiveSettlementPeriodResponse> {
  return fetchJson<ActiveSettlementPeriodResponse>(
    '/economy/periods/active',
    { method: 'GET' },
    { token: requireToken(token) }
  );
}

export async function closeSettlementPeriod(
  token: string
): Promise<CloseSettlementPeriodResponse> {
  return fetchJson<CloseSettlementPeriodResponse>(
    '/economy/periods/close',
    {
      method: 'POST',
      body: JSON.stringify({}),
    },
    { token: requireToken(token) }
  );
}

export async function listSettlementTransactions(
  token: string,
  periodId: string
): Promise<SettlementTransactionResponse[]> {
  return fetchJson<SettlementTransactionResponse[]>(
    `/economy/periods/${encodeURIComponent(periodId)}/transactions`,
    { method: 'GET' },
    { token: requireToken(token) }
  );
}

export async function createSettlementTransaction(
  token: string,
  periodId: string,
  request: CreateSettlementTransactionRequest
): Promise<SettlementTransactionResponse> {
  return fetchJson<SettlementTransactionResponse>(
    `/economy/periods/${encodeURIComponent(periodId)}/transactions`,
    {
      method: 'POST',
      body: JSON.stringify(request),
    },
    { token: requireToken(token) }
  );
}

export async function deleteSettlementTransaction(
  token: string,
  transactionId: string
): Promise<void> {
  await fetchJson<void>(
    `/economy/transactions/${encodeURIComponent(transactionId)}`,
    { method: 'DELETE' },
    { token: requireToken(token) }
  );
}

export async function calculateSettlement(
  token: string,
  periodId: string
): Promise<CalculateSettlementResponse> {
  return fetchJson<CalculateSettlementResponse>(
    `/economy/periods/${encodeURIComponent(periodId)}/settlement`,
    { method: 'GET' },
    { token: requireToken(token) }
  );
}

export async function updateSettlementStrategy(
  token: string,
  periodId: string,
  request: UpdateSettlementStrategyRequest
): Promise<void> {
  await fetchJson<void>(
    `/economy/periods/${encodeURIComponent(periodId)}/strategy`,
    {
      method: 'PATCH',
      body: JSON.stringify(request),
    },
    { token: requireToken(token) }
  );
}
