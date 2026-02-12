package app.lifelinq.features.household.infrastructure;

import app.lifelinq.features.household.domain.Household;
import app.lifelinq.features.household.domain.HouseholdRepository;
import app.lifelinq.features.household.domain.Membership;
import app.lifelinq.features.household.domain.MembershipRepository;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

public final class HouseholdPersistenceAdapter
        implements HouseholdRepository, MembershipRepository {

    private final InMemoryHouseholdRepository householdRepository;
    private final InMemoryMembershipRepository membershipRepository;

    public HouseholdPersistenceAdapter() {
        this.householdRepository = new InMemoryHouseholdRepository();
        this.membershipRepository = new InMemoryMembershipRepository();
    }

    @Override
    public void save(Household household) {
        householdRepository.save(household);
    }

    @Override
    public Optional<Household> findById(UUID id) {
        return householdRepository.findById(id);
    }

    @Override
    public void save(Membership membership) {
        membershipRepository.save(membership);
    }

    @Override
    public List<Membership> findByHouseholdId(UUID householdId) {
        return membershipRepository.findByHouseholdId(householdId);
    }

    @Override
    public List<UUID> findHouseholdIdsByUserId(UUID userId) {
        return membershipRepository.findHouseholdIdsByUserId(userId);
    }

    @Override
    public boolean deleteByHouseholdIdAndUserId(UUID householdId, UUID userId) {
        return membershipRepository.deleteByHouseholdIdAndUserId(householdId, userId);
    }
}
