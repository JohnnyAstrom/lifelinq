package app.lifelinq.features.auth.application;

import app.lifelinq.config.JwtSigner;
import app.lifelinq.features.auth.contract.UserContextView;
import app.lifelinq.features.auth.contract.UserMembershipView;
import app.lifelinq.features.group.contract.UserDefaultGroupProvisioning;
import app.lifelinq.features.group.contract.UserGroupMembershipLookup;
import app.lifelinq.features.user.contract.UserAccountDeletion;
import app.lifelinq.features.user.contract.UserActiveGroupRead;
import app.lifelinq.features.user.contract.UserActiveGroupSelection;
import app.lifelinq.features.user.contract.UserProfileRead;
import app.lifelinq.features.user.contract.UserProvisioning;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class AuthApplicationService {
    private final UserProvisioning userProvisioning;
    private final UserAccountDeletion userAccountDeletion;
    private final UserActiveGroupSelection userActiveGroupSelection;
    private final UserActiveGroupRead userActiveGroupRead;
    private final UserProfileRead userProfileRead;
    private final UserDefaultGroupProvisioning userDefaultGroupProvisioning;
    private final UserGroupMembershipLookup userGroupMembershipLookup;
    private final JwtSigner jwtSigner;

    public AuthApplicationService(
            UserProvisioning userProvisioning,
            UserAccountDeletion userAccountDeletion,
            UserActiveGroupSelection userActiveGroupSelection,
            UserActiveGroupRead userActiveGroupRead,
            UserProfileRead userProfileRead,
            UserDefaultGroupProvisioning userDefaultGroupProvisioning,
            UserGroupMembershipLookup userGroupMembershipLookup,
            JwtSigner jwtSigner
    ) {
        if (userProvisioning == null) {
            throw new IllegalArgumentException("userProvisioning must not be null");
        }
        if (userAccountDeletion == null) {
            throw new IllegalArgumentException("userAccountDeletion must not be null");
        }
        if (userActiveGroupSelection == null) {
            throw new IllegalArgumentException("userActiveGroupSelection must not be null");
        }
        if (userActiveGroupRead == null) {
            throw new IllegalArgumentException("userActiveGroupRead must not be null");
        }
        if (userProfileRead == null) {
            throw new IllegalArgumentException("userProfileRead must not be null");
        }
        if (userDefaultGroupProvisioning == null) {
            throw new IllegalArgumentException("userDefaultGroupProvisioning must not be null");
        }
        if (userGroupMembershipLookup == null) {
            throw new IllegalArgumentException("userGroupMembershipLookup must not be null");
        }
        if (jwtSigner == null) {
            throw new IllegalArgumentException("jwtSigner must not be null");
        }
        this.userProvisioning = userProvisioning;
        this.userAccountDeletion = userAccountDeletion;
        this.userActiveGroupSelection = userActiveGroupSelection;
        this.userActiveGroupRead = userActiveGroupRead;
        this.userProfileRead = userProfileRead;
        this.userDefaultGroupProvisioning = userDefaultGroupProvisioning;
        this.userGroupMembershipLookup = userGroupMembershipLookup;
        this.jwtSigner = jwtSigner;
    }

    public String devLogin(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email must not be blank");
        }
        UUID userId = UUID.nameUUIDFromBytes(email.trim().toLowerCase().getBytes(StandardCharsets.UTF_8));
        return ensureProvisionedAndSignToken(userId);
    }

    @Transactional
    public String ensureProvisionedAndSignToken(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        userProvisioning.ensureUserExists(userId);
        UUID groupId = userDefaultGroupProvisioning.ensureDefaultGroupProvisioned(userId);
        if (userActiveGroupRead.getActiveGroupId(userId) == null) {
            userActiveGroupSelection.setActiveGroup(userId, groupId);
        }
        return jwtSigner.sign(userId);
    }

    public void deleteAccount(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        userAccountDeletion.deleteAccount(userId);
    }

    public UserContextView getMe(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        return buildUserContext(userId);
    }

    public UserContextView setActiveGroup(UUID userId, UUID groupId) {
        if (userId == null || groupId == null) {
            throw new IllegalArgumentException("userId/groupId must not be null");
        }
        if (!userGroupMembershipLookup.isMember(userId, groupId)) {
            throw new ActiveGroupSelectionConflictException("Selected group is not a membership of the current user");
        }
        userActiveGroupSelection.setActiveGroup(userId, groupId);
        return buildUserContext(userId);
    }

    private UserContextView buildUserContext(UUID userId) {
        UUID activeGroupId = userActiveGroupRead.getActiveGroupId(userId);
        var profile = userProfileRead.getProfile(userId);
        List<UserMembershipView> memberships = new ArrayList<>();
        for (var membership : userGroupMembershipLookup.listMemberships(userId)) {
            memberships.add(new UserMembershipView(membership.groupId(), membership.groupName(), membership.role()));
        }
        return new UserContextView(userId, activeGroupId, profile.firstName(), profile.lastName(), memberships);
    }
}
