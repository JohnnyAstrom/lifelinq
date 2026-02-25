package app.lifelinq.features.group.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.lifelinq.features.group.domain.Invitation;
import app.lifelinq.features.group.domain.InvitationStatus;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InMemoryInvitationRepositoryTest {

    @Test
    void requiresInvitation() {
        InMemoryInvitationRepository repository = new InMemoryInvitationRepository();
        assertThrows(IllegalArgumentException.class, () -> repository.save(null));
    }

    @Test
    void requiresTokenForLookup() {
        InMemoryInvitationRepository repository = new InMemoryInvitationRepository();
        assertThrows(IllegalArgumentException.class, () -> repository.findByToken(" "));
        assertThrows(IllegalArgumentException.class, () -> repository.existsByToken(null));
    }

    @Test
    void savesAndFindsByToken() {
        InMemoryInvitationRepository repository = new InMemoryInvitationRepository();
        Invitation invitation = Invitation.createActive(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "test@example.com",
                "token-1",
                Instant.parse("2026-01-01T00:00:00Z")
        );

        repository.save(invitation);
        Optional<Invitation> found = repository.findByToken("token-1");

        assertEquals(true, found.isPresent());
        assertEquals("token-1", found.get().getToken());
    }

    @Test
    void findsActiveOnly() {
        InMemoryInvitationRepository repository = new InMemoryInvitationRepository();
        repository.save(Invitation.createActive(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "test@example.com",
                "token-1",
                Instant.parse("2026-01-01T00:00:00Z")
        ));
        repository.save(Invitation.rehydrate(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "test2@example.com",
                "token-2",
                Instant.parse("2026-01-01T00:00:00Z"),
                InvitationStatus.REVOKED
        ));

        assertEquals(1, repository.findActive().size());
    }
}
