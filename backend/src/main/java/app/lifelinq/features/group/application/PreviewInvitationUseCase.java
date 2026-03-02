package app.lifelinq.features.group.application;

import app.lifelinq.features.group.domain.GroupRepository;
import app.lifelinq.features.group.domain.Invitation;
import app.lifelinq.features.group.domain.InvitationRepository;
import app.lifelinq.features.group.domain.InvitationStatus;

final class PreviewInvitationUseCase {
    private final InvitationRepository invitationRepository;
    private final GroupRepository groupRepository;

    PreviewInvitationUseCase(
            InvitationRepository invitationRepository,
            GroupRepository groupRepository
    ) {
        if (invitationRepository == null) {
            throw new IllegalArgumentException("invitationRepository must not be null");
        }
        if (groupRepository == null) {
            throw new IllegalArgumentException("groupRepository must not be null");
        }
        this.invitationRepository = invitationRepository;
        this.groupRepository = groupRepository;
    }

    PreviewInvitationResult execute(PreviewInvitationCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        if (command.getToken() == null || command.getToken().isBlank()) {
            throw new IllegalArgumentException("token must not be blank");
        }
        if (command.getNow() == null) {
            throw new IllegalArgumentException("now must not be null");
        }

        Invitation invitation = invitationRepository.findByToken(command.getToken()).orElse(null);
        if (invitation == null) {
            return new PreviewInvitationResult(
                    false,
                    PreviewInvitationReason.NOT_FOUND,
                    null,
                    null,
                    null,
                    null
            );
        }

        String placeName = groupRepository.findById(invitation.getGroupId())
                .map(group -> group.getName())
                .orElse(null);

        PreviewInvitationReason reason = resolveReason(invitation, command.getNow());
        boolean valid = reason == PreviewInvitationReason.VALID;
        return new PreviewInvitationResult(
                valid,
                reason,
                placeName,
                invitation.getInviterDisplayName(),
                invitation.getExpiresAt(),
                invitation.getType()
        );
    }

    private PreviewInvitationReason resolveReason(Invitation invitation, java.time.Instant now) {
        if (invitation.getStatus() == InvitationStatus.REVOKED) {
            return PreviewInvitationReason.REVOKED;
        }
        if (invitation.isExpired(now)) {
            return PreviewInvitationReason.EXPIRED;
        }
        if (invitation.getUsageCount() >= invitation.getMaxUses()) {
            return PreviewInvitationReason.EXHAUSTED;
        }
        return PreviewInvitationReason.VALID;
    }
}
