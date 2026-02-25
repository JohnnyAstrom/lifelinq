package app.lifelinq.features.group.application;

import app.lifelinq.features.group.domain.Invitation;
import app.lifelinq.features.group.domain.InvitationRepository;

final class RevokeInvitationUseCase {
    private final InvitationRepository invitationRepository;

    public RevokeInvitationUseCase(InvitationRepository invitationRepository) {
        if (invitationRepository == null) {
            throw new IllegalArgumentException("invitationRepository must not be null");
        }
        this.invitationRepository = invitationRepository;
    }

    public RevokeInvitationResult execute(RevokeInvitationCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        if (command.getInvitationId() == null) {
            throw new IllegalArgumentException("invitationId must not be null");
        }
        if (command.getNow() == null) {
            throw new IllegalArgumentException("now must not be null");
        }

        Invitation invitation = invitationRepository.findById(command.getInvitationId()).orElse(null);
        if (invitation == null) {
            return new RevokeInvitationResult(false);
        }

        invitation.revoke();
        invitationRepository.save(invitation);
        return new RevokeInvitationResult(true);
    }
}
