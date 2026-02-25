package app.lifelinq.features.documents.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DocumentItemTest {

    @Test
    void createsItemWhenValid() {
        UUID id = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        UUID createdByUserId = UUID.randomUUID();
        DocumentItem item = new DocumentItem(
                id,
                groupId,
                createdByUserId,
                "Pay rent",
                null,
                null,
                null,
                List.of(),
                null,
                Instant.now()
        );

        assertEquals(id, item.getId());
        assertEquals(groupId, item.getGroupId());
        assertEquals(createdByUserId, item.getCreatedByUserId());
        assertEquals("Pay rent", item.getTitle());
    }

    @Test
    void requiresId() {
        assertThrows(IllegalArgumentException.class,
                () -> new DocumentItem(
                        null,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "Pay rent",
                        null,
                        null,
                        null,
                        List.of(),
                        null,
                        Instant.now()
                ));
    }

    @Test
    void requiresGroupId() {
        assertThrows(IllegalArgumentException.class,
                () -> new DocumentItem(
                        UUID.randomUUID(),
                        null,
                        UUID.randomUUID(),
                        "Pay rent",
                        null,
                        null,
                        null,
                        List.of(),
                        null,
                        Instant.now()
                ));
    }

    @Test
    void requiresCreatedByUserId() {
        assertThrows(IllegalArgumentException.class,
                () -> new DocumentItem(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        null,
                        "Pay rent",
                        null,
                        null,
                        null,
                        List.of(),
                        null,
                        Instant.now()
                ));
    }

    @Test
    void requiresTitle() {
        assertThrows(IllegalArgumentException.class,
                () -> new DocumentItem(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        " ",
                        null,
                        null,
                        null,
                        List.of(),
                        null,
                        Instant.now()
                ));
    }

    @Test
    void requiresCreatedAt() {
        assertThrows(IllegalArgumentException.class,
                () -> new DocumentItem(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "Pay rent",
                        null,
                        null,
                        null,
                        List.of(),
                        null,
                        null
                ));
    }
}
