package app.lifelinq.features.group.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.features.group.application.GroupApplicationService;
import app.lifelinq.features.group.contract.UserDefaultGroupProvisioning;
import app.lifelinq.features.group.domain.GroupRole;
import app.lifelinq.features.group.domain.Membership;
import app.lifelinq.features.user.contract.UserProvisioning;
import app.lifelinq.features.user.contract.UserProfileView;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserDefaultGroupProvisioningAdapterTest {

    @Test
    void createsPersonalGroupAndAdminMembershipForFirstProvisioning() {
        InMemoryGroupRepository groupRepository = new InMemoryGroupRepository();
        InMemoryMembershipRepository membershipRepository = new InMemoryMembershipRepository();
        UserDefaultGroupProvisioning adapter = adapter(groupRepository, membershipRepository);
        UUID userId = UUID.randomUUID();

        UUID groupId = adapter.ensureDefaultGroupProvisioned(userId);

        assertNotNull(groupId);
        assertEquals("Personal", groupRepository.findById(groupId).orElseThrow().getName());
        List<Membership> memberships = membershipRepository.findByUserId(userId);
        assertEquals(1, memberships.size());
        assertEquals(groupId, memberships.get(0).getGroupId());
        assertEquals(GroupRole.ADMIN, memberships.get(0).getRole());
    }

    @Test
    void firstProvisioningUsesProvidedInitialPlaceName() {
        InMemoryGroupRepository groupRepository = new InMemoryGroupRepository();
        InMemoryMembershipRepository membershipRepository = new InMemoryMembershipRepository();
        UserDefaultGroupProvisioning adapter = adapter(groupRepository, membershipRepository);
        UUID userId = UUID.randomUUID();

        UUID groupId = adapter.ensureDefaultGroupProvisioned(userId, "My Place");

        assertEquals("My Place", groupRepository.findById(groupId).orElseThrow().getName());
    }

    @Test
    void subsequentProvisioningIgnoresInitialPlaceNameAndDoesNotRename() {
        InMemoryGroupRepository groupRepository = new InMemoryGroupRepository();
        InMemoryMembershipRepository membershipRepository = new InMemoryMembershipRepository();
        UserDefaultGroupProvisioning adapter = adapter(groupRepository, membershipRepository);
        UUID userId = UUID.randomUUID();

        UUID groupId = adapter.ensureDefaultGroupProvisioned(userId, "First Name");
        UUID second = adapter.ensureDefaultGroupProvisioned(userId, "Second Name");

        assertEquals(groupId, second);
        assertEquals("First Name", groupRepository.findById(groupId).orElseThrow().getName());
    }

    @Test
    void blankInitialPlaceNameFallsBackToDefaultGroupName() {
        InMemoryGroupRepository groupRepository = new InMemoryGroupRepository();
        InMemoryMembershipRepository membershipRepository = new InMemoryMembershipRepository();
        UserDefaultGroupProvisioning adapter = adapter(groupRepository, membershipRepository);
        UUID userId = UUID.randomUUID();

        UUID groupId = adapter.ensureDefaultGroupProvisioned(userId, "   ");

        assertEquals("Personal", groupRepository.findById(groupId).orElseThrow().getName());
    }

    @Test
    void deterministicIdIsUnaffectedByInitialPlaceName() {
        InMemoryGroupRepository groupRepository = new InMemoryGroupRepository();
        InMemoryMembershipRepository membershipRepository = new InMemoryMembershipRepository();
        UserDefaultGroupProvisioning adapter = adapter(groupRepository, membershipRepository);
        UUID userId = UUID.randomUUID();
        UUID expectedDefaultGroupId = UUID.nameUUIDFromBytes(("personal-group:" + userId).getBytes(StandardCharsets.UTF_8));

        UUID groupId = adapter.ensureDefaultGroupProvisioned(userId, "Custom Name");

        assertEquals(expectedDefaultGroupId, groupId);
        assertTrue(groupRepository.findById(expectedDefaultGroupId).isPresent());
    }

    @Test
    void isIdempotentOnRepeatedProvisioningForSameUser() {
        InMemoryGroupRepository groupRepository = new InMemoryGroupRepository();
        InMemoryMembershipRepository membershipRepository = new InMemoryMembershipRepository();
        UserDefaultGroupProvisioning adapter = adapter(groupRepository, membershipRepository);
        UUID userId = UUID.randomUUID();

        UUID first = adapter.ensureDefaultGroupProvisioned(userId);
        UUID second = adapter.ensureDefaultGroupProvisioned(userId);

        assertEquals(first, second);
        List<Membership> memberships = membershipRepository.findByUserId(userId);
        assertEquals(1, memberships.size());
        assertEquals(first, memberships.get(0).getGroupId());
        assertTrue(groupRepository.findById(first).isPresent());
    }

    private UserDefaultGroupProvisioning adapter(
            InMemoryGroupRepository groupRepository,
            InMemoryMembershipRepository membershipRepository
    ) {
        UserProvisioning noOpUserProvisioning = (proposedUserId, email) -> proposedUserId;
        var noOpActiveGroupSelection = (app.lifelinq.features.user.contract.UserActiveGroupSelection) (userId, groupId) -> {
        };
        var noOpUserProfileRead = (app.lifelinq.features.user.contract.UserProfileRead) userId ->
                new UserProfileView(null, null);
        GroupApplicationService service = GroupApplicationService.create(
                groupRepository,
                membershipRepository,
                new InMemoryInvitationRepository(),
                new InMemoryInvitationTokenGenerator(),
                new InMemoryInvitationShortCodeGenerator(),
                new NoOpGroupInvitationMailSender(),
                "http://localhost:8080",
                noOpUserProvisioning,
                (userId) -> null,
                noOpActiveGroupSelection,
                noOpUserProfileRead,
                Clock.fixed(Instant.parse("2026-02-26T00:00:00Z"), ZoneOffset.UTC)
        );
        return new UserDefaultGroupProvisioningAdapter(service);
    }
}
