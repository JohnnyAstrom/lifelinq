package app.lifelinq.features.user.application;

import app.lifelinq.features.group.contract.GroupAccountDeletionGovernancePort;
import app.lifelinq.features.user.domain.UserRepository;
import java.util.List;

public final class UserApplicationServiceTestFactory {
    private UserApplicationServiceTestFactory() {
    }

    public static UserApplicationService create(UserRepository userRepository) {
        GroupAccountDeletionGovernancePort noOpGovernancePort = new GroupAccountDeletionGovernancePort() {
            @Override
            public List<app.lifelinq.features.group.contract.UserGroupMembershipView> findMembershipsForUser(
                    java.util.UUID userId
            ) {
                return List.of();
            }

            @Override
            public void deleteMembershipsByUserId(java.util.UUID userId) {
            }

            @Override
            public void deleteEmptyGroupsByIds(List<java.util.UUID> groupIds) {
            }
        };
        return new UserApplicationService(
                new EnsureUserExistsUseCase(userRepository),
                new DeleteAccountUseCase(noOpGovernancePort, userRepository),
                userRepository
        );
    }
}
