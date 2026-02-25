package app.lifelinq.features.group.application;

import app.lifelinq.features.group.domain.GroupRole;
import app.lifelinq.features.group.domain.Membership;
import app.lifelinq.features.group.domain.MembershipRepository;

final class AddMemberToGroupUseCase {
    private final MembershipRepository membershipRepository;

    public AddMemberToGroupUseCase(MembershipRepository membershipRepository) {
        if (membershipRepository == null) {
            throw new IllegalArgumentException("membershipRepository must not be null");
        }
        this.membershipRepository = membershipRepository;
    }

    public AddMemberToGroupResult execute(AddMemberToGroupCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        if (command.getGroupId() == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        if (command.getUserId() == null) {
            throw new IllegalArgumentException("userId must not be null");
        }

        Membership membership = new Membership(
                command.getGroupId(),
                command.getUserId(),
                GroupRole.MEMBER
        );

        membershipRepository.save(membership);

        return new AddMemberToGroupResult(
                membership.getGroupId(),
                membership.getUserId(),
                membership.getRole()
        );
    }
}
