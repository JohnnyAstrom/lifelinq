package app.lifelinq.features.group.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

class ResolveInvitationCodeUseCaseTest {

    @Test
    void resolvesInvitationByCodeCaseInsensitively() {
        InMemoryInvitationRepository repository = new InMemoryInvitationRepository();
        Invitation invitation = Invitation.createActive(
                UUID.randomUUID(),
                UUID.randomUUID(),
                InvitationType.LINK,
                null,
                null,
                "token-1",
                "K7M9XQ",
                Instant.parse("2026-01-01T00:00:00Z"),
                null
        );
        repository.save(invitation);

        ResolveInvitationCodeUseCase useCase = new ResolveInvitationCodeUseCase(repository);
        Optional<Invitation> result = useCase.execute(new ResolveInvitationCodeCommand("k7m9xq"));

        assertTrue(result.isPresent());
        assertEquals(invitation.getId(), result.get().getId());
        assertEquals(invitation.getGroupId(), result.get().getGroupId());
        assertEquals(InvitationType.LINK, result.get().getType());
        assertEquals(InvitationStatus.ACTIVE, result.get().getStatus());
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
        public Optional<Invitation> findByShortCode(String shortCode) {
            for (Invitation invitation : byToken.values()) {
                if (shortCode.equals(invitation.getShortCode())) {
                    return Optional.of(invitation);
                }
            }
            return Optional.empty();
        }

        @Override
        public Optional<Invitation> findById(UUID id) {
            return byToken.values().stream().filter(inv -> inv.getId().equals(id)).findFirst();
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
                    .filter(inv -> groupId.equals(inv.getGroupId()))
                    .toList();
        }

        @Override
        public List<Invitation> findActive() {
            return byToken.values().stream().filter(inv -> inv.getStatus() == InvitationStatus.ACTIVE).toList();
        }

        @Override
        public Optional<Invitation> findActiveByGroupIdAndInviteeEmail(UUID groupId, String inviteeEmail) {
            return byToken.values().stream()
                    .filter(inv -> inv.getStatus() == InvitationStatus.ACTIVE)
                    .filter(inv -> groupId.equals(inv.getGroupId()))
                    .filter(invitee -> inviteeEmail.equals(invitee.getInviteeEmail()))
                    .findFirst();
        }
    }
}
