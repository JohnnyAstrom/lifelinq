package app.lifelinq.features.user.application;

import app.lifelinq.features.user.contract.UserAccountDeletion;
import app.lifelinq.features.user.contract.UserActiveGroupRead;
import app.lifelinq.features.user.contract.UserActiveGroupSelection;
import app.lifelinq.features.user.contract.UserProvisioning;
import app.lifelinq.features.user.domain.UserRepository;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class UserApplicationService implements
        UserProvisioning,
        UserAccountDeletion,
        UserActiveGroupSelection,
        UserActiveGroupRead {
    private final EnsureUserExistsUseCase ensureUserExistsUseCase;
    private final DeleteAccountUseCase deleteAccountUseCase;
    private final UserRepository userRepository;

    public UserApplicationService(
            EnsureUserExistsUseCase ensureUserExistsUseCase,
            DeleteAccountUseCase deleteAccountUseCase,
            UserRepository userRepository
    ) {
        if (ensureUserExistsUseCase == null) {
            throw new IllegalArgumentException("ensureUserExistsUseCase must not be null");
        }
        if (deleteAccountUseCase == null) {
            throw new IllegalArgumentException("deleteAccountUseCase must not be null");
        }
        if (userRepository == null) {
            throw new IllegalArgumentException("userRepository must not be null");
        }
        this.ensureUserExistsUseCase = ensureUserExistsUseCase;
        this.deleteAccountUseCase = deleteAccountUseCase;
        this.userRepository = userRepository;
    }

    @Override
    public void ensureUserExists(UUID userId) {
        ensureUserExistsUseCase.execute(userId);
    }

    @Transactional
    @Override
    public void deleteAccount(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        var memberships = deleteAccountUseCase.loadMemberships(userId);
        deleteAccountUseCase.validateGovernance(memberships);
        deleteAccountUseCase.deleteMemberships(userId);
        deleteAccountUseCase.deleteEmptyGroups(memberships);
        deleteAccountUseCase.deleteUser(userId);
    }

    @Transactional
    @Override
    public void setActiveGroup(UUID userId, UUID groupId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        if (groupId == null) {
            throw new IllegalArgumentException("groupId must not be null");
        }
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
        userRepository.save(user.withActiveGroupId(groupId));
    }

    @Override
    public UUID getActiveGroupId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        return userRepository.findById(userId)
                .map(app.lifelinq.features.user.domain.User::getActiveGroupId)
                .orElse(null);
    }
}
