package app.lifelinq.features.group.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.features.group.domain.GroupRole;
import app.lifelinq.features.group.domain.Membership;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = GroupJpaTestApplication.class)
@ActiveProfiles("test")
class JpaMembershipRepositoryAdapterTest {

    @Autowired
    private MembershipJpaRepository membershipJpaRepository;

    @Test
    void savesAndLoadsRoundTrip() {
        JpaMembershipRepositoryAdapter adapter = new JpaMembershipRepositoryAdapter(
                membershipJpaRepository,
                new MembershipMapper()
        );
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Membership membership = new Membership(groupId, userId, GroupRole.MEMBER);

        adapter.save(membership);
        List<Membership> loaded = adapter.findByGroupId(groupId);

        assertEquals(1, loaded.size());
        assertEquals(groupId, loaded.get(0).getGroupId());
        assertEquals(userId, loaded.get(0).getUserId());
        assertEquals(GroupRole.MEMBER, loaded.get(0).getRole());
    }

    @Test
    @Transactional
    void deletesByGroupAndUser() {
        JpaMembershipRepositoryAdapter adapter = new JpaMembershipRepositoryAdapter(
                membershipJpaRepository,
                new MembershipMapper()
        );
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        adapter.save(new Membership(groupId, userId, GroupRole.MEMBER));

        boolean removed = adapter.deleteByGroupIdAndUserId(groupId, userId);

        assertTrue(removed);
        assertEquals(0, adapter.findByGroupId(groupId).size());
    }

    @Test
    void findsGroupIdsByUser() {
        JpaMembershipRepositoryAdapter adapter = new JpaMembershipRepositoryAdapter(
                membershipJpaRepository,
                new MembershipMapper()
        );
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        adapter.save(new Membership(groupId, userId, GroupRole.MEMBER));

        List<UUID> ids = adapter.findGroupIdsByUserId(userId);

        assertEquals(List.of(groupId), ids);
    }

    @Test
    void findsByUserId() {
        JpaMembershipRepositoryAdapter adapter = new JpaMembershipRepositoryAdapter(
                membershipJpaRepository,
                new MembershipMapper()
        );
        UUID userId = UUID.randomUUID();
        adapter.save(new Membership(UUID.randomUUID(), userId, GroupRole.MEMBER));
        adapter.save(new Membership(UUID.randomUUID(), userId, GroupRole.ADMIN));
        adapter.save(new Membership(UUID.randomUUID(), UUID.randomUUID(), GroupRole.MEMBER));

        List<Membership> memberships = adapter.findByUserId(userId);

        assertEquals(2, memberships.size());
        assertTrue(memberships.stream().allMatch(m -> userId.equals(m.getUserId())));
    }

    @Test
    @Transactional
    void deletesByUserId() {
        JpaMembershipRepositoryAdapter adapter = new JpaMembershipRepositoryAdapter(
                membershipJpaRepository,
                new MembershipMapper()
        );
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        adapter.save(new Membership(UUID.randomUUID(), userId, GroupRole.MEMBER));
        adapter.save(new Membership(UUID.randomUUID(), userId, GroupRole.ADMIN));
        adapter.save(new Membership(UUID.randomUUID(), otherUserId, GroupRole.MEMBER));

        adapter.deleteByUserId(userId);

        assertEquals(0, adapter.findByUserId(userId).size());
        assertEquals(1, adapter.findByUserId(otherUserId).size());
    }
}
