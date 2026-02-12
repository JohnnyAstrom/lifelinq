package app.lifelinq.features.household.application;

import app.lifelinq.features.household.contract.EnsureHouseholdMemberUseCase;
import app.lifelinq.features.household.domain.Membership;
import app.lifelinq.features.household.domain.MembershipRepository;
import java.util.List;
import java.util.UUID;

public final class EnsureHouseholdMemberUseCaseImpl implements EnsureHouseholdMemberUseCase {
    private final MembershipRepository membershipRepository;

    public EnsureHouseholdMemberUseCaseImpl(MembershipRepository membershipRepository) {
        if (membershipRepository == null) {
            throw new IllegalArgumentException("membershipRepository must not be null");
        }
        this.membershipRepository = membershipRepository;
    }

    @Override
    public void execute(UUID householdId, UUID actorUserId) {
        if (householdId == null) {
            throw new IllegalArgumentException("householdId must not be null");
        }
        if (actorUserId == null) {
            throw new IllegalArgumentException("actorUserId must not be null");
        }
        List<Membership> memberships = membershipRepository.findByHouseholdId(householdId);
        for (Membership membership : memberships) {
            if (membership.getUserId().equals(actorUserId)) {
                return;
            }
        }
        throw new AccessDeniedException("Actor is not a member of the household");
    }
}
