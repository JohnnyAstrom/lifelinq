package app.lifelinq.features.household.application;

import app.lifelinq.features.household.domain.Invitation;
import app.lifelinq.features.household.domain.InvitationRepository;
import app.lifelinq.features.household.domain.InvitationStatus;
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

        List<Invitation> pending = invitationRepository.findPending();
        int expiredCount = 0;
        for (Invitation invitation : pending) {
            if (invitation == null) {
                continue;
            }
            InvitationStatus before = invitation.getStatus();
            invitation.expire(now);
            if (before != invitation.getStatus()) {
                invitationRepository.save(invitation);
                expiredCount++;
            }
        }

        return new ExpireInvitationsResult(expiredCount);
    }
}
