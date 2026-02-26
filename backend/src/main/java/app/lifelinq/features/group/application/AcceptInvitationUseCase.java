package app.lifelinq.features.group.application;

import app.lifelinq.features.group.domain.GroupRole;
import app.lifelinq.features.group.domain.Invitation;
import app.lifelinq.features.group.domain.InvitationRepository;
import app.lifelinq.features.group.domain.Membership;
import app.lifelinq.features.group.domain.MembershipRepository;

final class AcceptInvitationUseCase {
    private final InvitationRepository invitationRepository;
    private final MembershipRepository membershipRepository;

    public AcceptInvitationUseCase(
            InvitationRepository invitationRepository,
            MembershipRepository membershipRepository
    ) {
        if (invitationRepository == null) {
            throw new IllegalArgumentException("invitationRepository must not be null");
        }
        if (membershipRepository == null) {
            throw new IllegalArgumentException("membershipRepository must not be null");
        }
        this.invitationRepository = invitationRepository;
        this.membershipRepository = membershipRepository;
    }

    public AcceptInvitationResult execute(AcceptInvitationCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        if (command.getToken() == null || command.getToken().isBlank()) {
            throw new IllegalArgumentException("token must not be blank");
        }
        if (command.getUserId() == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        if (command.getNow() == null) {
            throw new IllegalArgumentException("now must not be null");
        }

        Invitation invitation = invitationRepository.findByToken(command.getToken())
                .orElseThrow(() -> new IllegalArgumentException("invitation not found"));

        if (isAlreadyMember(invitation.getGroupId(), command.getUserId())) {
            if (invitation.isActive(command.getNow())) {
                invitation.revoke();
                invitationRepository.save(invitation);
            }
            return new AcceptInvitationResult(invitation.getGroupId(), command.getUserId());
        }

        if (!invitation.isActive(command.getNow())) {
            throw new IllegalStateException("invitation is not active");
        }
        invitation.revoke();

        Membership membership = new Membership(
                invitation.getGroupId(),
                command.getUserId(),
                GroupRole.MEMBER
        );

        membershipRepository.save(membership);
        invitationRepository.save(invitation);

        return new AcceptInvitationResult(invitation.getGroupId(), command.getUserId());
    }

    private boolean isAlreadyMember(java.util.UUID groupId, java.util.UUID userId) {
        for (Membership membership : membershipRepository.findByGroupId(groupId)) {
            if (membership.getGroupId().equals(groupId) && membership.getUserId().equals(userId)) {
                return true;
            }
        }
        return false;
    }
}
