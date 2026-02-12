package app.lifelinq.features.household.application;

import app.lifelinq.features.household.domain.HouseholdRole;
import app.lifelinq.features.household.domain.Invitation;
import app.lifelinq.features.household.domain.InvitationRepository;
import app.lifelinq.features.household.domain.Membership;
import app.lifelinq.features.household.domain.MembershipRepository;

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

        if (!invitation.isActive(command.getNow())) {
            throw new IllegalStateException("invitation is not active");
        }
        invitation.revoke();

        Membership membership = new Membership(
                invitation.getHouseholdId(),
                command.getUserId(),
                HouseholdRole.MEMBER
        );

        membershipRepository.save(membership);
        invitationRepository.save(invitation);

        return new AcceptInvitationResult(invitation.getHouseholdId(), command.getUserId());
    }
}
