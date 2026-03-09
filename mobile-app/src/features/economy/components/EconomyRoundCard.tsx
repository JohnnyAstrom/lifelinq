import { SettlementPeriodHeader } from './SettlementPeriodHeader';
import { type ActiveSettlementPeriodResponse } from '../api/economyApi';
import { AppButton, AppCard, SectionTitle, Subtle } from '../../../shared/ui/components';

type Props = {
  period: ActiveSettlementPeriodResponse | null;
  showPreviousPeriodClosed: boolean;
  resolveUserName: (userId: string) => string;
  onCloseRound: () => void;
};

export function EconomyRoundCard({
  period,
  showPreviousPeriodClosed,
  resolveUserName,
  onCloseRound,
}: Props) {
  return (
    <AppCard>
      <SectionTitle>Round</SectionTitle>
      {period ? (
        <>
          <SettlementPeriodHeader
            period={period}
            showPreviousPeriodClosed={showPreviousPeriodClosed}
            resolveUserName={resolveUserName}
          />
          <AppButton
            title="Start new settlement round"
            onPress={onCloseRound}
            variant="ghost"
            fullWidth
          />
        </>
      ) : (
        <Subtle>No active period.</Subtle>
      )}
    </AppCard>
  );
}

