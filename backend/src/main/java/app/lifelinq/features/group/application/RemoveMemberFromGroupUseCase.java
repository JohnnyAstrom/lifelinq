package app.lifelinq.features.group.application;

import app.lifelinq.features.group.domain.MembershipRepository;

final class RemoveMemberFromGroupUseCase {
    private final MembershipRepository membershipRepository;

    public RemoveMemberFromGroupUseCase(MembershipRepository membershipRepository) {
        if (membershipRepository == null) {
            throw new IllegalArgumentException("membershipRepository must not be null");
        }
        this.membershipRepository = membershipRepository;
    }

    public RemoveMemberFromGroupResult execute(RemoveMemberFromGroupCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        if (command.getGroupId() == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        if (command.getUserId() == null) {
            throw new IllegalArgumentException("userId must not be null");
        }

        boolean removed = membershipRepository.deleteByGroupIdAndUserId(
                command.getGroupId(),
                command.getUserId()
        );

        return new RemoveMemberFromGroupResult(removed);
    }
}
