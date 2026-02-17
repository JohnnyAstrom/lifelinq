package app.lifelinq.features.documents.application;

import app.lifelinq.features.documents.domain.DocumentItem;
import app.lifelinq.features.documents.domain.DocumentRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ListDocumentsUseCaseTest {

    @Test
    void lists_documents_by_household() {
        UUID householdId = UUID.randomUUID();
        InMemoryDocumentRepository repository = new InMemoryDocumentRepository();
        repository.items.add(new DocumentItem(
                UUID.randomUUID(),
                householdId,
                UUID.randomUUID(),
                "Title",
                null,
                null,
                null,
                List.of(),
                null,
                Instant.now()
        ));
        ListDocumentsUseCase useCase = new ListDocumentsUseCase(repository);

        List<DocumentItem> items = useCase.execute(householdId, Optional.empty());

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getHouseholdId()).isEqualTo(householdId);
        assertThat(repository.lastQuery).isEmpty();
    }

    @Test
    void treats_whitespace_query_as_absent() {
        UUID householdId = UUID.randomUUID();
        InMemoryDocumentRepository repository = new InMemoryDocumentRepository();
        ListDocumentsUseCase useCase = new ListDocumentsUseCase(repository);

        useCase.execute(householdId, Optional.of("   "));

        assertThat(repository.lastQuery).isEmpty();
    }

    @Test
    void rejects_missing_household_id() {
        ListDocumentsUseCase useCase = new ListDocumentsUseCase(new InMemoryDocumentRepository());

        assertThatThrownBy(() -> useCase.execute(null, Optional.empty()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static final class InMemoryDocumentRepository implements DocumentRepository {
        private final List<DocumentItem> items = new ArrayList<>();
        private Optional<String> lastQuery = Optional.empty();

        @Override
        public void save(DocumentItem item) {
            items.add(item);
        }

        @Override
        public List<DocumentItem> findByHouseholdId(UUID householdId, Optional<String> q) {
            this.lastQuery = q;
            return new ArrayList<>(items);
        }

        @Override
        public boolean deleteByIdAndHouseholdId(UUID id, UUID householdId) {
            throw new UnsupportedOperationException("Not needed for this test");
        }
    }
}
