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
    void lists_documents_by_group() {
        UUID groupId = UUID.randomUUID();
        InMemoryDocumentRepository repository = new InMemoryDocumentRepository();
        repository.items.add(new DocumentItem(
                UUID.randomUUID(),
                groupId,
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

        List<DocumentItem> items = useCase.execute(groupId, Optional.empty());

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getGroupId()).isEqualTo(groupId);
        assertThat(repository.lastQuery).isEmpty();
    }

    @Test
    void treats_whitespace_query_as_absent() {
        UUID groupId = UUID.randomUUID();
        InMemoryDocumentRepository repository = new InMemoryDocumentRepository();
        ListDocumentsUseCase useCase = new ListDocumentsUseCase(repository);

        useCase.execute(groupId, Optional.of("   "));

        assertThat(repository.lastQuery).isEmpty();
    }

    @Test
    void rejects_missing_group_id() {
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
        public List<DocumentItem> findByGroupId(UUID groupId, Optional<String> q) {
            this.lastQuery = q;
            return new ArrayList<>(items);
        }

        @Override
        public boolean deleteByIdAndGroupId(UUID id, UUID groupId) {
            throw new UnsupportedOperationException("Not needed for this test");
        }
    }
}
