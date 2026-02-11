package app.lifelinq.features.household.application;

import app.lifelinq.features.household.domain.Household;
import app.lifelinq.features.household.domain.HouseholdRepository;
import app.lifelinq.features.household.domain.HouseholdRole;
import app.lifelinq.features.household.domain.Membership;
import app.lifelinq.features.household.domain.MembershipRepository;
import java.util.UUID;

final class CreateHouseholdUseCase {
    private final HouseholdRepository householdRepository;
    private final MembershipRepository membershipRepository;

    public CreateHouseholdUseCase(HouseholdRepository householdRepository, MembershipRepository membershipRepository) {
        if (householdRepository == null) {
            throw new IllegalArgumentException("householdRepository must not be null");
        }
        if (membershipRepository == null) {
            throw new IllegalArgumentException("membershipRepository must not be null");
        }
        this.householdRepository = householdRepository;
        this.membershipRepository = membershipRepository;
    }

    public CreateHouseholdResult execute(CreateHouseholdCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        if (command.getOwnerUserId() == null) {
            throw new IllegalArgumentException("ownerUserId must not be null");
        }

        UUID householdId = UUID.randomUUID();
        Household household = new Household(householdId, command.getHouseholdName());
        Membership ownerMembership = new Membership(household.getId(), command.getOwnerUserId(), HouseholdRole.OWNER);

        householdRepository.save(household);
        membershipRepository.save(ownerMembership);

        return new CreateHouseholdResult(household.getId());
    }
}
