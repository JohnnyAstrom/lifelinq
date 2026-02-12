package app.lifelinq.features.household.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.features.household.domain.HouseholdRole;
import app.lifelinq.features.household.domain.Membership;
import app.lifelinq.features.household.domain.MembershipRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RemoveMemberFromHouseholdUseCaseTest {

    @Test
    void removesMemberWhenPresent() {
        InMemoryMembershipRepository repository = new InMemoryMembershipRepository();
        UUID householdId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        repository.save(new Membership(householdId, userId, HouseholdRole.MEMBER));

        RemoveMemberFromHouseholdUseCase useCase = new RemoveMemberFromHouseholdUseCase(repository);
        RemoveMemberFromHouseholdCommand command = new RemoveMemberFromHouseholdCommand(householdId, userId);

        RemoveMemberFromHouseholdResult result = useCase.execute(command);

        assertTrue(result.isRemoved());
        assertEquals(0, repository.saved.size());
    }

    @Test
    void returnsFalseWhenNoMatch() {
        InMemoryMembershipRepository repository = new InMemoryMembershipRepository();
        UUID householdId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        RemoveMemberFromHouseholdUseCase useCase = new RemoveMemberFromHouseholdUseCase(repository);
        RemoveMemberFromHouseholdCommand command = new RemoveMemberFromHouseholdCommand(householdId, userId);

        RemoveMemberFromHouseholdResult result = useCase.execute(command);

        assertFalse(result.isRemoved());
    }

    @Test
    void requiresCommand() {
        RemoveMemberFromHouseholdUseCase useCase = new RemoveMemberFromHouseholdUseCase(new InMemoryMembershipRepository());
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));
    }

    @Test
    void requiresHouseholdId() {
        RemoveMemberFromHouseholdUseCase useCase = new RemoveMemberFromHouseholdUseCase(new InMemoryMembershipRepository());
        RemoveMemberFromHouseholdCommand command = new RemoveMemberFromHouseholdCommand(null, UUID.randomUUID());
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
    }

    @Test
    void requiresUserId() {
        RemoveMemberFromHouseholdUseCase useCase = new RemoveMemberFromHouseholdUseCase(new InMemoryMembershipRepository());
        RemoveMemberFromHouseholdCommand command = new RemoveMemberFromHouseholdCommand(UUID.randomUUID(), null);
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
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
