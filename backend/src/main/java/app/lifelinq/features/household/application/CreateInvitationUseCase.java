package app.lifelinq.features.household.application;

import app.lifelinq.features.household.domain.Invitation;
import app.lifelinq.features.household.domain.InvitationRepository;
import app.lifelinq.features.household.domain.InvitationStatus;
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
        if (command.getHouseholdId() == null) {
            throw new IllegalArgumentException("householdId must not be null");
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
        String token = generateUniqueToken();
        Invitation invitation = new Invitation(
                UUID.randomUUID(),
                command.getHouseholdId(),
                command.getInviteeEmail(),
                token,
                expiresAt,
                InvitationStatus.PENDING
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
