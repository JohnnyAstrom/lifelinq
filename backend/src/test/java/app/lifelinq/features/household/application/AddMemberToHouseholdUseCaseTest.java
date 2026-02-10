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

class AddMemberToHouseholdUseCaseTest {

    @Test
    void addsMemberWithMemberRole() {
        InMemoryMembershipRepository repository = new InMemoryMembershipRepository();
        AddMemberToHouseholdUseCase useCase = new AddMemberToHouseholdUseCase(repository);
        UUID householdId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        AddMemberToHouseholdCommand command = new AddMemberToHouseholdCommand(householdId, userId);

        AddMemberToHouseholdResult result = useCase.execute(command);

        assertEquals(householdId, result.getHouseholdId());
        assertEquals(userId, result.getUserId());
        assertEquals(HouseholdRole.MEMBER, result.getRole());
        assertEquals(1, repository.saved.size());
    }

    @Test
    void requiresCommand() {
        AddMemberToHouseholdUseCase useCase = new AddMemberToHouseholdUseCase(new InMemoryMembershipRepository());
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));
    }

    @Test
    void requiresHouseholdId() {
        AddMemberToHouseholdUseCase useCase = new AddMemberToHouseholdUseCase(new InMemoryMembershipRepository());
        AddMemberToHouseholdCommand command = new AddMemberToHouseholdCommand(null, UUID.randomUUID());
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
    }

    @Test
    void requiresUserId() {
        AddMemberToHouseholdUseCase useCase = new AddMemberToHouseholdUseCase(new InMemoryMembershipRepository());
        AddMemberToHouseholdCommand command = new AddMemberToHouseholdCommand(UUID.randomUUID(), null);
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
    }
}
