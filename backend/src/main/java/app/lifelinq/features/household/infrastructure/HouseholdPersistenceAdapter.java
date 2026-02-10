package app.lifelinq.features.household.infrastructure;

import app.lifelinq.features.household.domain.Household;
import app.lifelinq.features.household.domain.HouseholdRepository;
import app.lifelinq.features.household.domain.Invitation;
import app.lifelinq.features.household.domain.InvitationRepository;
import app.lifelinq.features.household.domain.Membership;
import app.lifelinq.features.household.domain.MembershipRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class HouseholdPersistenceAdapter
        implements HouseholdRepository, MembershipRepository, InvitationRepository {

    private final InMemoryHouseholdRepository householdRepository;
    private final InMemoryMembershipRepository membershipRepository;
    private final InMemoryInvitationRepository invitationRepository;

    public HouseholdPersistenceAdapter() {
        this.householdRepository = new InMemoryHouseholdRepository();
        this.membershipRepository = new InMemoryMembershipRepository();
        this.invitationRepository = new InMemoryInvitationRepository();
    }

    @Override
    public void save(Household household) {
        householdRepository.save(household);
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
    public boolean deleteByHouseholdIdAndUserId(UUID householdId, UUID userId) {
        return membershipRepository.deleteByHouseholdIdAndUserId(householdId, userId);
    }

    @Override
    public void save(Invitation invitation) {
        invitationRepository.save(invitation);
    }

    @Override
    public Optional<Invitation> findByToken(String token) {
        return invitationRepository.findByToken(token);
    }

    @Override
    public boolean existsByToken(String token) {
        return invitationRepository.existsByToken(token);
    }
}
