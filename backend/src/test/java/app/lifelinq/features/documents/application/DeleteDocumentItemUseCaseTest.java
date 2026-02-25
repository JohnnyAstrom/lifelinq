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
    void deletes_document_by_id_and_group() {
        InMemoryDocumentRepository repository = new InMemoryDocumentRepository();
        DeleteDocumentItemUseCase useCase = new DeleteDocumentItemUseCase(repository);
        UUID groupId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();

        boolean deleted = useCase.execute(groupId, documentId);

        assertThat(deleted).isTrue();
        assertThat(repository.lastGroupId).isEqualTo(groupId);
        assertThat(repository.lastDocumentId).isEqualTo(documentId);
    }

    @Test
    void rejects_missing_group_id() {
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
        private UUID lastGroupId;

        @Override
        public void save(DocumentItem item) {
            throw new UnsupportedOperationException("Not needed for this test");
        }

        @Override
        public List<DocumentItem> findByGroupId(UUID groupId, Optional<String> q) {
            throw new UnsupportedOperationException("Not needed for this test");
        }

        @Override
        public boolean deleteByIdAndGroupId(UUID id, UUID groupId) {
            this.lastDocumentId = id;
            this.lastGroupId = groupId;
            return true;
        }
    }
}
