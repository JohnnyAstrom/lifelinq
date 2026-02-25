package app.lifelinq.features.group.infrastructure;

import app.lifelinq.features.group.domain.Invitation;

public final class InvitationMapper {

    public InvitationEntity toEntity(Invitation invitation) {
        if (invitation == null) {
            throw new IllegalArgumentException("invitation must not be null");
        }
        return new InvitationEntity(
                invitation.getId(),
                invitation.getGroupId(),
                invitation.getInviteeEmail(),
                invitation.getToken(),
                invitation.getExpiresAt(),
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
                entity.getInviteeEmail(),
                entity.getToken(),
                entity.getExpiresAt(),
                entity.getStatus()
        );
    }
}
