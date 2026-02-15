package app.lifelinq.features.documents.application;

import app.lifelinq.features.documents.domain.DocumentItem;
import app.lifelinq.features.documents.domain.DocumentRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateDocumentItemUseCaseTest {

    @Test
    void creates_document_item_with_metadata() {
        InMemoryDocumentRepository repository = new InMemoryDocumentRepository();
        CreateDocumentItemUseCase useCase = new CreateDocumentItemUseCase(repository);
        UUID householdId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant createdAt = Instant.now();

        CreateDocumentItemResult result = useCase.execute(new CreateDocumentItemCommand(
                householdId,
                userId,
                "Warranty",
                "Kitchen fridge",
                LocalDate.of(2026, 2, 1),
                "Appliances",
                List.of("warranty", "fridge"),
                "https://example.com",
                createdAt
        ));

        assertThat(result.getItemId()).isNotNull();
        assertThat(repository.savedItem).isNotNull();
        assertThat(repository.savedItem.getHouseholdId()).isEqualTo(householdId);
        assertThat(repository.savedItem.getCreatedByUserId()).isEqualTo(userId);
        assertThat(repository.savedItem.getTitle()).isEqualTo("Warranty");
        assertThat(repository.savedItem.getNotes()).isEqualTo("Kitchen fridge");
        assertThat(repository.savedItem.getDate()).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(repository.savedItem.getCategory()).isEqualTo("Appliances");
        assertThat(repository.savedItem.getTags()).containsExactly("warranty", "fridge");
        assertThat(repository.savedItem.getExternalLink()).isEqualTo("https://example.com");
        assertThat(repository.savedItem.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void rejects_missing_created_by_user() {
        CreateDocumentItemUseCase useCase = new CreateDocumentItemUseCase(new InMemoryDocumentRepository());

        assertThatThrownBy(() -> useCase.execute(new CreateDocumentItemCommand(
                UUID.randomUUID(),
                null,
                "Title",
                null,
                null,
                null,
                null,
                null,
                Instant.now()
        ))).isInstanceOf(IllegalArgumentException.class);
    }

    private static final class InMemoryDocumentRepository implements DocumentRepository {
        private DocumentItem savedItem;

        @Override
        public void save(DocumentItem item) {
            this.savedItem = item;
        }

        @Override
        public List<DocumentItem> findByHouseholdId(UUID householdId) {
            throw new UnsupportedOperationException("Not needed for this test");
        }
    }
}
