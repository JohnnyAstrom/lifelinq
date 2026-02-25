package app.lifelinq.features.group.infrastructure;

import app.lifelinq.features.group.domain.InvitationStatus;
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
                @Index(name = "idx_invitation_household_email_status", columnList = "household_id,inviteeEmail,status")
        }
)
public class InvitationEntity {
    @Id
    private UUID id;

    @Column(name = "household_id", nullable = false)
    private UUID groupId;

    @Column(nullable = false)
    private String inviteeEmail;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvitationStatus status;

    protected InvitationEntity() {
    }

    public InvitationEntity(
            UUID id,
            UUID groupId,
            String inviteeEmail,
            String token,
            Instant expiresAt,
            InvitationStatus status
    ) {
        this.id = id;
        this.groupId = groupId;
        this.inviteeEmail = inviteeEmail;
        this.token = token;
        this.expiresAt = expiresAt;
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

    public String getToken() {
        return token;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public InvitationStatus getStatus() {
        return status;
    }
}
