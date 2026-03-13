package app.lifelinq.features.shopping.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.features.shopping.domain.ShoppingCategory;
import app.lifelinq.features.shopping.domain.ShoppingCategoryPreference;
import app.lifelinq.features.shopping.domain.ShoppingCategoryPreferenceRepository;
import app.lifelinq.features.shopping.domain.ShoppingListType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = ShoppingJpaTestApplication.class)
@ActiveProfiles("test")
class JpaShoppingCategoryPreferenceRepositoryAdapterTest {

    @Autowired
    private ShoppingCategoryPreferenceRepository repository;

    @Test
    void savesAndLoadsCategoryPreferenceRoundTrip() {
        UUID groupId = UUID.randomUUID();
        Instant updatedAt = Instant.now();
        repository.save(new ShoppingCategoryPreference(
                groupId,
                ShoppingListType.GROCERY,
                "apple",
                ShoppingCategory.PRODUCE,
                updatedAt
        ));

        List<ShoppingCategoryPreference> preferences = repository.findByGroupId(groupId);
        assertEquals(1, preferences.size());
        assertEquals("apple", preferences.get(0).normalizedTitle());
        assertEquals(ShoppingCategory.PRODUCE, preferences.get(0).preferredCategory());
        assertTrue(repository.findByGroupIdAndListTypeAndNormalizedTitle(groupId, ShoppingListType.GROCERY, "apple").isPresent());
    }

    @Test
    void upsertsByGroupAndNormalizedTitle() {
        UUID groupId = UUID.randomUUID();
        repository.save(new ShoppingCategoryPreference(
                groupId,
                ShoppingListType.GROCERY,
                "apple",
                ShoppingCategory.PRODUCE,
                Instant.now()
        ));
        repository.save(new ShoppingCategoryPreference(
                groupId,
                ShoppingListType.GROCERY,
                "apple",
                ShoppingCategory.PANTRY,
                Instant.now().plusSeconds(5)
        ));

        List<ShoppingCategoryPreference> preferences = repository.findByGroupId(groupId);
        assertEquals(1, preferences.size());
        assertEquals(ShoppingCategory.PANTRY, preferences.get(0).preferredCategory());
    }

    @Test
    void deletesExistingPreferenceForGroupTypeAndTitle() {
        UUID groupId = UUID.randomUUID();
        repository.save(new ShoppingCategoryPreference(
                groupId,
                ShoppingListType.SUPPLIES,
                "tape",
                ShoppingCategory.HOUSEHOLD,
                Instant.now()
        ));

        repository.deleteByGroupIdAndListTypeAndNormalizedTitle(groupId, ShoppingListType.SUPPLIES, "tape");

        assertTrue(repository.findByGroupIdAndListTypeAndNormalizedTitle(groupId, ShoppingListType.SUPPLIES, "tape").isEmpty());
    }
}
