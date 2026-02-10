package app.lifelinq.features.household.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InvitationTest {

    @Test
    void acceptsWhenPendingAndNotExpired() {
        Invitation invitation = new Invitation(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "test@example.com",
                "token",
                Instant.parse("2026-01-01T00:00:00Z"),
                InvitationStatus.PENDING
        );

        invitation.accept(Instant.parse("2025-12-31T00:00:00Z"));

        assertEquals(InvitationStatus.ACCEPTED, invitation.getStatus());
    }

    @Test
    void cannotAcceptWhenExpired() {
        Invitation invitation = new Invitation(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "test@example.com",
                "token",
                Instant.parse("2025-01-01T00:00:00Z"),
                InvitationStatus.PENDING
        );

        assertThrows(IllegalStateException.class, () -> invitation.accept(Instant.parse("2026-01-01T00:00:00Z")));
    }

    @Test
    void expiresWhenPastDue() {
        Invitation invitation = new Invitation(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "test@example.com",
                "token",
                Instant.parse("2025-01-01T00:00:00Z"),
                InvitationStatus.PENDING
        );

        invitation.expire(Instant.parse("2026-01-01T00:00:00Z"));

        assertEquals(InvitationStatus.EXPIRED, invitation.getStatus());
    }
}
