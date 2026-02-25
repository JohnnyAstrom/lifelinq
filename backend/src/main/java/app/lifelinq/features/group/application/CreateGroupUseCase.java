package app.lifelinq.features.group.application;

import app.lifelinq.features.group.domain.Group;
import app.lifelinq.features.group.domain.GroupRepository;
import app.lifelinq.features.group.domain.GroupRole;
import app.lifelinq.features.group.domain.Membership;
import app.lifelinq.features.group.domain.MembershipRepository;
import java.util.UUID;

final class CreateGroupUseCase {
    private final GroupRepository groupRepository;
    private final MembershipRepository membershipRepository;

    public CreateGroupUseCase(GroupRepository groupRepository, MembershipRepository membershipRepository) {
        if (groupRepository == null) {
            throw new IllegalArgumentException("groupRepository must not be null");
        }
        if (membershipRepository == null) {
            throw new IllegalArgumentException("membershipRepository must not be null");
        }
        this.groupRepository = groupRepository;
        this.membershipRepository = membershipRepository;
    }

    public CreateGroupResult execute(CreateGroupCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        if (command.getOwnerUserId() == null) {
            throw new IllegalArgumentException("ownerUserId must not be null");
        }

        UUID groupId = UUID.randomUUID();
        Group group = new Group(groupId, command.getGroupName());
        Membership ownerMembership = new Membership(group.getId(), command.getOwnerUserId(), GroupRole.OWNER);

        groupRepository.save(group);
        membershipRepository.save(ownerMembership);

        return new CreateGroupResult(group.getId());
    }
}
