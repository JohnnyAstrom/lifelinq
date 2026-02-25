package app.lifelinq.features.group.domain;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class MembershipTest {

    @Test
    void requiresGroupId() {
        assertThrows(IllegalArgumentException.class, () -> new Membership(null, UUID.randomUUID(), GroupRole.MEMBER));
    }

    @Test
    void requiresUserId() {
        assertThrows(IllegalArgumentException.class, () -> new Membership(UUID.randomUUID(), null, GroupRole.MEMBER));
    }

    @Test
    void requiresRole() {
        assertThrows(IllegalArgumentException.class, () -> new Membership(UUID.randomUUID(), UUID.randomUUID(), null));
    }
}
