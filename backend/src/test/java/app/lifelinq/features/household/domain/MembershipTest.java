package app.lifelinq.features.household.domain;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class MembershipTest {

    @Test
    void requiresHouseholdId() {
        assertThrows(IllegalArgumentException.class, () -> new Membership(null, UUID.randomUUID(), HouseholdRole.MEMBER));
    }

    @Test
    void requiresUserId() {
        assertThrows(IllegalArgumentException.class, () -> new Membership(UUID.randomUUID(), null, HouseholdRole.MEMBER));
    }

    @Test
    void requiresRole() {
        assertThrows(IllegalArgumentException.class, () -> new Membership(UUID.randomUUID(), UUID.randomUUID(), null));
    }
}
