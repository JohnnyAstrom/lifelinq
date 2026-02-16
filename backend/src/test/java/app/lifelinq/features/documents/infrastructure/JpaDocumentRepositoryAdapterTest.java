package app.lifelinq.features.documents.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import app.lifelinq.features.documents.domain.DocumentItem;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = DocumentsJpaTestApplication.class)
@ActiveProfiles("test")
class JpaDocumentRepositoryAdapterTest {

    @Autowired
    private DocumentJpaRepository repository;

    @Test
    void search_matches_notes_only_and_is_household_scoped() {
        UUID householdId = UUID.randomUUID();
        UUID otherHouseholdId = UUID.randomUUID();
        repository.save(new DocumentEntity(
                UUID.randomUUID(),
                householdId,
                UUID.randomUUID(),
                "Warranty",
                "Kitchen FRIDGE papers",
                null,
                null,
                List.of(),
                null,
                Instant.parse("2026-02-10T10:00:00Z")
        ));
        repository.save(new DocumentEntity(
                UUID.randomUUID(),
                otherHouseholdId,
                UUID.randomUUID(),
                "Other",
                "fridge copy",
                null,
                null,
                List.of(),
                null,
                Instant.parse("2026-02-11T10:00:00Z")
        ));

        JpaDocumentRepositoryAdapter adapter = new JpaDocumentRepositoryAdapter(repository, new DocumentMapper());
        List<DocumentItem> items = adapter.findByHouseholdId(householdId, Optional.of("ridg"));

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getHouseholdId()).isEqualTo(householdId);
        assertThat(items.get(0).getNotes()).contains("FRIDGE");
    }

    @Test
    void returns_created_at_desc_for_list_and_search() {
        UUID householdId = UUID.randomUUID();
        repository.save(new DocumentEntity(
                UUID.randomUUID(),
                householdId,
                UUID.randomUUID(),
                "Older",
                "invoice",
                null,
                null,
                List.of(),
                null,
                Instant.parse("2026-02-10T10:00:00Z")
        ));
        repository.save(new DocumentEntity(
                UUID.randomUUID(),
                householdId,
                UUID.randomUUID(),
                "Newer",
                "invoice",
                null,
                null,
                List.of(),
                null,
                Instant.parse("2026-02-11T10:00:00Z")
        ));

        JpaDocumentRepositoryAdapter adapter = new JpaDocumentRepositoryAdapter(repository, new DocumentMapper());
        List<DocumentItem> listItems = adapter.findByHouseholdId(householdId, Optional.empty());
        List<DocumentItem> searchItems = adapter.findByHouseholdId(householdId, Optional.of("invoice"));

        assertThat(listItems).extracting(DocumentItem::getTitle).containsExactly("Newer", "Older");
        assertThat(searchItems).extracting(DocumentItem::getTitle).containsExactly("Newer", "Older");
    }
}
