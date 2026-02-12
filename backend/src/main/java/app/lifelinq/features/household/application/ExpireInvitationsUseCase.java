package app.lifelinq.features.household.application;

import app.lifelinq.features.household.domain.Invitation;
import app.lifelinq.features.household.domain.InvitationRepository;
import java.time.Instant;
import java.util.List;

final class ExpireInvitationsUseCase {
    private final InvitationRepository invitationRepository;

    public ExpireInvitationsUseCase(InvitationRepository invitationRepository) {
        if (invitationRepository == null) {
            throw new IllegalArgumentException("invitationRepository must not be null");
        }
        this.invitationRepository = invitationRepository;
    }

    public ExpireInvitationsResult execute(ExpireInvitationsCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        Instant now = command.getNow();
        if (now == null) {
            throw new IllegalArgumentException("now must not be null");
        }

        List<Invitation> active = invitationRepository.findActive();
        int expiredCount = 0;
        for (Invitation invitation : active) {
            if (invitation == null) {
                continue;
            }
            if (invitation.isExpired(now)) {
                expiredCount++;
            }
        }

        return new ExpireInvitationsResult(expiredCount);
    }
}
