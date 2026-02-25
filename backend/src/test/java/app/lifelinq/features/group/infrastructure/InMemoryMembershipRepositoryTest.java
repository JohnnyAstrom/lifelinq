package app.lifelinq.features.group.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import app.lifelinq.features.group.domain.GroupRole;
import app.lifelinq.features.group.domain.Membership;
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
    void requiresGroupIdForLookup() {
        InMemoryMembershipRepository repository = new InMemoryMembershipRepository();
        assertThrows(IllegalArgumentException.class, () -> repository.findByGroupId(null));
    }

    @Test
    void findsByGroupId() {
        InMemoryMembershipRepository repository = new InMemoryMembershipRepository();
        UUID groupId = UUID.randomUUID();
        UUID otherGroupId = UUID.randomUUID();

        repository.save(new Membership(groupId, UUID.randomUUID(), GroupRole.MEMBER));
        repository.save(new Membership(groupId, UUID.randomUUID(), GroupRole.ADMIN));
        repository.save(new Membership(otherGroupId, UUID.randomUUID(), GroupRole.MEMBER));

        List<Membership> result = repository.findByGroupId(groupId);

        assertEquals(2, result.size());
        assertEquals(groupId, result.get(0).getGroupId());
        assertEquals(groupId, result.get(1).getGroupId());
    }

    @Test
    void deletesByGroupIdAndUserId() {
        InMemoryMembershipRepository repository = new InMemoryMembershipRepository();
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        repository.save(new Membership(groupId, userId, GroupRole.MEMBER));

        boolean removed = repository.deleteByGroupIdAndUserId(groupId, userId);

        assertEquals(true, removed);
        assertEquals(0, repository.findByGroupId(groupId).size());
    }

    @Test
    void findsGroupIdsByUserId() {
        InMemoryMembershipRepository repository = new InMemoryMembershipRepository();
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        repository.save(new Membership(groupId, userId, GroupRole.MEMBER));

        List<UUID> result = repository.findGroupIdsByUserId(userId);

        assertEquals(List.of(groupId), result);
    }
}
