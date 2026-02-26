package app.lifelinq.features.group.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.features.group.domain.Group;
import app.lifelinq.features.group.domain.GroupRepository;
import app.lifelinq.features.group.domain.GroupRole;
import app.lifelinq.features.group.domain.Invitation;
import app.lifelinq.features.group.domain.InvitationRepository;
import app.lifelinq.features.group.domain.LastAdminRemovalException;
import app.lifelinq.features.group.domain.Membership;
import app.lifelinq.features.group.domain.MembershipRepository;
import app.lifelinq.features.user.contract.UserProvisioning;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GroupApplicationServiceGovernanceTest {

    @Test
    void removeMemberBlocksWhenAdminRemovesAnotherAdmin() {
        InMemoryMembershipRepository membershipRepository = new InMemoryMembershipRepository();
        UUID groupId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        membershipRepository.save(new Membership(groupId, actorUserId, GroupRole.ADMIN));
        membershipRepository.save(new Membership(groupId, targetUserId, GroupRole.ADMIN));

        GroupApplicationService service = createService(membershipRepository);

        assertThrows(
                AdminRemovalConflictException.class,
                () -> service.removeMember(groupId, actorUserId, targetUserId)
        );
    }

    @Test
    void removeMemberBlocksWhenTargetIsSoleAdminAndGroupHasMultipleMembers() {
        InMemoryMembershipRepository membershipRepository = new InMemoryMembershipRepository();
        UUID groupId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        membershipRepository.save(new Membership(groupId, actorUserId, GroupRole.MEMBER));
        membershipRepository.save(new Membership(groupId, targetUserId, GroupRole.ADMIN));

        GroupApplicationService service = createService(membershipRepository);

        assertThrows(
                LastAdminRemovalException.class,
                () -> service.removeMember(groupId, targetUserId, targetUserId)
        );
    }

    @Test
    void removeMemberAllowsRemovingSoleAdminWhenOnlyMember() {
        InMemoryMembershipRepository membershipRepository = new InMemoryMembershipRepository();
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        membershipRepository.save(new Membership(groupId, userId, GroupRole.ADMIN));

        GroupApplicationService service = createService(membershipRepository);

        boolean removed = service.removeMember(groupId, userId, userId);

        assertTrue(removed);
        assertEquals(List.of(), membershipRepository.findByGroupId(groupId));
    }

    @Test
    void removeMemberAllowsAdminToRemoveMember() {
        InMemoryMembershipRepository membershipRepository = new InMemoryMembershipRepository();
        UUID groupId = UUID.randomUUID();
        UUID actorUserId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        membershipRepository.save(new Membership(groupId, actorUserId, GroupRole.ADMIN));
        membershipRepository.save(new Membership(groupId, targetUserId, GroupRole.MEMBER));

        GroupApplicationService service = createService(membershipRepository);

        boolean removed = service.removeMember(groupId, actorUserId, targetUserId);

        assertTrue(removed);
        assertEquals(1, membershipRepository.findByGroupId(groupId).size());
    }

    private GroupApplicationService createService(MembershipRepository membershipRepository) {
        GroupRepository groupRepository = new StubGroupRepository();
        InvitationRepository invitationRepository = new StubInvitationRepository();
        UserProvisioning userProvisioning = userId -> {
        };
        var userActiveGroupSelection = (app.lifelinq.features.user.contract.UserActiveGroupSelection) (userId, groupId) -> {
        };
        return GroupApplicationService.create(
                groupRepository,
                membershipRepository,
                invitationRepository,
                () -> "token",
                userProvisioning,
                userActiveGroupSelection,
                Clock.fixed(Instant.parse("2026-02-25T00:00:00Z"), ZoneOffset.UTC)
        );
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
        public List<UUID> findGroupIdsByUserId(UUID userId) {
            List<UUID> result = new ArrayList<>();
            for (Membership membership : memberships) {
                if (membership.getUserId().equals(userId) && !result.contains(membership.getGroupId())) {
                    result.add(membership.getGroupId());
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
        public void deleteByUserId(UUID userId) {
            memberships.removeIf(membership -> membership.getUserId().equals(userId));
        }
    }

    private static final class StubGroupRepository implements GroupRepository {
        private final Map<UUID, Group> groups = new HashMap<>();

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

    private static final class StubInvitationRepository implements InvitationRepository {
        @Override
        public Optional<Invitation> findByToken(String token) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public Optional<Invitation> findById(UUID id) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public boolean existsByToken(String token) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public List<Invitation> findActive() {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public void save(Invitation invitation) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public Optional<Invitation> findActiveByGroupIdAndInviteeEmail(UUID groupId, String inviteeEmail) {
            throw new UnsupportedOperationException("not used");
        }
    }
}
