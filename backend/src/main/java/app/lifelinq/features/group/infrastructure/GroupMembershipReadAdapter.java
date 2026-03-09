package app.lifelinq.features.group.infrastructure;

import app.lifelinq.features.group.contract.GroupMembershipReadPort;
import app.lifelinq.features.group.domain.Membership;
import app.lifelinq.features.group.domain.MembershipRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class GroupMembershipReadAdapter implements GroupMembershipReadPort {
    private final MembershipRepository membershipRepository;

    public GroupMembershipReadAdapter(MembershipRepository membershipRepository) {
        if (membershipRepository == null) {
            throw new IllegalArgumentException("membershipRepository must not be null");
        }
        this.membershipRepository = membershipRepository;
    }

    @Override
    public List<UUID> listMemberUserIds(UUID groupId) {
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        List<UUID> result = new ArrayList<>();
        for (Membership membership : membershipRepository.findByGroupId(groupId)) {
            result.add(membership.getUserId());
        }
        return result;
    }
}
