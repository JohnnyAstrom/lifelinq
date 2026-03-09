package app.lifelinq.features.economy.application;

import app.lifelinq.features.economy.domain.PeriodParticipant;
import app.lifelinq.features.economy.domain.SettlementPeriod;
import app.lifelinq.features.economy.domain.SettlementPeriodRepository;
import app.lifelinq.features.group.contract.GroupMembershipReadPort;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class CloseSettlementPeriodUseCase {
    private final SettlementPeriodRepository settlementPeriodRepository;
    private final GroupMembershipReadPort groupMembershipReadPort;
    private final Clock clock;

    public CloseSettlementPeriodUseCase(
            SettlementPeriodRepository settlementPeriodRepository,
            GroupMembershipReadPort groupMembershipReadPort,
            Clock clock
    ) {
        if (settlementPeriodRepository == null) {
            throw new IllegalArgumentException("settlementPeriodRepository must not be null");
        }
        if (groupMembershipReadPort == null) {
            throw new IllegalArgumentException("groupMembershipReadPort must not be null");
        }
        if (clock == null) {
            throw new IllegalArgumentException("clock must not be null");
        }
        this.settlementPeriodRepository = settlementPeriodRepository;
        this.groupMembershipReadPort = groupMembershipReadPort;
        this.clock = clock;
    }

    @Transactional
    public CloseSettlementPeriodResult execute(UUID groupId) {
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        SettlementPeriod activePeriod = settlementPeriodRepository.findOpenByGroupId(groupId)
                .orElseThrow(() -> new IllegalStateException("no OPEN settlement period for group"));
        Instant now = clock.instant();

        List<UUID> memberUserIds = groupMembershipReadPort.listMemberUserIds(groupId);
        if (memberUserIds.isEmpty()) {
            throw new IllegalStateException("cannot open new settlement period without participants");
        }
        Set<PeriodParticipant> newParticipants = new LinkedHashSet<>();
        for (UUID memberUserId : memberUserIds) {
            newParticipants.add(new PeriodParticipant(memberUserId));
        }

        SettlementPeriod closedPeriod = activePeriod.close(now);
        SettlementPeriod newOpenPeriod = SettlementPeriod.createOpenPeriod(
                groupId,
                now,
                activePeriod.getStrategySnapshot(),
                newParticipants
        );

        settlementPeriodRepository.save(closedPeriod);
        settlementPeriodRepository.flush();
        settlementPeriodRepository.save(newOpenPeriod);
        return new CloseSettlementPeriodResult(closedPeriod.getId(), newOpenPeriod.getId());
    }
}
