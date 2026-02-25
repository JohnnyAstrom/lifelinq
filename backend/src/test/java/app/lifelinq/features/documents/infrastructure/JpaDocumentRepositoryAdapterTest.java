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
    void search_matches_notes_only_and_is_group_scoped() {
        UUID groupId = UUID.randomUUID();
        UUID otherGroupId = UUID.randomUUID();
        repository.save(new DocumentEntity(
                UUID.randomUUID(),
                groupId,
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
                otherGroupId,
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
        List<DocumentItem> items = adapter.findByGroupId(groupId, Optional.of("ridg"));

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getGroupId()).isEqualTo(groupId);
        assertThat(items.get(0).getNotes()).contains("FRIDGE");
    }

    @Test
    void returns_created_at_desc_for_list_and_search() {
        UUID groupId = UUID.randomUUID();
        repository.save(new DocumentEntity(
                UUID.randomUUID(),
                groupId,
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
                groupId,
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
        List<DocumentItem> listItems = adapter.findByGroupId(groupId, Optional.empty());
        List<DocumentItem> searchItems = adapter.findByGroupId(groupId, Optional.of("invoice"));

        assertThat(listItems).extracting(DocumentItem::getTitle).containsExactly("Newer", "Older");
        assertThat(searchItems).extracting(DocumentItem::getTitle).containsExactly("Newer", "Older");
    }

    @Test
    void applies_id_asc_tie_breaker_when_created_at_is_identical() {
        UUID groupId = UUID.randomUUID();
        Instant sameCreatedAt = Instant.parse("2026-02-12T10:00:00Z");
        UUID lowerId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID higherId = UUID.fromString("00000000-0000-0000-0000-000000000002");

        repository.save(new DocumentEntity(
                higherId,
                groupId,
                UUID.randomUUID(),
                "Invoice B",
                "invoice note",
                null,
                null,
                List.of(),
                null,
                sameCreatedAt
        ));
        repository.save(new DocumentEntity(
                lowerId,
                groupId,
                UUID.randomUUID(),
                "Invoice A",
                "invoice note",
                null,
                null,
                List.of(),
                null,
                sameCreatedAt
        ));

        JpaDocumentRepositoryAdapter adapter = new JpaDocumentRepositoryAdapter(repository, new DocumentMapper());
        List<DocumentItem> listItems = adapter.findByGroupId(groupId, Optional.empty());
        List<DocumentItem> searchItems = adapter.findByGroupId(groupId, Optional.of("invoice"));

        assertThat(listItems).extracting(DocumentItem::getId).containsExactly(lowerId, higherId);
        assertThat(searchItems).extracting(DocumentItem::getId).containsExactly(lowerId, higherId);
    }

    @Test
    void deletes_document_only_within_same_group() {
        UUID groupId = UUID.randomUUID();
        UUID otherGroupId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();

        repository.save(new DocumentEntity(
                targetId,
                groupId,
                UUID.randomUUID(),
                "Target",
                null,
                null,
                null,
                List.of(),
                null,
                Instant.parse("2026-02-10T10:00:00Z")
        ));
        repository.save(new DocumentEntity(
                otherId,
                otherGroupId,
                UUID.randomUUID(),
                "Other",
                null,
                null,
                null,
                List.of(),
                null,
                Instant.parse("2026-02-11T10:00:00Z")
        ));

        JpaDocumentRepositoryAdapter adapter = new JpaDocumentRepositoryAdapter(repository, new DocumentMapper());

        boolean deleted = adapter.deleteByIdAndGroupId(targetId, groupId);

        assertThat(deleted).isTrue();
        assertThat(repository.findById(targetId)).isEmpty();
        assertThat(repository.findById(otherId)).isPresent();
    }

    @Test
    void does_not_delete_document_from_other_group() {
        UUID groupId = UUID.randomUUID();
        UUID otherGroupId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        repository.save(new DocumentEntity(
                targetId,
                otherGroupId,
                UUID.randomUUID(),
                "Other group",
                null,
                null,
                null,
                List.of(),
                null,
                Instant.parse("2026-02-10T10:00:00Z")
        ));

        JpaDocumentRepositoryAdapter adapter = new JpaDocumentRepositoryAdapter(repository, new DocumentMapper());

        boolean deleted = adapter.deleteByIdAndGroupId(targetId, groupId);

        assertThat(deleted).isFalse();
        assertThat(repository.findById(targetId)).isPresent();
    }
}
