package app.lifelinq.features.economy.application;

import app.lifelinq.features.economy.contract.InitializeGroupEconomyPort;
import app.lifelinq.features.economy.domain.PeriodParticipant;
import app.lifelinq.features.economy.domain.SettlementPeriod;
import app.lifelinq.features.economy.domain.SettlementPeriodRepository;
import app.lifelinq.features.group.contract.GroupMembershipReadPort;
import java.time.Clock;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class InitializeGroupEconomyUseCase implements InitializeGroupEconomyPort {
    private final SettlementPeriodRepository settlementPeriodRepository;
    private final GroupMembershipReadPort groupMembershipReadPort;
    private final Clock clock;

    public InitializeGroupEconomyUseCase(
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

    @Override
    public void initializeGroupEconomy(UUID groupId) {
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        if (settlementPeriodRepository.existsOpenByGroupId(groupId)) {
            return;
        }
        List<UUID> memberUserIds = groupMembershipReadPort.listMemberUserIds(groupId);
        if (memberUserIds.isEmpty()) {
            throw new IllegalStateException("cannot initialize economy period without participants");
        }
        Set<PeriodParticipant> participants = new LinkedHashSet<>();
        for (UUID memberUserId : memberUserIds) {
            participants.add(new PeriodParticipant(memberUserId));
        }
        SettlementPeriod period = SettlementPeriod.createInitialOpenPeriod(
                groupId,
                clock.instant(),
                participants
        );
        try {
            settlementPeriodRepository.save(period);
        } catch (RuntimeException ex) {
            // Idempotency guard for concurrent initializers where a unique OPEN-period constraint may win first.
            if (settlementPeriodRepository.existsOpenByGroupId(groupId)) {
                return;
            }
            throw ex;
        }
    }
}
