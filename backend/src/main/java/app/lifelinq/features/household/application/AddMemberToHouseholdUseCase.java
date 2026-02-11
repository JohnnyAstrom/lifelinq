package app.lifelinq.features.household.application;

import app.lifelinq.features.household.domain.HouseholdRole;
import app.lifelinq.features.household.domain.Membership;
import app.lifelinq.features.household.domain.MembershipRepository;

final class AddMemberToHouseholdUseCase {
    private final MembershipRepository membershipRepository;

    public AddMemberToHouseholdUseCase(MembershipRepository membershipRepository) {
        if (membershipRepository == null) {
            throw new IllegalArgumentException("membershipRepository must not be null");
        }
        this.membershipRepository = membershipRepository;
    }

    public AddMemberToHouseholdResult execute(AddMemberToHouseholdCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        if (command.getHouseholdId() == null) {
            throw new IllegalArgumentException("householdId must not be null");
        }
        if (command.getUserId() == null) {
            throw new IllegalArgumentException("userId must not be null");
        }

        Membership membership = new Membership(
                command.getHouseholdId(),
                command.getUserId(),
                HouseholdRole.MEMBER
        );

        membershipRepository.save(membership);

        return new AddMemberToHouseholdResult(
                membership.getHouseholdId(),
                membership.getUserId(),
                membership.getRole()
        );
    }
}
