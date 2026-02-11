package app.lifelinq.features.household.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.features.household.domain.Household;
import app.lifelinq.features.household.domain.HouseholdRole;
import app.lifelinq.features.household.domain.Membership;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = HouseholdJpaTestApplication.class)
@ActiveProfiles("test")
class JpaHouseholdRepositoryAdapterTest {

    @Autowired
    private HouseholdJpaRepository householdJpaRepository;

    @Test
    void savesAndLoadsRoundTrip() {
        JpaHouseholdRepositoryAdapter adapter = new JpaHouseholdRepositoryAdapter(
                householdJpaRepository,
                new HouseholdMapper()
        );
        Household household = new Household(UUID.randomUUID(), "Home");

        adapter.save(household);
        Optional<Household> loaded = adapter.findById(household.getId());

        assertTrue(loaded.isPresent());
        assertEquals(household.getId(), loaded.get().getId());
        assertEquals(household.getName(), loaded.get().getName());

        Membership membership = new Membership(loaded.get().getId(), UUID.randomUUID(), HouseholdRole.MEMBER);
        assertEquals(loaded.get().getId(), membership.getHouseholdId());
    }
}
