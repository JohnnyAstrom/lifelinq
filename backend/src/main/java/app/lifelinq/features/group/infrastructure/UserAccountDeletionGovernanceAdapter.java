package app.lifelinq.features.group.infrastructure;

import app.lifelinq.features.group.contract.GroupAccountDeletionGovernancePort;
import app.lifelinq.features.group.contract.UserGroupMembershipView;
import app.lifelinq.features.group.domain.GroupRepository;
import app.lifelinq.features.group.domain.GroupRole;
import app.lifelinq.features.group.domain.Membership;
import app.lifelinq.features.group.domain.MembershipRepository;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

public final class UserAccountDeletionGovernanceAdapter implements GroupAccountDeletionGovernancePort {
    private final MembershipRepository membershipRepository;
    private final GroupRepository groupRepository;

    public UserAccountDeletionGovernanceAdapter(
            MembershipRepository membershipRepository,
            GroupRepository groupRepository
    ) {
        if (membershipRepository == null) {
            throw new IllegalArgumentException("membershipRepository must not be null");
        }
        if (groupRepository == null) {
            throw new IllegalArgumentException("groupRepository must not be null");
        }
        this.membershipRepository = membershipRepository;
        this.groupRepository = groupRepository;
    }

    @Override
    public List<UserGroupMembershipView> findMembershipsForUser(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        List<UserGroupMembershipView> result = new ArrayList<>();
        for (Membership membership : membershipRepository.findByUserId(userId)) {
            List<Membership> groupMemberships = membershipRepository.findByGroupId(membership.getGroupId());
            int memberCount = groupMemberships.size();
            int adminCount = 0;
            for (Membership groupMembership : groupMemberships) {
                if (groupMembership.getRole() == GroupRole.ADMIN) {
                    adminCount++;
                }
            }
            result.add(new UserGroupMembershipView(
                    membership.getGroupId(),
                    membership.getRole() == GroupRole.ADMIN,
                    memberCount,
                    adminCount
            ));
        }
        return result;
    }

    @Override
    public void deleteMembershipsByUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        membershipRepository.deleteByUserId(userId);
    }

    @Override
    public void deleteEmptyGroupsByIds(List<UUID> groupIds) {
        if (groupIds == null) {
            throw new IllegalArgumentException("groupIds must not be null");
        }
        LinkedHashSet<UUID> uniqueIds = new LinkedHashSet<>(groupIds);
        for (UUID groupId : uniqueIds) {
            if (groupId == null) {
                throw new IllegalArgumentException("groupIds must not contain null");
            }
            if (membershipRepository.findByGroupId(groupId).isEmpty()) {
                groupRepository.deleteById(groupId);
            }
        }
    }
}
