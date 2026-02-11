package app.lifelinq.features.shopping.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.lifelinq.features.shopping.domain.ShoppingItem;
import app.lifelinq.features.shopping.domain.ShoppingItemRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = ShoppingJpaTestApplication.class)
@ActiveProfiles("test")
class JpaShoppingItemRepositoryAdapterTest {

    @Autowired
    private ShoppingItemRepository repository;

    @Test
    void savesAndLoadsShoppingItemRoundTrip() {
        ShoppingItem item = new ShoppingItem(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Milk",
                Instant.now()
        );

        repository.save(item);

        Optional<ShoppingItem> loaded = repository.findById(item.getId());
        assertTrue(loaded.isPresent());
        assertEquals(item.getId(), loaded.get().getId());
        assertEquals(item.getHouseholdId(), loaded.get().getHouseholdId());
        assertEquals(item.getName(), loaded.get().getName());
        long diffNanos = Math.abs(Duration.between(item.getCreatedAt(), loaded.get().getCreatedAt()).toNanos());
        assertTrue(diffNanos <= 1_000);
    }
}
