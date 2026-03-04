package app.lifelinq.features.group.infrastructure;

import app.lifelinq.features.group.domain.InvitationStatus;
import app.lifelinq.features.group.domain.InvitationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "invitations",
        indexes = {
                @Index(name = "idx_invitation_group_email_status", columnList = "group_id,inviteeEmail,status")
        }
)
public class InvitationEntity {
    @Id
    private UUID id;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvitationType type;

    @Column(nullable = true)
    private String inviteeEmail;

    @Column(nullable = true)
    private String inviterDisplayName;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = true, unique = true, length = 6)
    private String shortCode;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = true)
    private Integer maxUses;

    @Column(nullable = false)
    private int usageCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvitationStatus status;

    protected InvitationEntity() {
    }

    public InvitationEntity(
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

    public UUID getId() {
        return id;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public String getInviteeEmail() {
        return inviteeEmail;
    }

    public String getInviterDisplayName() {
        return inviterDisplayName;
    }

    public InvitationType getType() {
        return type;
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

    public Integer getMaxUses() {
        return maxUses;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public InvitationStatus getStatus() {
        return status;
    }
}
