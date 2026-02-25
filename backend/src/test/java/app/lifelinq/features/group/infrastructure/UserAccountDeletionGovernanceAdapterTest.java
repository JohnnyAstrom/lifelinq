package app.lifelinq.features.group.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.features.group.contract.UserGroupMembershipView;
import app.lifelinq.features.group.domain.Group;
import app.lifelinq.features.group.domain.GroupRole;
import app.lifelinq.features.group.domain.Membership;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserAccountDeletionGovernanceAdapterTest {

    @Test
    void findsMembershipsForUserWithPerGroupCounts() {
        InMemoryMembershipRepository membershipRepository = new InMemoryMembershipRepository();
        InMemoryGroupRepository groupRepository = new InMemoryGroupRepository();
        UUID userId = UUID.randomUUID();
        UUID groupA = UUID.randomUUID();
        UUID groupB = UUID.randomUUID();
        groupRepository.save(new Group(groupA, "A"));
        groupRepository.save(new Group(groupB, "B"));
        membershipRepository.save(new Membership(groupA, userId, GroupRole.ADMIN));
        membershipRepository.save(new Membership(groupA, UUID.randomUUID(), GroupRole.MEMBER));
        membershipRepository.save(new Membership(groupB, userId, GroupRole.MEMBER));
        membershipRepository.save(new Membership(groupB, UUID.randomUUID(), GroupRole.ADMIN));
        membershipRepository.save(new Membership(groupB, UUID.randomUUID(), GroupRole.MEMBER));

        UserAccountDeletionGovernanceAdapter adapter =
                new UserAccountDeletionGovernanceAdapter(membershipRepository, groupRepository);

        List<UserGroupMembershipView> snapshots = adapter.findMembershipsForUser(userId);

        assertEquals(2, snapshots.size());
        UserGroupMembershipView a = snapshots.stream().filter(s -> s.groupId().equals(groupA)).findFirst().orElseThrow();
        UserGroupMembershipView b = snapshots.stream().filter(s -> s.groupId().equals(groupB)).findFirst().orElseThrow();
        assertTrue(a.admin());
        assertEquals(2, a.memberCount());
        assertEquals(1, a.adminCount());
        assertFalse(b.admin());
        assertEquals(3, b.memberCount());
        assertEquals(1, b.adminCount());
    }

    @Test
    void deletesMembershipsByUserId() {
        InMemoryMembershipRepository membershipRepository = new InMemoryMembershipRepository();
        InMemoryGroupRepository groupRepository = new InMemoryGroupRepository();
        UUID userId = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        membershipRepository.save(new Membership(UUID.randomUUID(), userId, GroupRole.MEMBER));
        membershipRepository.save(new Membership(UUID.randomUUID(), userId, GroupRole.ADMIN));
        membershipRepository.save(new Membership(UUID.randomUUID(), other, GroupRole.MEMBER));
        UserAccountDeletionGovernanceAdapter adapter =
                new UserAccountDeletionGovernanceAdapter(membershipRepository, groupRepository);

        adapter.deleteMembershipsByUserId(userId);

        assertEquals(0, membershipRepository.findByUserId(userId).size());
        assertEquals(1, membershipRepository.findByUserId(other).size());
    }

    @Test
    void deletesOnlyGroupsThatBecomeEmpty() {
        InMemoryMembershipRepository membershipRepository = new InMemoryMembershipRepository();
        InMemoryGroupRepository groupRepository = new InMemoryGroupRepository();
        UUID emptyGroup = UUID.randomUUID();
        UUID nonEmptyGroup = UUID.randomUUID();
        groupRepository.save(new Group(emptyGroup, "Empty"));
        groupRepository.save(new Group(nonEmptyGroup, "NonEmpty"));
        membershipRepository.save(new Membership(nonEmptyGroup, UUID.randomUUID(), GroupRole.MEMBER));
        UserAccountDeletionGovernanceAdapter adapter =
                new UserAccountDeletionGovernanceAdapter(membershipRepository, groupRepository);

        adapter.deleteEmptyGroupsByIds(List.of(emptyGroup, nonEmptyGroup, emptyGroup));

        assertFalse(groupRepository.findById(emptyGroup).isPresent());
        assertTrue(groupRepository.findById(nonEmptyGroup).isPresent());
    }
}
