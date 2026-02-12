package app.lifelinq.features.household.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.lifelinq.features.household.domain.HouseholdRole;
import app.lifelinq.features.household.domain.Membership;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InMemoryMembershipRepositoryTest {

    @Test
    void requiresMembership() {
        InMemoryMembershipRepository repository = new InMemoryMembershipRepository();
        assertThrows(IllegalArgumentException.class, () -> repository.save(null));
    }

    @Test
    void requiresHouseholdIdForLookup() {
        InMemoryMembershipRepository repository = new InMemoryMembershipRepository();
        assertThrows(IllegalArgumentException.class, () -> repository.findByHouseholdId(null));
    }

    @Test
    void findsByHouseholdId() {
        InMemoryMembershipRepository repository = new InMemoryMembershipRepository();
        UUID householdId = UUID.randomUUID();
        UUID otherHouseholdId = UUID.randomUUID();

        repository.save(new Membership(householdId, UUID.randomUUID(), HouseholdRole.MEMBER));
        repository.save(new Membership(householdId, UUID.randomUUID(), HouseholdRole.OWNER));
        repository.save(new Membership(otherHouseholdId, UUID.randomUUID(), HouseholdRole.MEMBER));

        List<Membership> result = repository.findByHouseholdId(householdId);

        assertEquals(2, result.size());
        assertEquals(householdId, result.get(0).getHouseholdId());
        assertEquals(householdId, result.get(1).getHouseholdId());
    }

    @Test
    void deletesByHouseholdIdAndUserId() {
        InMemoryMembershipRepository repository = new InMemoryMembershipRepository();
        UUID householdId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        repository.save(new Membership(householdId, userId, HouseholdRole.MEMBER));

        boolean removed = repository.deleteByHouseholdIdAndUserId(householdId, userId);

        assertEquals(true, removed);
        assertEquals(0, repository.findByHouseholdId(householdId).size());
    }

    @Test
    void findsHouseholdIdsByUserId() {
        InMemoryMembershipRepository repository = new InMemoryMembershipRepository();
        UUID householdId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        repository.save(new Membership(householdId, userId, HouseholdRole.MEMBER));

        List<UUID> result = repository.findHouseholdIdsByUserId(userId);

        assertEquals(List.of(householdId), result);
    }
}
