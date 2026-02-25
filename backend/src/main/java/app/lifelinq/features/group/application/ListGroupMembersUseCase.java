package app.lifelinq.features.group.application;

import app.lifelinq.features.group.domain.Membership;
import app.lifelinq.features.group.domain.MembershipRepository;
import java.util.List;

final class ListGroupMembersUseCase {
    private final MembershipRepository membershipRepository;

    ListGroupMembersUseCase(MembershipRepository membershipRepository) {
        if (membershipRepository == null) {
            throw new IllegalArgumentException("membershipRepository must not be null");
        }
        this.membershipRepository = membershipRepository;
    }

    ListGroupMembersResult execute(ListGroupMembersCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        if (command.getGroupId() == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }

        List<Membership> members = membershipRepository.findByGroupId(command.getGroupId());
        return new ListGroupMembersResult(members);
    }
}
