package app.lifelinq.features.group.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.features.group.domain.Group;
import app.lifelinq.features.group.domain.GroupRepository;
import app.lifelinq.features.group.domain.GroupRole;
import app.lifelinq.features.group.domain.Invitation;
import app.lifelinq.features.group.domain.InvitationRepository;
import app.lifelinq.features.group.domain.Membership;
import app.lifelinq.features.group.domain.MembershipRepository;
import app.lifelinq.features.group.contract.AccessDeniedException;
import app.lifelinq.features.user.contract.UserProfileView;
import app.lifelinq.features.user.contract.UserProvisioning;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GroupApplicationServicePlaceGovernanceTest {

    @Test
    void leaveCurrentPlaceBlocksDefaultPlace() {
        InMemoryGroupRepository groupRepository = new InMemoryGroupRepository();
        InMemoryMembershipRepository membershipRepository = new InMemoryMembershipRepository();
        FakeActiveGroups activeGroups = new FakeActiveGroups();
        GroupApplicationService service = createService(groupRepository, membershipRepository, activeGroups);

        UUID userId = UUID.randomUUID();
        UUID defaultGroupId = GroupApplicationService.defaultGroupIdFor(userId);
        UUID otherGroupId = UUID.randomUUID();
        membershipRepository.save(new Membership(defaultGroupId, userId, GroupRole.ADMIN));
        membershipRepository.save(new Membership(otherGroupId, userId, GroupRole.MEMBER));

        assertThrows(IllegalStateException.class, () -> service.leaveCurrentPlace(defaultGroupId, userId));
    }

    @Test
    void leaveCurrentPlaceReassignsActiveGroupAndDeletesPlaceWhenLastMemberLeaves() {
        InMemoryGroupRepository groupRepository = new InMemoryGroupRepository();
        InMemoryMembershipRepository membershipRepository = new InMemoryMembershipRepository();
        FakeActiveGroups activeGroups = new FakeActiveGroups();
        GroupApplicationService service = createService(groupRepository, membershipRepository, activeGroups);

        UUID userId = UUID.randomUUID();
        UUID leavingGroupId = UUID.randomUUID();
        UUID fallbackGroupId = UUID.randomUUID();
        groupRepository.save(new Group(leavingGroupId, "Leaving"));
        groupRepository.save(new Group(fallbackGroupId, "Fallback"));
        membershipRepository.save(new Membership(leavingGroupId, userId, GroupRole.ADMIN));
        membershipRepository.save(new Membership(fallbackGroupId, userId, GroupRole.MEMBER));
        activeGroups.setActiveGroup(userId, leavingGroupId);

        service.leaveCurrentPlace(leavingGroupId, userId);

        assertTrue(groupRepository.findById(leavingGroupId).isEmpty());
        assertEquals(fallbackGroupId, activeGroups.getActiveGroupId(userId));
    }

    @Test
    void deleteCurrentPlaceClearsActiveGroupForAffectedUsers() {
        InMemoryGroupRepository groupRepository = new InMemoryGroupRepository();
        InMemoryMembershipRepository membershipRepository = new InMemoryMembershipRepository();
        FakeActiveGroups activeGroups = new FakeActiveGroups();
        GroupApplicationService service = createService(groupRepository, membershipRepository, activeGroups);

        UUID actorUserId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID deletedGroupId = UUID.randomUUID();
        UUID actorOtherGroupId = UUID.randomUUID();

        groupRepository.save(new Group(deletedGroupId, "To delete"));
        groupRepository.save(new Group(actorOtherGroupId, "Other"));
        membershipRepository.save(new Membership(deletedGroupId, actorUserId, GroupRole.ADMIN));
        membershipRepository.save(new Membership(deletedGroupId, otherUserId, GroupRole.MEMBER));
        membershipRepository.save(new Membership(actorOtherGroupId, actorUserId, GroupRole.MEMBER));
        activeGroups.setActiveGroup(actorUserId, deletedGroupId);
        activeGroups.setActiveGroup(otherUserId, deletedGroupId);

        service.deleteCurrentPlace(deletedGroupId, actorUserId);

        assertTrue(groupRepository.findById(deletedGroupId).isEmpty());
        assertNull(activeGroups.getActiveGroupId(actorUserId));
        assertNull(activeGroups.getActiveGroupId(otherUserId));
        assertEquals(0, membershipRepository.findByGroupId(deletedGroupId).size());
    }

    @Test
    void renameCurrentPlaceRequiresAdmin() {
        InMemoryGroupRepository groupRepository = new InMemoryGroupRepository();
        InMemoryMembershipRepository membershipRepository = new InMemoryMembershipRepository();
        FakeActiveGroups activeGroups = new FakeActiveGroups();
        GroupApplicationService service = createService(groupRepository, membershipRepository, activeGroups);

        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        groupRepository.save(new Group(groupId, "Original"));
        membershipRepository.save(new Membership(groupId, userId, GroupRole.MEMBER));

        assertThrows(AccessDeniedException.class, () -> service.renameCurrentPlace(groupId, userId, "Renamed"));
    }

    private GroupApplicationService createService(
            GroupRepository groupRepository,
            MembershipRepository membershipRepository,
            FakeActiveGroups activeGroups
    ) {
        InvitationRepository invitationRepository = new StubInvitationRepository();
        UserProvisioning userProvisioning = userId -> {
        };
        var userProfileRead = (app.lifelinq.features.user.contract.UserProfileRead) userId ->
                new UserProfileView(null, null);
        return GroupApplicationService.create(
                groupRepository,
                membershipRepository,
                invitationRepository,
                () -> "token",
                () -> "ABC123",
                userProvisioning,
                activeGroups,
                activeGroups,
                userProfileRead,
                Clock.fixed(Instant.parse("2026-02-25T00:00:00Z"), ZoneOffset.UTC)
        );
    }

    private static final class FakeActiveGroups implements
            app.lifelinq.features.user.contract.UserActiveGroupRead,
            app.lifelinq.features.user.contract.UserActiveGroupSelection {
        private final Map<UUID, UUID> byUserId = new HashMap<>();

        @Override
        public UUID getActiveGroupId(UUID userId) {
            return byUserId.get(userId);
        }

        @Override
        public void setActiveGroup(UUID userId, UUID groupId) {
            byUserId.put(userId, groupId);
        }

        @Override
        public void clearActiveGroup(UUID userId) {
            byUserId.put(userId, null);
        }
    }

    private static final class InMemoryGroupRepository implements GroupRepository {
        private final Map<UUID, Group> groups = new LinkedHashMap<>();

        @Override
        public void save(Group group) {
            groups.put(group.getId(), group);
        }

        @Override
        public Optional<Group> findById(UUID id) {
            return Optional.ofNullable(groups.get(id));
        }

        @Override
        public void deleteById(UUID id) {
            groups.remove(id);
        }
    }

    private static final class InMemoryMembershipRepository implements MembershipRepository {
        private final List<Membership> memberships = new ArrayList<>();

        @Override
        public void save(Membership membership) {
            memberships.removeIf(m -> m.getId().equals(membership.getId()));
            memberships.add(membership);
        }

        @Override
        public List<Membership> findByGroupId(UUID groupId) {
            List<Membership> result = new ArrayList<>();
            for (Membership membership : memberships) {
                if (membership.getGroupId().equals(groupId)) {
                    result.add(membership);
                }
            }
            return result;
        }

        @Override
        public List<Membership> findByUserId(UUID userId) {
            List<Membership> result = new ArrayList<>();
            for (Membership membership : memberships) {
                if (membership.getUserId().equals(userId)) {
                    result.add(membership);
                }
            }
            return result;
        }

        @Override
        public List<UUID> findGroupIdsByUserId(UUID userId) {
            LinkedHashSet<UUID> ids = new LinkedHashSet<>();
            for (Membership membership : memberships) {
                if (membership.getUserId().equals(userId)) {
                    ids.add(membership.getGroupId());
                }
            }
            return new ArrayList<>(ids);
        }

        @Override
        public boolean deleteByGroupIdAndUserId(UUID groupId, UUID userId) {
            for (int i = 0; i < memberships.size(); i++) {
                Membership membership = memberships.get(i);
                if (membership.getGroupId().equals(groupId) && membership.getUserId().equals(userId)) {
                    memberships.remove(i);
                    return true;
                }
            }
            return false;
        }

        @Override
        public void deleteByGroupId(UUID groupId) {
            memberships.removeIf(membership -> membership.getGroupId().equals(groupId));
        }

        @Override
        public void deleteByUserId(UUID userId) {
            memberships.removeIf(membership -> membership.getUserId().equals(userId));
        }
    }

    private static final class StubInvitationRepository implements InvitationRepository {
        @Override
        public void save(Invitation invitation) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public Optional<Invitation> findByToken(String token) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public Optional<Invitation> findById(UUID id) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public Optional<Invitation> findByShortCode(String shortCode) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public boolean existsByToken(String token) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public boolean existsByShortCode(String shortCode) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public List<Invitation> findActive() {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public Optional<Invitation> findActiveByGroupIdAndInviteeEmail(UUID groupId, String inviteeEmail) {
            throw new UnsupportedOperationException("not used");
        }
    }
}
