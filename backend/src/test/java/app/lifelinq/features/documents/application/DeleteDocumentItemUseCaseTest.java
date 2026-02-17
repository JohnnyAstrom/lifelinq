package app.lifelinq.features.documents.application;

import app.lifelinq.features.documents.domain.DocumentItem;
import app.lifelinq.features.documents.domain.DocumentRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeleteDocumentItemUseCaseTest {

    @Test
    void deletes_document_by_id_and_household() {
        InMemoryDocumentRepository repository = new InMemoryDocumentRepository();
        DeleteDocumentItemUseCase useCase = new DeleteDocumentItemUseCase(repository);
        UUID householdId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();

        boolean deleted = useCase.execute(householdId, documentId);

        assertThat(deleted).isTrue();
        assertThat(repository.lastHouseholdId).isEqualTo(householdId);
        assertThat(repository.lastDocumentId).isEqualTo(documentId);
    }

    @Test
    void rejects_missing_household_id() {
        DeleteDocumentItemUseCase useCase = new DeleteDocumentItemUseCase(new InMemoryDocumentRepository());

        assertThatThrownBy(() -> useCase.execute(null, UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejects_missing_document_id() {
        DeleteDocumentItemUseCase useCase = new DeleteDocumentItemUseCase(new InMemoryDocumentRepository());

        assertThatThrownBy(() -> useCase.execute(UUID.randomUUID(), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static final class InMemoryDocumentRepository implements DocumentRepository {
        private UUID lastDocumentId;
        private UUID lastHouseholdId;

        @Override
        public void save(DocumentItem item) {
            throw new UnsupportedOperationException("Not needed for this test");
        }

        @Override
        public List<DocumentItem> findByHouseholdId(UUID householdId, Optional<String> q) {
            throw new UnsupportedOperationException("Not needed for this test");
        }

        @Override
        public boolean deleteByIdAndHouseholdId(UUID id, UUID householdId) {
            this.lastDocumentId = id;
            this.lastHouseholdId = householdId;
            return true;
        }
    }
}
