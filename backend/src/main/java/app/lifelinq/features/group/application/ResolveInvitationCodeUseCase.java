package app.lifelinq.features.group.application;

import app.lifelinq.features.group.domain.Invitation;
import app.lifelinq.features.group.domain.InvitationRepository;
import java.util.Locale;
import java.util.Optional;

final class ResolveInvitationCodeUseCase {
    private final InvitationRepository invitationRepository;

    ResolveInvitationCodeUseCase(InvitationRepository invitationRepository) {
        if (invitationRepository == null) {
            throw new IllegalArgumentException("invitationRepository must not be null");
        }
        this.invitationRepository = invitationRepository;
    }

    Optional<Invitation> execute(ResolveInvitationCodeCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        if (command.getCode() == null || command.getCode().isBlank()) {
            throw new IllegalArgumentException("code must not be blank");
        }

        String normalized = command.getCode().trim().toUpperCase(Locale.ROOT);
        return invitationRepository.findByShortCode(normalized);
    }
}
