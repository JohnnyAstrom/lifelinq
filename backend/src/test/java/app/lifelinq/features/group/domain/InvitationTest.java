package app.lifelinq.features.group.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InvitationTest {

    @Test
    void isActiveWhenNotExpired() {
        Invitation invitation = Invitation.createActive(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "test@example.com",
                "token",
                Instant.parse("2026-01-01T00:00:00Z")
        );

        assertTrue(invitation.isActive(Instant.parse("2025-12-31T00:00:00Z")));
    }

    @Test
    void isNotActiveWhenExpired() {
        Invitation invitation = Invitation.createActive(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "test@example.com",
                "token",
                Instant.parse("2025-01-01T00:00:00Z")
        );

        assertFalse(invitation.isActive(Instant.parse("2026-01-01T00:00:00Z")));
    }

    @Test
    void revokesWhenActive() {
        Invitation invitation = Invitation.createActive(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "test@example.com",
                "token",
                Instant.parse("2026-01-01T00:00:00Z")
        );

        invitation.revoke();

        assertEquals(InvitationStatus.REVOKED, invitation.getStatus());
    }

    @Test
    void revokeThrowsWhenAlreadyRevoked() {
        Invitation invitation = Invitation.rehydrate(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "test@example.com",
                "token",
                Instant.parse("2026-01-01T00:00:00Z"),
                InvitationStatus.REVOKED
        );
        assertThrows(IllegalStateException.class, invitation::revoke);
    }
}
