package app.lifelinq.features.group.domain;

import java.time.Instant;
import java.util.UUID;

public final class Invitation {
    private final UUID id;
    private final UUID groupId;
    private final InvitationType type;
    private final String inviteeEmail;
    private final String inviterDisplayName;
    private final String token;
    private final String shortCode;
    private final Instant expiresAt;
    private final Integer maxUses;
    private int usageCount;
    private InvitationStatus status;

    private Invitation(
            UUID id,
            UUID groupId,
            InvitationType type,
            String inviteeEmail,
            String inviterDisplayName,
            String token,
            String shortCode,
            Instant expiresAt,
            Integer maxUses,
            int usageCount,
            InvitationStatus status
    ) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
        if (type == InvitationType.EMAIL && (inviteeEmail == null || inviteeEmail.isBlank())) {
            throw new IllegalArgumentException("inviteeEmail must not be blank");
        }
        if (type == InvitationType.LINK && inviteeEmail != null) {
            throw new IllegalArgumentException("inviteeEmail must be null for LINK invitations");
        }
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("token must not be blank");
        }
        if (shortCode != null && !shortCode.matches("^[A-Z0-9]{6}$")) {
            throw new IllegalArgumentException("shortCode must match ^[A-Z0-9]{6}$");
        }
        if (expiresAt == null) {
            throw new IllegalArgumentException("expiresAt must not be null");
        }
        if (maxUses != null && maxUses <= 0) {
            throw new IllegalArgumentException("maxUses must be > 0 when provided");
        }
        if (usageCount < 0) {
            throw new IllegalArgumentException("usageCount must be >= 0");
        }
        if (maxUses != null && usageCount > maxUses) {
            throw new IllegalArgumentException("usageCount must be <= maxUses when maxUses is provided");
        }
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        this.id = id;
        this.groupId = groupId;
        this.type = type;
        this.inviteeEmail = inviteeEmail;
        this.inviterDisplayName = inviterDisplayName;
        this.token = token;
        this.shortCode = shortCode;
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
        return new Invitation(id, groupId, InvitationType.EMAIL, inviteeEmail, null, token, null, expiresAt, 1, 0, InvitationStatus.ACTIVE);
    }

    public static Invitation createActive(
            UUID id,
            UUID groupId,
            String inviteeEmail,
            String token,
            Instant expiresAt,
            Integer maxUses
    ) {
        return createActive(id, groupId, inviteeEmail, token, null, expiresAt, maxUses);
    }

    public static Invitation createActive(
            UUID id,
            UUID groupId,
            String inviteeEmail,
            String token,
            String shortCode,
            Instant expiresAt,
            Integer maxUses
    ) {
        return new Invitation(
                id,
                groupId,
                InvitationType.EMAIL,
                inviteeEmail,
                null,
                token,
                shortCode,
                expiresAt,
                maxUses,
                0,
                InvitationStatus.ACTIVE
        );
    }

    public static Invitation createActive(
            UUID id,
            UUID groupId,
            InvitationType type,
            String inviteeEmail,
            String inviterDisplayName,
            String token,
            String shortCode,
            Instant expiresAt,
            Integer maxUses
    ) {
        return new Invitation(
                id,
                groupId,
                type,
                inviteeEmail,
                inviterDisplayName,
                token,
                shortCode,
                expiresAt,
                maxUses,
                0,
                InvitationStatus.ACTIVE
        );
    }

    public static Invitation createActive(
            UUID id,
            UUID groupId,
            InvitationType type,
            String inviteeEmail,
            String token,
            Instant expiresAt,
            Integer maxUses
    ) {
        return createActive(id, groupId, type, inviteeEmail, null, token, null, expiresAt, maxUses);
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
        return new Invitation(id, groupId, InvitationType.EMAIL, inviteeEmail, null, token, null, expiresAt, 1, usageCount, status);
    }

    public static Invitation rehydrate(
            UUID id,
            UUID groupId,
            String inviteeEmail,
            String token,
            Instant expiresAt,
            Integer maxUses,
            int usageCount,
            InvitationStatus status
    ) {
        return new Invitation(
                id,
                groupId,
                InvitationType.EMAIL,
                inviteeEmail,
                null,
                token,
                null,
                expiresAt,
                maxUses,
                usageCount,
                status
        );
    }

    public static Invitation rehydrate(
            UUID id,
            UUID groupId,
            InvitationType type,
            String inviteeEmail,
            String inviterDisplayName,
            String token,
            String shortCode,
            Instant expiresAt,
            Integer maxUses,
            int usageCount,
            InvitationStatus status
    ) {
        return new Invitation(
                id,
                groupId,
                type,
                inviteeEmail,
                inviterDisplayName,
                token,
                shortCode,
                expiresAt,
                maxUses,
                usageCount,
                status
        );
    }

    public static Invitation rehydrate(
            UUID id,
            UUID groupId,
            InvitationType type,
            String inviteeEmail,
            String token,
            Instant expiresAt,
            Integer maxUses,
            int usageCount,
            InvitationStatus status
    ) {
        return rehydrate(id, groupId, type, inviteeEmail, null, token, null, expiresAt, maxUses, usageCount, status);
    }

    public UUID getId() {
        return id;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public InvitationType getType() {
        return type;
    }

    public String getInviteeEmail() {
        return inviteeEmail;
    }

    public String getInviterDisplayName() {
        return inviterDisplayName;
    }

    public String getToken() {
        return token;
    }

    public String getShortCode() {
        return shortCode;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public InvitationStatus getStatus() {
        return status;
    }

    public Integer getMaxUses() {
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
                && (maxUses == null || usageCount < maxUses);
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
        if (maxUses != null && usageCount >= maxUses) {
            throw new IllegalStateException("invitation has reached max uses");
        }
        if (maxUses != null) {
            usageCount++;
        }
    }
}
