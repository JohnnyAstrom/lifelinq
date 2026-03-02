package app.lifelinq.features.group.infrastructure;

import app.lifelinq.features.group.domain.Invitation;
import app.lifelinq.features.group.domain.InvitationType;

public final class InvitationMapper {

    public InvitationEntity toEntity(Invitation invitation) {
        if (invitation == null) {
            throw new IllegalArgumentException("invitation must not be null");
        }
        return new InvitationEntity(
                invitation.getId(),
                invitation.getGroupId(),
                invitation.getType(),
                invitation.getInviteeEmail(),
                invitation.getInviterDisplayName(),
                invitation.getToken(),
                invitation.getExpiresAt(),
                invitation.getMaxUses(),
                invitation.getUsageCount(),
                invitation.getStatus()
        );
    }

    public Invitation toDomain(InvitationEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity must not be null");
        }
        return Invitation.rehydrate(
                entity.getId(),
                entity.getGroupId(),
                entity.getType() == null ? InvitationType.EMAIL : entity.getType(),
                entity.getInviteeEmail(),
                entity.getInviterDisplayName(),
                entity.getToken(),
                entity.getExpiresAt(),
                entity.getMaxUses(),
                entity.getUsageCount(),
                entity.getStatus()
        );
    }
}
