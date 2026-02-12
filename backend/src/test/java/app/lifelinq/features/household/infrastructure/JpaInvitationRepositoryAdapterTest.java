package app.lifelinq.features.household.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.features.household.domain.Invitation;
import app.lifelinq.features.household.domain.InvitationStatus;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = HouseholdJpaTestApplication.class)
@ActiveProfiles("test")
class JpaInvitationRepositoryAdapterTest {

    @Autowired
    private InvitationJpaRepository invitationJpaRepository;

    @Test
    void savesLoadsAndPreservesLifecycle() {
        JpaInvitationRepositoryAdapter adapter = new JpaInvitationRepositoryAdapter(
                invitationJpaRepository,
                new InvitationMapper()
        );
        Instant expiresAt = Instant.parse("2026-01-01T00:00:00Z");
        Invitation invitation = Invitation.createActive(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "test@example.com",
                "token-1",
                expiresAt
        );

        adapter.save(invitation);
        Optional<Invitation> loaded = adapter.findByToken("token-1");

        assertTrue(loaded.isPresent());
        assertEquals(invitation.getId(), loaded.get().getId());
        assertEquals(invitation.getHouseholdId(), loaded.get().getHouseholdId());
        assertEquals(invitation.getInviteeEmail(), loaded.get().getInviteeEmail());
        assertEquals("token-1", loaded.get().getToken());
        assertEquals(expiresAt, loaded.get().getExpiresAt());
        assertEquals(InvitationStatus.ACTIVE, loaded.get().getStatus());

        loaded.get().revoke();
        assertEquals(InvitationStatus.REVOKED, loaded.get().getStatus());

        Invitation expiredCandidate = Invitation.createActive(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "expire@example.com",
                "token-2",
                Instant.parse("2025-01-01T00:00:00Z")
        );
        adapter.save(expiredCandidate);
        Invitation loadedExpired = adapter.findByToken("token-2").orElseThrow();
        assertTrue(loadedExpired.isExpired(Instant.parse("2026-01-01T00:00:00Z")));
    }
}
