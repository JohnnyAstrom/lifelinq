package app.lifelinq.features.household.application;

import app.lifelinq.features.household.domain.MembershipRepository;

public final class RemoveMemberFromHouseholdUseCase {
    private final MembershipRepository membershipRepository;

    public RemoveMemberFromHouseholdUseCase(MembershipRepository membershipRepository) {
        if (membershipRepository == null) {
            throw new IllegalArgumentException("membershipRepository must not be null");
        }
        this.membershipRepository = membershipRepository;
    }

    public RemoveMemberFromHouseholdResult execute(RemoveMemberFromHouseholdCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        if (command.getHouseholdId() == null) {
            throw new IllegalArgumentException("householdId must not be null");
        }
        if (command.getUserId() == null) {
            throw new IllegalArgumentException("userId must not be null");
        }

        boolean removed = membershipRepository.deleteByHouseholdIdAndUserId(
                command.getHouseholdId(),
                command.getUserId()
        );

        return new RemoveMemberFromHouseholdResult(removed);
    }
}
