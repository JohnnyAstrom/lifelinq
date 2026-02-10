package app.lifelinq.features.household.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.lifelinq.features.household.domain.HouseholdRole;
import app.lifelinq.features.household.domain.Membership;
import app.lifelinq.features.household.domain.MembershipRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ListHouseholdMembersUseCaseTest {

    @Test
    void returnsOnlyMembersForHousehold() {
        InMemoryMembershipRepository repository = new InMemoryMembershipRepository();
        ListHouseholdMembersUseCase useCase = new ListHouseholdMembersUseCase(repository);
        UUID householdId = UUID.randomUUID();
        UUID otherHouseholdId = UUID.randomUUID();

        repository.save(new Membership(householdId, UUID.randomUUID(), HouseholdRole.MEMBER));
        repository.save(new Membership(householdId, UUID.randomUUID(), HouseholdRole.OWNER));
        repository.save(new Membership(otherHouseholdId, UUID.randomUUID(), HouseholdRole.MEMBER));

        ListHouseholdMembersCommand command = new ListHouseholdMembersCommand(householdId);

        ListHouseholdMembersResult result = useCase.execute(command);

        assertEquals(2, result.getMembers().size());
        assertEquals(householdId, result.getMembers().get(0).getHouseholdId());
        assertEquals(householdId, result.getMembers().get(1).getHouseholdId());
    }

    @Test
    void requiresCommand() {
        ListHouseholdMembersUseCase useCase = new ListHouseholdMembersUseCase(new InMemoryMembershipRepository());
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));
    }

    @Test
    void requiresHouseholdId() {
        ListHouseholdMembersUseCase useCase = new ListHouseholdMembersUseCase(new InMemoryMembershipRepository());
        ListHouseholdMembersCommand command = new ListHouseholdMembersCommand(null);
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
    }

    @Test
    void requiresMembershipRepository() {
        assertThrows(IllegalArgumentException.class, () -> new ListHouseholdMembersUseCase(null));
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
    }
}
