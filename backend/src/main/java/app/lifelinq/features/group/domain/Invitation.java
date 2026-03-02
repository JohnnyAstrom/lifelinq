package app.lifelinq.features.group.domain;

import java.time.Instant;
import java.util.UUID;

public final class Invitation {
    private final UUID id;
    private final UUID groupId;
    private final String inviteeEmail;
    private final String token;
    private final Instant expiresAt;
    private final int maxUses;
    private int usageCount;
    private InvitationStatus status;

    private Invitation(
            UUID id,
            UUID groupId,
            String inviteeEmail,
            String token,
            Instant expiresAt,
            int maxUses,
            int usageCount,
            InvitationStatus status
    ) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
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
        if (maxUses <= 0) {
            throw new IllegalArgumentException("maxUses must be > 0");
        }
        if (usageCount < 0) {
            throw new IllegalArgumentException("usageCount must be >= 0");
        }
        if (usageCount > maxUses) {
            throw new IllegalArgumentException("usageCount must be <= maxUses");
        }
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        this.id = id;
        this.groupId = groupId;
        this.inviteeEmail = inviteeEmail;
        this.token = token;
        this.expiresAt = expiresAt;
        this.maxUses = maxUses;
        this.usageCount = usageCount;
        this.status = status;
    }

    public static Invitation createActive(
            UUID id,
            UUID groupId,
            String inviteeEmail,
            String token,
            Instant expiresAt
    ) {
        return new Invitation(id, groupId, inviteeEmail, token, expiresAt, 1, 0, InvitationStatus.ACTIVE);
    }

    public static Invitation createActive(
            UUID id,
            UUID groupId,
            String inviteeEmail,
            String token,
            Instant expiresAt,
            int maxUses
    ) {
        return new Invitation(id, groupId, inviteeEmail, token, expiresAt, maxUses, 0, InvitationStatus.ACTIVE);
    }

    public static Invitation rehydrate(
            UUID id,
            UUID groupId,
            String inviteeEmail,
            String token,
            Instant expiresAt,
            InvitationStatus status
    ) {
        int usageCount = status == InvitationStatus.REVOKED ? 1 : 0;
        return new Invitation(id, groupId, inviteeEmail, token, expiresAt, 1, usageCount, status);
    }

    public static Invitation rehydrate(
            UUID id,
            UUID groupId,
            String inviteeEmail,
            String token,
            Instant expiresAt,
            int maxUses,
            int usageCount,
            InvitationStatus status
    ) {
        return new Invitation(id, groupId, inviteeEmail, token, expiresAt, maxUses, usageCount, status);
    }

    public UUID getId() {
        return id;
    }

    public UUID getGroupId() {
        return groupId;
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

    public int getMaxUses() {
        return maxUses;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public boolean isActive(Instant now) {
        return status == InvitationStatus.ACTIVE && !isExpired(now);
    }

    public boolean isAcceptAllowed(Instant now) {
        return status == InvitationStatus.ACTIVE
                && !isExpired(now)
                && usageCount < maxUses;
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

    public void registerAcceptance(Instant now) {
        if (now == null) {
            throw new IllegalArgumentException("now must not be null");
        }
        if (status == InvitationStatus.REVOKED) {
            throw new IllegalStateException("invitation is revoked");
        }
        if (isExpired(now)) {
            throw new IllegalStateException("invitation is expired");
        }
        if (usageCount >= maxUses) {
            throw new IllegalStateException("invitation has reached max uses");
        }
        usageCount++;
    }
}
