package app.lifelinq.features.user.application;

import app.lifelinq.features.group.contract.GroupAccountDeletionGovernancePort;
import app.lifelinq.features.group.contract.UserGroupMembershipView;
import app.lifelinq.features.user.contract.DeleteAccountBlockedException;
import app.lifelinq.features.user.domain.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class DeleteAccountUseCase {
    private final GroupAccountDeletionGovernancePort groupGovernancePort;
    private final UserRepository userRepository;

    DeleteAccountUseCase(
            GroupAccountDeletionGovernancePort groupGovernancePort,
            UserRepository userRepository
    ) {
        if (groupGovernancePort == null) {
            throw new IllegalArgumentException("groupGovernancePort must not be null");
        }
        if (userRepository == null) {
            throw new IllegalArgumentException("userRepository must not be null");
        }
        this.groupGovernancePort = groupGovernancePort;
        this.userRepository = userRepository;
    }

    List<UserGroupMembershipView> loadMemberships(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        return groupGovernancePort.findMembershipsForUser(userId);
    }

    void validateGovernance(List<UserGroupMembershipView> memberships) {
        if (memberships == null) {
            throw new IllegalArgumentException("memberships must not be null");
        }
        for (UserGroupMembershipView membership : memberships) {
            if (membership.admin() && membership.adminCount() <= 1 && membership.memberCount() > 1) {
                throw new DeleteAccountBlockedException(
                        "Account deletion blocked: you are the sole admin in one or more groups"
                );
            }
        }
    }

    void deleteMemberships(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        groupGovernancePort.deleteMembershipsByUserId(userId);
    }

    void deleteEmptyGroups(List<UserGroupMembershipView> memberships) {
        if (memberships == null) {
            throw new IllegalArgumentException("memberships must not be null");
        }
        List<UUID> groupIds = new ArrayList<>();
        for (UserGroupMembershipView membership : memberships) {
            groupIds.add(membership.groupId());
        }
        groupGovernancePort.deleteEmptyGroupsByIds(groupIds);
    }

    void deleteUser(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        userRepository.deleteById(userId);
    }
}
