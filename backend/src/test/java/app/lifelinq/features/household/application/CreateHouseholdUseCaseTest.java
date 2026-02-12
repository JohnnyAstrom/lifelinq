package app.lifelinq.features.household.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.lifelinq.features.household.domain.Household;
import app.lifelinq.features.household.domain.HouseholdRepository;
import app.lifelinq.features.household.domain.Membership;
import app.lifelinq.features.household.domain.MembershipRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CreateHouseholdUseCaseTest {

    @Test
    void createsHouseholdAndReturnsId() {
        InMemoryHouseholdRepository householdRepository = new InMemoryHouseholdRepository();
        InMemoryMembershipRepository membershipRepository = new InMemoryMembershipRepository();
        CreateHouseholdUseCase useCase = new CreateHouseholdUseCase(householdRepository, membershipRepository);
        CreateHouseholdCommand command = new CreateHouseholdCommand("Home", UUID.randomUUID());

        CreateHouseholdResult result = useCase.execute(command);

        assertNotNull(result);
        assertNotNull(result.getHouseholdId());
        assertEquals(1, householdRepository.saved.size());
        assertEquals(1, membershipRepository.saved.size());
    }

    @Test
    void requiresCommand() {
        CreateHouseholdUseCase useCase = new CreateHouseholdUseCase(
                new InMemoryHouseholdRepository(),
                new InMemoryMembershipRepository()
        );
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));
    }

    @Test
    void requiresOwnerUserId() {
        CreateHouseholdUseCase useCase = new CreateHouseholdUseCase(
                new InMemoryHouseholdRepository(),
                new InMemoryMembershipRepository()
        );
        CreateHouseholdCommand command = new CreateHouseholdCommand("Home", null);
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
    }

    private static final class InMemoryHouseholdRepository implements HouseholdRepository {
        private final List<Household> saved = new ArrayList<>();

        @Override
        public void save(Household household) {
            saved.add(household);
        }

        @Override
        public java.util.Optional<Household> findById(UUID id) {
            for (Household household : saved) {
                if (id.equals(household.getId())) {
                    return java.util.Optional.of(household);
                }
            }
            return java.util.Optional.empty();
        }
    }

    private static final class InMemoryMembershipRepository implements MembershipRepository {
        private final List<Membership> saved = new ArrayList<>();

        @Override
        public void save(Membership membership) {
            saved.add(membership);
        }

        @Override
        public List<Membership> findByHouseholdId(UUID householdId) {
            List<Membership> result = new ArrayList<>();
            for (Membership membership : saved) {
                if (householdId.equals(membership.getHouseholdId())) {
                    result.add(membership);
                }
            }
            return result;
        }

        @Override
        public List<UUID> findHouseholdIdsByUserId(UUID userId) {
            List<UUID> result = new ArrayList<>();
            for (Membership membership : saved) {
                if (userId.equals(membership.getUserId()) && !result.contains(membership.getHouseholdId())) {
                    result.add(membership.getHouseholdId());
                }
            }
            return result;
        }

        @Override
        public boolean deleteByHouseholdIdAndUserId(UUID householdId, UUID userId) {
            for (int i = 0; i < saved.size(); i++) {
                Membership membership = saved.get(i);
                if (householdId.equals(membership.getHouseholdId()) && userId.equals(membership.getUserId())) {
                    saved.remove(i);
                    return true;
                }
            }
            return false;
        }
    }
}
