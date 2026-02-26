package app.lifelinq.features.group.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.features.group.application.GroupApplicationService;
import app.lifelinq.features.group.contract.UserDefaultGroupProvisioning;
import app.lifelinq.features.group.domain.GroupRole;
import app.lifelinq.features.group.domain.Membership;
import app.lifelinq.features.user.contract.UserProvisioning;
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
        UserProvisioning noOpUserProvisioning = userId -> {
        };
        GroupApplicationService service = GroupApplicationService.create(
                groupRepository,
                membershipRepository,
                new InMemoryInvitationRepository(),
                new InMemoryInvitationTokenGenerator(),
                noOpUserProvisioning,
                Clock.fixed(Instant.parse("2026-02-26T00:00:00Z"), ZoneOffset.UTC)
        );
        return new UserDefaultGroupProvisioningAdapter(service);
    }
}
