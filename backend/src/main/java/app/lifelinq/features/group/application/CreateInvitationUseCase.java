package app.lifelinq.features.group.application;

import app.lifelinq.features.group.domain.Invitation;
import app.lifelinq.features.group.domain.InvitationRepository;
import app.lifelinq.features.group.domain.InvitationType;
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
        if (command.getType() == null) {
            throw new IllegalArgumentException("type must not be null");
        }
        if (command.getType() == InvitationType.EMAIL
                && (command.getInviteeEmail() == null || command.getInviteeEmail().isBlank())) {
            throw new IllegalArgumentException("inviteeEmail must not be blank");
        }
        if (command.getType() == InvitationType.LINK && command.getInviteeEmail() != null) {
            throw new IllegalArgumentException("inviteeEmail must be null for LINK invitations");
        }
        if (command.getNow() == null) {
            throw new IllegalArgumentException("now must not be null");
        }
        Duration ttl = command.getTtl();
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("ttl must be positive");
        }
        if (command.getMaxUses() <= 0) {
            throw new IllegalArgumentException("maxUses must be > 0");
        }

        Instant expiresAt = command.getNow().plus(ttl);
        if (command.getType() == InvitationType.EMAIL) {
            invitationRepository.findActiveByGroupIdAndInviteeEmail(
                            command.getGroupId(),
                            command.getInviteeEmail()
                    )
                    .filter(existing -> existing.isAcceptAllowed(command.getNow()))
                    .ifPresent(existing -> {
                        throw new IllegalStateException("active invitation already exists");
                    });
        } else {
            Invitation activeLink = findActiveLink(command.getGroupId(), command.getNow());
            if (activeLink != null) {
                return new CreateInvitationResult(activeLink.getId(), activeLink.getToken(), activeLink.getExpiresAt());
            }
        }
        String token = generateUniqueToken();
        Invitation invitation = Invitation.createActive(
                UUID.randomUUID(),
                command.getGroupId(),
                command.getType(),
                command.getInviteeEmail(),
                command.getInviterDisplayName(),
                token,
                expiresAt,
                command.getMaxUses()
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

    private Invitation findActiveLink(UUID groupId, Instant now) {
        for (Invitation invitation : invitationRepository.findActive()) {
            if (invitation.getType() == InvitationType.LINK
                    && groupId.equals(invitation.getGroupId())
                    && invitation.isAcceptAllowed(now)) {
                return invitation;
            }
        }
        return null;
    }
}
