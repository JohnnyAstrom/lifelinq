package app.lifelinq.features.household.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.features.household.domain.HouseholdRole;
import app.lifelinq.features.household.domain.Membership;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = HouseholdJpaTestApplication.class)
@ActiveProfiles("test")
class JpaMembershipRepositoryAdapterTest {

    @Autowired
    private MembershipJpaRepository membershipJpaRepository;

    @Test
    void savesAndLoadsRoundTrip() {
        JpaMembershipRepositoryAdapter adapter = new JpaMembershipRepositoryAdapter(
                membershipJpaRepository,
                new MembershipMapper()
        );
        UUID householdId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Membership membership = new Membership(householdId, userId, HouseholdRole.MEMBER);

        adapter.save(membership);
        List<Membership> loaded = adapter.findByHouseholdId(householdId);

        assertEquals(1, loaded.size());
        assertEquals(householdId, loaded.get(0).getHouseholdId());
        assertEquals(userId, loaded.get(0).getUserId());
        assertEquals(HouseholdRole.MEMBER, loaded.get(0).getRole());
    }

    @Test
    @Transactional
    void deletesByHouseholdAndUser() {
        JpaMembershipRepositoryAdapter adapter = new JpaMembershipRepositoryAdapter(
                membershipJpaRepository,
                new MembershipMapper()
        );
        UUID householdId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        adapter.save(new Membership(householdId, userId, HouseholdRole.MEMBER));

        boolean removed = adapter.deleteByHouseholdIdAndUserId(householdId, userId);

        assertTrue(removed);
        assertEquals(0, adapter.findByHouseholdId(householdId).size());
    }
}
