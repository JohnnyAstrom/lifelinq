package app.lifelinq.features.group.infrastructure;

import app.lifelinq.features.group.contract.UserGroupMembershipLookup;
import app.lifelinq.features.group.contract.UserGroupMembershipSummary;
import app.lifelinq.features.group.domain.GroupRepository;
import app.lifelinq.features.group.domain.Membership;
import app.lifelinq.features.group.domain.MembershipRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class UserGroupMembershipLookupAdapter implements UserGroupMembershipLookup {
    private final MembershipRepository membershipRepository;
    private final GroupRepository groupRepository;

    public UserGroupMembershipLookupAdapter(MembershipRepository membershipRepository, GroupRepository groupRepository) {
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
    public boolean isMember(UUID userId, UUID groupId) {
        if (userId == null || groupId == null) {
            throw new IllegalArgumentException("userId/groupId must not be null");
        }
        for (Membership membership : membershipRepository.findByUserId(userId)) {
            if (groupId.equals(membership.getGroupId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<UserGroupMembershipSummary> listMemberships(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        List<UserGroupMembershipSummary> summaries = new ArrayList<>();
        for (Membership membership : membershipRepository.findByUserId(userId)) {
            String groupName = groupRepository.findById(membership.getGroupId())
                    .map(group -> group.getName())
                    .orElse(null);
            summaries.add(new UserGroupMembershipSummary(
                    membership.getGroupId(),
                    groupName,
                    membership.getRole().name()
            ));
        }
        return summaries;
    }
}
