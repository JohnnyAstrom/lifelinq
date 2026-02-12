package app.lifelinq.features.household.domain;

import java.time.Instant;
import java.util.UUID;

public final class Invitation {
    private final UUID id;
    private final UUID householdId;
    private final String inviteeEmail;
    private final String token;
    private final Instant expiresAt;
    private InvitationStatus status;

    private Invitation(
            UUID id,
            UUID householdId,
            String inviteeEmail,
            String token,
            Instant expiresAt,
            InvitationStatus status
    ) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        if (householdId == null) {
            throw new IllegalArgumentException("householdId must not be null");
        }
        if (inviteeEmail == null || inviteeEmail.isBlank()) {
            throw new IllegalArgumentException("inviteeEmail must not be blank");
        }
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("token must not be blank");
        }
        if (expiresAt == null) {
            throw new IllegalArgumentException("expiresAt must not be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        this.id = id;
        this.householdId = householdId;
        this.inviteeEmail = inviteeEmail;
        this.token = token;
        this.expiresAt = expiresAt;
        this.status = status;
    }

    public static Invitation createActive(
            UUID id,
            UUID householdId,
            String inviteeEmail,
            String token,
            Instant expiresAt
    ) {
        return new Invitation(id, householdId, inviteeEmail, token, expiresAt, InvitationStatus.ACTIVE);
    }

    public static Invitation rehydrate(
            UUID id,
            UUID householdId,
            String inviteeEmail,
            String token,
            Instant expiresAt,
            InvitationStatus status
    ) {
        return new Invitation(id, householdId, inviteeEmail, token, expiresAt, status);
    }

    public UUID getId() {
        return id;
    }

    public UUID getHouseholdId() {
        return householdId;
    }

    public String getInviteeEmail() {
        return inviteeEmail;
    }

    public String getToken() {
        return token;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public InvitationStatus getStatus() {
        return status;
    }

    public boolean isActive(Instant now) {
        return status == InvitationStatus.ACTIVE && !isExpired(now);
    }

    public boolean isExpired(Instant now) {
        if (now == null) {
            throw new IllegalArgumentException("now must not be null");
        }
        return now.isAfter(expiresAt);
    }

    public void revoke() {
        if (status == InvitationStatus.REVOKED) {
            throw new IllegalStateException("invitation is already revoked");
        }
        status = InvitationStatus.REVOKED;
    }
}
