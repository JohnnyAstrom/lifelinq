package app.lifelinq.features.household.application;

import app.lifelinq.features.household.domain.Membership;
import app.lifelinq.features.household.domain.MembershipRepository;
import java.util.List;

public final class ListHouseholdMembersUseCase {
    private final MembershipRepository membershipRepository;

    public ListHouseholdMembersUseCase(MembershipRepository membershipRepository) {
        if (membershipRepository == null) {
            throw new IllegalArgumentException("membershipRepository must not be null");
        }
        this.membershipRepository = membershipRepository;
    }

    public ListHouseholdMembersResult execute(ListHouseholdMembersCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        if (command.getHouseholdId() == null) {
            throw new IllegalArgumentException("householdId must not be null");
        }

        List<Membership> members = membershipRepository.findByHouseholdId(command.getHouseholdId());
        return new ListHouseholdMembersResult(members);
    }
}
