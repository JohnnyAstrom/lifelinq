package app.lifelinq.features.shopping.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.features.shopping.domain.ShoppingCategory;
import app.lifelinq.features.shopping.domain.ShoppingCategoryPreference;
import app.lifelinq.features.shopping.domain.ShoppingListType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InMemoryShoppingCategoryPreferenceRepositoryTest {

    @Test
    void savesAndFindsPreferencesByGroup() {
        InMemoryShoppingCategoryPreferenceRepository repository = new InMemoryShoppingCategoryPreferenceRepository();
        UUID groupId = UUID.randomUUID();
        ShoppingCategoryPreference preference = new ShoppingCategoryPreference(
                groupId,
                ShoppingListType.GROCERY,
                "apple",
                ShoppingCategory.PRODUCE,
                Instant.now()
        );

        repository.save(preference);

        List<ShoppingCategoryPreference> preferences = repository.findByGroupId(groupId);
        assertEquals(1, preferences.size());
        assertEquals("apple", preferences.get(0).normalizedTitle());
        assertEquals(ShoppingCategory.PRODUCE, preferences.get(0).preferredCategory());
    }

    @Test
    void updatesExistingPreferenceForSameGroupAndTitle() {
        InMemoryShoppingCategoryPreferenceRepository repository = new InMemoryShoppingCategoryPreferenceRepository();
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
                Instant.now().plusSeconds(10)
        ));

        List<ShoppingCategoryPreference> preferences = repository.findByGroupId(groupId);
        assertEquals(1, preferences.size());
        assertEquals(ShoppingCategory.PANTRY, preferences.get(0).preferredCategory());
        assertTrue(repository.findByGroupIdAndListTypeAndNormalizedTitle(groupId, ShoppingListType.GROCERY, "apple").isPresent());
    }
}
