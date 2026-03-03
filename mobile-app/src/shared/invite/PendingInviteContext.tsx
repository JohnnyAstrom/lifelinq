import React, { createContext, useContext, useMemo, useState } from 'react';

type PendingInviteState = {
  pendingInviteToken: string | null;
  setPendingInviteToken: (token: string) => void;
  clearPendingInviteToken: () => void;
};

const PendingInviteContext = createContext<PendingInviteState | null>(null);

export function PendingInviteProvider({ children }: { children: React.ReactNode }) {
  const [pendingInviteToken, setPendingInviteTokenState] = useState<string | null>(null);

  const value = useMemo<PendingInviteState>(
    () => ({
      pendingInviteToken,
      setPendingInviteToken: (token: string) => {
        const normalized = token.trim();
        if (!normalized) {
          return;
        }
        setPendingInviteTokenState(normalized);
      },
      clearPendingInviteToken: () => {
        setPendingInviteTokenState(null);
      },
    }),
    [pendingInviteToken]
  );

  return <PendingInviteContext.Provider value={value}>{children}</PendingInviteContext.Provider>;
}

export function usePendingInvite() {
  const ctx = useContext(PendingInviteContext);
  if (!ctx) {
    throw new Error('usePendingInvite must be used within PendingInviteProvider');
  }
  return ctx;
}

