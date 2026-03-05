package app.lifelinq.features.group.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.features.group.domain.Group;
import app.lifelinq.features.group.domain.GroupRepository;
import app.lifelinq.features.group.domain.Invitation;
import app.lifelinq.features.group.domain.InvitationRepository;
import app.lifelinq.features.group.domain.InvitationStatus;
import app.lifelinq.features.group.domain.InvitationType;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PreviewInvitationUseCaseTest {

    @Test
    void returnsValidWhenInvitationIsActiveAndUsable() {
        InMemoryInvitationRepository invitationRepository = new InMemoryInvitationRepository();
        InMemoryGroupRepository groupRepository = new InMemoryGroupRepository();
        UUID groupId = UUID.randomUUID();
        invitationRepository.save(Invitation.createActive(
                UUID.randomUUID(),
                groupId,
                InvitationType.EMAIL,
                "test@example.com",
                "Alex Doe",
                "token-valid",
                null,
                Instant.parse("2026-01-10T00:00:00Z"),
                2
        ));
        groupRepository.save(new Group(groupId, "Family"));

        PreviewInvitationUseCase useCase = new PreviewInvitationUseCase(invitationRepository, groupRepository);
        PreviewInvitationResult result = useCase.execute(new PreviewInvitationCommand(
                "token-valid",
                Instant.parse("2026-01-01T00:00:00Z")
        ));

        assertTrue(result.isValid());
        assertEquals(PreviewInvitationReason.VALID, result.getReason());
        assertEquals("Family", result.getPlaceName());
        assertEquals("Alex Doe", result.getInviterDisplayName());
        assertEquals(InvitationType.EMAIL, result.getType());
    }

    @Test
    void returnsNotFoundWhenTokenMissing() {
        PreviewInvitationUseCase useCase = new PreviewInvitationUseCase(
                new InMemoryInvitationRepository(),
                new InMemoryGroupRepository()
        );

        PreviewInvitationResult result = useCase.execute(new PreviewInvitationCommand(
                "missing-token",
                Instant.parse("2026-01-01T00:00:00Z")
        ));

        assertFalse(result.isValid());
        assertEquals(PreviewInvitationReason.NOT_FOUND, result.getReason());
    }

    @Test
    void returnsExpiredWhenInvitationExpired() {
        InMemoryInvitationRepository invitationRepository = new InMemoryInvitationRepository();
        invitationRepository.save(Invitation.createActive(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "test@example.com",
                "token-expired",
                Instant.parse("2026-01-01T00:00:00Z")
        ));
        PreviewInvitationUseCase useCase = new PreviewInvitationUseCase(
                invitationRepository,
                new InMemoryGroupRepository()
        );

        PreviewInvitationResult result = useCase.execute(new PreviewInvitationCommand(
                "token-expired",
                Instant.parse("2026-01-02T00:00:00Z")
        ));

        assertFalse(result.isValid());
        assertEquals(PreviewInvitationReason.EXPIRED, result.getReason());
    }

    @Test
    void returnsRevokedWhenInvitationRevoked() {
        InMemoryInvitationRepository invitationRepository = new InMemoryInvitationRepository();
        invitationRepository.save(Invitation.rehydrate(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "test@example.com",
                "token-revoked",
                Instant.parse("2026-01-10T00:00:00Z"),
                1,
                1,
                InvitationStatus.REVOKED
        ));
        PreviewInvitationUseCase useCase = new PreviewInvitationUseCase(
                invitationRepository,
                new InMemoryGroupRepository()
        );

        PreviewInvitationResult result = useCase.execute(new PreviewInvitationCommand(
                "token-revoked",
                Instant.parse("2026-01-01T00:00:00Z")
        ));

        assertFalse(result.isValid());
        assertEquals(PreviewInvitationReason.REVOKED, result.getReason());
    }

    @Test
    void returnsExhaustedWhenUsageAtMax() {
        InMemoryInvitationRepository invitationRepository = new InMemoryInvitationRepository();
        invitationRepository.save(Invitation.rehydrate(
                UUID.randomUUID(),
                UUID.randomUUID(),
                InvitationType.LINK,
                null,
                "token-exhausted",
                Instant.parse("2026-01-10T00:00:00Z"),
                2,
                2,
                InvitationStatus.ACTIVE
        ));
        PreviewInvitationUseCase useCase = new PreviewInvitationUseCase(
                invitationRepository,
                new InMemoryGroupRepository()
        );

        PreviewInvitationResult result = useCase.execute(new PreviewInvitationCommand(
                "token-exhausted",
                Instant.parse("2026-01-01T00:00:00Z")
        ));

        assertFalse(result.isValid());
        assertEquals(PreviewInvitationReason.EXHAUSTED, result.getReason());
    }

    @Test
    void unlimitedLinkIsNotExhausted() {
        InMemoryInvitationRepository invitationRepository = new InMemoryInvitationRepository();
        invitationRepository.save(Invitation.rehydrate(
                UUID.randomUUID(),
                UUID.randomUUID(),
                InvitationType.LINK,
                null,
                "token-unlimited",
                Instant.parse("2026-01-10T00:00:00Z"),
                null,
                0,
                InvitationStatus.ACTIVE
        ));
        PreviewInvitationUseCase useCase = new PreviewInvitationUseCase(
                invitationRepository,
                new InMemoryGroupRepository()
        );

        PreviewInvitationResult result = useCase.execute(new PreviewInvitationCommand(
                "token-unlimited",
                Instant.parse("2026-01-01T00:00:00Z")
        ));

        assertTrue(result.isValid());
        assertEquals(PreviewInvitationReason.VALID, result.getReason());
    }

    private static final class InMemoryInvitationRepository implements InvitationRepository {
        private final Map<String, Invitation> byToken = new HashMap<>();

        @Override
        public void save(Invitation invitation) {
            byToken.put(invitation.getToken(), invitation);
        }

        @Override
        public Optional<Invitation> findByToken(String token) {
            return Optional.ofNullable(byToken.get(token));
        }

        @Override
        public Optional<Invitation> findById(UUID id) {
            return Optional.empty();
        }

        @Override
        public Optional<Invitation> findByShortCode(String shortCode) {
            for (Invitation invitation : byToken.values()) {
                if (shortCode.equals(invitation.getShortCode())) {
                    return Optional.of(invitation);
                }
            }
            return Optional.empty();
        }

        @Override
        public boolean existsByToken(String token) {
            return byToken.containsKey(token);
        }

        @Override
        public boolean existsByShortCode(String shortCode) {
            return findByShortCode(shortCode).isPresent();
        }

        @Override
        public List<Invitation> findByGroupId(UUID groupId) {
            return byToken.values().stream()
                    .filter(invitation -> groupId.equals(invitation.getGroupId()))
                    .toList();
        }

        @Override
        public List<Invitation> findActive() {
            return List.of();
        }

        @Override
        public Optional<Invitation> findActiveByGroupIdAndInviteeEmail(UUID groupId, String inviteeEmail) {
            return Optional.empty();
        }
    }

    private static final class InMemoryGroupRepository implements GroupRepository {
        private final Map<UUID, Group> byId = new HashMap<>();

        @Override
        public void save(Group group) {
            byId.put(group.getId(), group);
        }

        @Override
        public Optional<Group> findById(UUID id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public void deleteById(UUID id) {
            byId.remove(id);
        }
    }
}
