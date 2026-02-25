package app.lifelinq.features.group.application;

import app.lifelinq.features.group.domain.Invitation;
import app.lifelinq.features.group.domain.InvitationRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

final class CreateInvitationUseCase {
    private static final int MAX_TOKEN_ATTEMPTS = 5;

    private final InvitationRepository invitationRepository;
    private final InvitationTokenGenerator tokenGenerator;

    public CreateInvitationUseCase(
            InvitationRepository invitationRepository,
            InvitationTokenGenerator tokenGenerator
    ) {
        if (invitationRepository == null) {
            throw new IllegalArgumentException("invitationRepository must not be null");
        }
        if (tokenGenerator == null) {
            throw new IllegalArgumentException("tokenGenerator must not be null");
        }
        this.invitationRepository = invitationRepository;
        this.tokenGenerator = tokenGenerator;
    }

    public CreateInvitationResult execute(CreateInvitationCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        if (command.getGroupId() == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        if (command.getInviteeEmail() == null || command.getInviteeEmail().isBlank()) {
            throw new IllegalArgumentException("inviteeEmail must not be blank");
        }
        if (command.getNow() == null) {
            throw new IllegalArgumentException("now must not be null");
        }
        Duration ttl = command.getTtl();
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("ttl must be positive");
        }

        Instant expiresAt = command.getNow().plus(ttl);
        invitationRepository.findActiveByGroupIdAndInviteeEmail(
                        command.getGroupId(),
                        command.getInviteeEmail()
                )
                .filter(existing -> existing.isActive(command.getNow()))
                .ifPresent(existing -> {
                    throw new IllegalStateException("active invitation already exists");
                });
        String token = generateUniqueToken();
        Invitation invitation = Invitation.createActive(
                UUID.randomUUID(),
                command.getGroupId(),
                command.getInviteeEmail(),
                token,
                expiresAt
        );

        invitationRepository.save(invitation);

        return new CreateInvitationResult(invitation.getId(), invitation.getToken(), invitation.getExpiresAt());
    }

    private String generateUniqueToken() {
        for (int attempt = 0; attempt < MAX_TOKEN_ATTEMPTS; attempt++) {
            String token = tokenGenerator.generate();
            if (token == null || token.isBlank()) {
                throw new IllegalArgumentException("generated token must not be blank");
            }
            if (!invitationRepository.existsByToken(token)) {
                return token;
            }
        }
        throw new IllegalStateException("could not generate a unique token");
    }
}
