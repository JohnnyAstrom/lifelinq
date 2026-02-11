package app.lifelinq.features.household.application;

import app.lifelinq.features.household.domain.Invitation;
import app.lifelinq.features.household.domain.InvitationRepository;

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
        if (command.getToken() == null || command.getToken().isBlank()) {
            throw new IllegalArgumentException("token must not be blank");
        }
        if (command.getNow() == null) {
            throw new IllegalArgumentException("now must not be null");
        }

        Invitation invitation = invitationRepository.findByToken(command.getToken()).orElse(null);
        if (invitation == null) {
            return new RevokeInvitationResult(false);
        }

        boolean revoked = invitation.revoke(command.getNow());
        if (revoked) {
            invitationRepository.save(invitation);
        }

        return new RevokeInvitationResult(revoked);
    }
}
