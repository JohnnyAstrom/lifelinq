package app.lifelinq.features.group.application;

import app.lifelinq.features.group.domain.Invitation;
import app.lifelinq.features.group.domain.InvitationRepository;
import app.lifelinq.features.group.domain.InvitationType;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

final class CreateInvitationUseCase {
    private static final int MAX_TOKEN_ATTEMPTS = 5;
    private static final int MAX_SHORT_CODE_ATTEMPTS = 10;

    private final InvitationRepository invitationRepository;
    private final InvitationTokenGenerator tokenGenerator;
    private final InvitationShortCodeGenerator shortCodeGenerator;

    public CreateInvitationUseCase(
            InvitationRepository invitationRepository,
            InvitationTokenGenerator tokenGenerator,
            InvitationShortCodeGenerator shortCodeGenerator
    ) {
        if (invitationRepository == null) {
            throw new IllegalArgumentException("invitationRepository must not be null");
        }
        if (tokenGenerator == null) {
            throw new IllegalArgumentException("tokenGenerator must not be null");
        }
        if (shortCodeGenerator == null) {
            throw new IllegalArgumentException("shortCodeGenerator must not be null");
        }
        this.invitationRepository = invitationRepository;
        this.tokenGenerator = tokenGenerator;
        this.shortCodeGenerator = shortCodeGenerator;
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
        if (command.getType() == InvitationType.EMAIL) {
            if (command.getMaxUses() == null || command.getMaxUses() != 1) {
                throw new IllegalArgumentException("EMAIL invitations must use maxUses = 1");
            }
        } else if (command.getMaxUses() != null && command.getMaxUses() <= 0) {
            throw new IllegalArgumentException("maxUses must be > 0 when provided");
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
        String shortCode = generateUniqueShortCode();
        Invitation invitation = Invitation.createActive(
                UUID.randomUUID(),
                command.getGroupId(),
                command.getType(),
                command.getInviteeEmail(),
                command.getInviterDisplayName(),
                token,
                shortCode,
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

    private String generateUniqueShortCode() {
        for (int attempt = 0; attempt < MAX_SHORT_CODE_ATTEMPTS; attempt++) {
            String generated = shortCodeGenerator.generate();
            if (generated == null || !generated.matches("^[A-Z0-9]{6}$")) {
                throw new IllegalArgumentException("generated shortCode must match ^[A-Z0-9]{6}$");
            }
            String normalized = generated.toUpperCase(Locale.ROOT);
            if (!invitationRepository.existsByShortCode(normalized)) {
                return normalized;
            }
        }
        throw new IllegalStateException("could not generate a unique shortCode");
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
